/*
 * Copyright (C) 2012 Thasso Griebel
 *
 * This file is part of JIP.
 *
 * JIP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JIP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package jip.plugin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * In memory plugin registry that uses reflection to search for
 * plugins
 */
public class PluginRegistryImpl implements PluginRegistry {
    /**
     * The logger
     */
    private static Logger log = LoggerFactory.getLogger(PluginRegistryImpl.class);
    /**
     * Map from the extension point to the referenced interface or class
     */
    public Set<Class<?>> extensionPoints;
    /**
     * Map from the extension to the implementing class
     */
    public Multimap<Class, Class> extensions;

    /**
     * The injector
     */
    private Injector injector;

    /**
     * New plugin registry
     *
     * @param injector the main injector
     */
    @Inject
    public PluginRegistryImpl(Injector injector) {
        if(injector == null) throw new NullPointerException("NULL injector not permitted");
        this.injector = injector;
    }

    public PluginRegistryImpl(Injector injector, PluginRegistry source) {
        this(injector);
        this.extensions = source.getExtensions();
        this.extensionPoints = source.getExtensionPoints();
    }

    @Override
    public Set<Class<?>> getExtensionPoints() {
        return extensionPoints;
    }

    @Override
    public Multimap<Class, Class> getExtensions() {
        return extensions;
    }

    /**
     * Search for extension points and extensions and initialize the registry
     */
    public void initialize(){
        if(extensionPoints == null || extensions == null){
            Reflections reflections = ReflectionUtils.get();
            extensionPoints = reflections.getTypesAnnotatedWith(ExtensionPoint.class, true);
            log.debug("Plugin registry : found " + extensionPoints.size() + " extension points");
            Set<Class<?>> extensions = reflections.getTypesAnnotatedWith(Extension.class, true);
            log.debug("Plugin registry : found " + extensions.size() + " extensions");
            this.extensions = ArrayListMultimap.create();
            // map extensions to extension points

            for (Class<?> extension : extensions) {
                boolean matched = false;
                for (Class<?> extensionPoint : extensionPoints) {
                    if(extensionPoint.isAssignableFrom(extension)){
                        this.extensions.put(extensionPoint, extension);
                        matched = true;
                    }
                }
                if(!matched){
                    log.warn("No extension point found for " + extension.getName());
                }
            }
            log.debug("Plugin registry : " + this.extensions.size() + " extensions registered");
        }
    }

    /**
     * Get an instance of all implementations of the specified extension point
     *
     * @param extensionPoint the extension point
     * @return instances set of instances implementing the extension point
     */
    public Set getInstances(Class extensionPoint){
        if(extensionPoint == null) throw new NullPointerException("NULL extension point not permitted");
        Set instances = new LinkedHashSet();
        if(extensions == null){
            initialize();
        }
        Collection<Class> classes = extensions.get(extensionPoint);
        for (Class cl: classes) {
            instances.add(injector.getInstance(cl));
        }
        return instances;
    }

    public Set<PluginExtension> getExtensions(Class extensionPoint) {
        if(extensionPoint == null) throw new NullPointerException("NULL extension point not permitted");
        Set instances = new LinkedHashSet();
        if(extensions == null){
            initialize();
        }
        Collection<Class> classes = extensions.get(extensionPoint);
        for (Class cl: classes) {
            Extension annotation = (Extension) cl.getAnnotation(Extension.class);
            String name = annotation.value();
            instances.add(new PluginExtension(cl, name));

        }
        return instances;
    }

    /**
     * Update the injector instance
     *
     * @param injector the injector instance
     */
    public void setInjector(Injector injector){
        this.injector = injector;
    }
}
