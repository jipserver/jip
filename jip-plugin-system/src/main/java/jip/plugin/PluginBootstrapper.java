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
 * along with JIP.  If not, see <http://www.gnu.org/licenses/>.
 */

package jip.plugin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.*;
import com.google.inject.name.Names;
import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Initialize the plugin system
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class PluginBootstrapper {
    /**
     * the logger
     */
    private static final Logger log = LoggerFactory.getLogger(PluginBootstrapper.class);
    /**
     * The properties
     */
    private final Properties properties;
    /**
     * The local properties location
     */
    private final File localProperties;
    /**
     * Plugins foudn by the boostrapper
     */
    private Set<Plugin> plugins;
    /**
     * User provided properties
     */
    private Properties providedProperties;
    /**
     * List of primary modules
     */
    private List<Module> primaryModules;
    /**
     * Main injector created by this bootstrapper
     */
    private Injector mainInjector;
    /**
     * The plugin registry
     */
    private BoostrapPluginRegistry boostrapRegistry;

    /**
     * Create a bew bootstrapper
     *
     * @param properties the initial properties
     * @param localProperties (optional) path to a local properties file
     */
    public PluginBootstrapper(Properties properties, File localProperties) {
        this(properties, localProperties, Collections.EMPTY_LIST);
    }

    /**
     * Create a bew bootstrapper
     *
     * @param properties the initial properties
     * @param localProperties (optional) path to a local properties file
     * @param primaryModules list of primary modules
     */
    public PluginBootstrapper(Properties properties, File localProperties, List<Module> primaryModules) {
        this.providedProperties = properties;
        this.primaryModules = primaryModules;
        this.properties = new Properties();
        this.localProperties = localProperties;
    }

    /**
     * Returns a view on the properties. Modifications will not have any effect.
     *
     * @return properties the properties
     */
    public Properties getProperties(){
        return new Properties(properties);
    }

    /**
     * Access the main injecor created by this bootstrapper
     *
     * @return main the main injector
     */
    public Injector getMainInjector() {
        return mainInjector;
    }

    /**
     * Boostrap the plugin system and create an injector.
     * This loads all plugins, extends teh configuration with
     * the default plugins configuration, overrides with
     * properties from a local file and finally overrides with
     * system properties. The created properties are bound and an injector
     * is created from all plugin modules.
     *
     * @return injector the injector
     * @throws Exception in case a plugin could not be loaded
     */
    public Injector bootstrap(boolean addShutdownHooks) throws Exception{
        log.debug("Creating bootstrap injector");
        Injector boostrapInjector = Guice.createInjector();
        log.info("Loading plugins");
        plugins = loadPlugins(boostrapInjector);
        log.info("Loaded plugins : " + plugins.size() + " plugins");
        log.info("Loading configuration");
        extendWithPluginProperties(plugins);
        extendFromFile();
        extendFromSystem();
        extendFromProvided();
        log.info("Loaded configuration : " +properties.size() + " entries");
        log.debug("------------------ JIP Server and System Properties ------------------");
        List<String> propertynames = new ArrayList<String>(getProperties().stringPropertyNames());
        Collections.sort(propertynames);
        for (String name : propertynames) {
            log.debug("{} : {}", name, getProperties().getProperty(name));
        }
        log.debug("------------------ JIP Server Properties ------------------");


        List<Module> modules = new ArrayList<Module>(this.primaryModules);
        modules.addAll(extractModules(plugins));
        modules.add(0, new BoostrapModule(properties, this, plugins));
        log.debug("Creating main injector");
        mainInjector = createMainInjector(modules, boostrapInjector);
        // update the plugins with the instances from the new injector
//        plugins = loadPlugins(scope, mainInjector);

        log.info("Injecting dependencies to plugins");
        for (Plugin plugin : plugins) {
            mainInjector.injectMembers(plugin);
        }

        //add shutdown hooks
        if(addShutdownHooks){
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    for (Plugin plugin : plugins) {
                        try {
                            plugin.stop();
                        } catch (Exception err) {
                            log.error("Error while stopping plugin : " + plugin.getName(), err);
                        }
                    }
                }
            });
        }
        return mainInjector;
    }


    /**
     * Start all plugins. Exceptions are temporarily catched, but rethrown after
     * all plugins were started. Its up to you to continue or stop in case of
     * a plugin startup failure.
     *
     */
    public void startPlugins() throws BoostrapException{
        if(plugins == null) throw new BoostrapException("Bootstrapper not initialized, no plugins found!");

        BoostrapException boostrapException = new BoostrapException("Plugin startup failed", new ArrayList<Exception>(), new ArrayList<Plugin>());
        for (Plugin plugin : plugins) {
            try {
                log.info("Starting plugin : " + plugin.getName());
                plugin.start();
            } catch (Exception e) {
                log.error("Error while starting plugin "+plugin.getName() + " : " +e.getMessage());
                boostrapException.getFailedPlugins().add(plugin);
                boostrapException.getCauses().add(e);
            }
        }
        if(boostrapException.getFailedPlugins().size() > 0){
            throw boostrapException;
        }
    }


    /**
     * Extracts all modules form the set of plugins
     *
     * @param plugins the plugins
     * @return modules list of available modules
     */
    protected List<Module> extractModules(Set<Plugin> plugins){
        ArrayList<Module> modules = new ArrayList<Module>();
        for (Plugin plugin : plugins) {
            List<Module> pluginmodules = plugin.getModules();
            if(pluginmodules != null) modules.addAll(pluginmodules);
        }
        return  modules;
    }

    /**
     * Initial search for plugins. NOTE that plugins are not
     * started here, and they should not have any constructor dependencies.
     *
     * <p>
     *     This also loads plugin configurations, and an exception is thrown
     *     in case the a plugin is not valid, i.e. it as no key assigned
     * </p>
     *
     *
     * @param boostrapInjector the bootstrap injector
     * @return plugins all found plugins
     */
    protected Set<Plugin> loadPlugins(Injector boostrapInjector){
        boostrapRegistry = new BoostrapPluginRegistry(boostrapInjector);
        boostrapRegistry.initialize();
        // 2. find all server plugins
        Set<Plugin> all = boostrapRegistry.getInstances(Plugin.class);
        Set<Plugin> matchingScope = new HashSet<Plugin>();
        for (Plugin o : all) {
            matchingScope.add(o);
        }
        return matchingScope;
    }

    /**
     * Add plugin properties to the global configuration
     *
     * @param plugins the plugins
     */
    protected void extendWithPluginProperties(Set<Plugin> plugins) {
        for (Plugin plugin : plugins) {
            log.info("Loading configuration for " +plugin.getName());
            String key = plugin.getKey();
            if(key == null || key.isEmpty()){
                log.error("Plugin "+ plugin.getName() + " has a null or empty key ! This is not permitted !");
                throw new RuntimeException("Plugin "+ plugin.getName() + " has a null or empty key ! This is not permitted !");
            }
            Properties pluginCfg = plugin.getConfiguration();
            if(pluginCfg != null){
                for (String property : pluginCfg.stringPropertyNames()) {
                    properties.put(key+"."+property, pluginCfg.getProperty(property));
                }
            }
        }
    }

    /**
     * Extend properties from user provided properties
     */
    protected void extendFromProvided() {
        if(providedProperties != null){
            this.properties.putAll(providedProperties);
        }
    }


    /**
     * Extend the properties from the file
     *
     * @throws IOException in case an error occurs while reading the file
     */
    protected void extendFromFile() throws IOException {
        if(localProperties != null && localProperties.exists()) {
            if(localProperties.getName().endsWith(".groovy")){
                // use the groovy slurper
                ConfigSlurper slurper = new ConfigSlurper();
                ConfigObject cfg = slurper.parse(localProperties.toURI().toURL());
                Properties local = cfg.toProperties();
                properties.putAll(local);
            }else{
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(localProperties));
                    Properties local = new Properties();
                    local.load(reader);
                    properties.putAll(local);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
        }
    }

    /**
     * Put all system properties into the current properties
     *
     * @throws IOException in case an error occurs while reading the file
     */
    protected void extendFromSystem() throws IOException {
        properties.putAll(System.getProperties());
    }

    /**
     * Create the main injector.
     *
     * @return injector the main injector
     */
    protected Injector createMainInjector(List<Module> modules, Injector boostrapInjector){
        return boostrapInjector.createChildInjector(modules);
//        return Guice.createInjector(modules);
    }


    /**
     * Bootstrap module that adds constant named bindings for all properties
     */
    private static class BoostrapModule extends AbstractModule{
        private Properties properties;
        private PluginBootstrapper bootstrapper;
        private Set<Plugin> plugins;

        private BoostrapModule(Properties properties, PluginBootstrapper bootstrapper, Set<Plugin> plugins) {
            this.properties = properties;
            this.bootstrapper = bootstrapper;
            this.plugins = plugins;
        }

        @Override
        protected void configure() {
            if(properties != null){
                Properties copy = new Properties();
                for (String property : properties.stringPropertyNames()) {
                    String value = properties.getProperty(property);
                    if(value != null) copy.setProperty(property, value);
                }
                Names.bindProperties(binder(), copy);
            }
            // bind the plugin registry but make sure we use the main injector
            bind(PluginRegistry.class).toProvider(new Provider<PluginRegistry>() {
                private PluginRegistryImpl pluginRegistry;

                @Override
                public PluginRegistry get() {
                    if (pluginRegistry == null) {
                        Multimap<Class, Class> extensions = bootstrapper.boostrapRegistry.extensions;
                        Set<Class<?>> extensionPoints = bootstrapper.boostrapRegistry.extensionPoints;
                        pluginRegistry = new PluginRegistryImpl(bootstrapper.mainInjector, bootstrapper.boostrapRegistry);
                    }
                    return pluginRegistry;
                }
            }).in(Scopes.SINGLETON);
            bind(PluginBootstrapper.class).toInstance(bootstrapper);
        }
    }

    /**
     * Boostrap plugin registry that allows loading extension classes
     *
     * @author Thasso Griebel <thasso.griebel@gmail.com>
     */
    private static class BoostrapPluginRegistry implements PluginRegistry {
        /**
         * The logger
         */
        private static Logger log = LoggerFactory.getLogger(BoostrapPluginRegistry.class);
        /**
         * Map from the extension point to the referenced interface or class
         */
        public Set<Class<?>> extensionPoints;
        /**
         * Map from the extension to the implementing class
         */
        public Multimap<Class, Class> extensions;
        /**
         * Injector without any dependencies
         */
        private final Injector injector;

        /**
         * Create with base injector
         *
         * @param injector the base injector
         */
        private BoostrapPluginRegistry(Injector injector) {
            this.injector = injector;
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
            Reflections reflections = ReflectionUtils.get();
            extensionPoints = reflections.getTypesAnnotatedWith(ExtensionPoint.class, true);
            log.info("Bootstrap registry : found " + extensionPoints.size() + " extension points");
            Set<Class<?>> extensions = reflections.getTypesAnnotatedWith(Extension.class, true);
            log.info("Bootstrap registry : found " + extensions.size() + " extensions");
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
            log.info("Bootstrap registry : " + this.extensions.size() + " extensions registered");
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

                //instances.add(injector.getInstance(cl));
                try {
                    instances.add(cl.newInstance());
                } catch (Exception e) {
                   throw new RuntimeException("Unable to create plugin instance : " + cl.getName(), e);
                }

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
    }
    
    
    public static class BoostrapException extends Exception{
        public List<Exception> causes;
        public List<Plugin> failedPlugins;

        public BoostrapException(String message) {
            super(message);
        }

        public BoostrapException(String message, List<Exception> causes, List<Plugin> failedPlugins) {
            super(message);
            this.causes = causes;
            this.failedPlugins = failedPlugins;
        }

        public List<Exception> getCauses() {
            return causes;
        }

        public List<Plugin> getFailedPlugins() {
            return failedPlugins;
        }
    }

}
