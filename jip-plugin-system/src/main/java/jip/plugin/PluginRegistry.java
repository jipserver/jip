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

import com.google.common.collect.Multimap;

import java.util.Set;

/**
 * Manage plugins
 */
public interface PluginRegistry {
    /**
     * Get an instance of all implementations of the specified extension point
     *
     * @param extensionPoint the extension point
     * @return instances set of instances implementing the extension point
     */
    <T> Set<T> getInstances(Class<T> extensionPoint);

    /**
     * Get the list of registered extensions
     *
     * @param extensionPoint the extension point
     * @return plugins the list of registered extensions
     */
    Set<PluginExtension> getExtensions(Class extensionPoint);

    /**
     * Map from the extension point to the referenced interface or class
     */
    Set<Class<?>> getExtensionPoints();

    /**
     * Map from the extension to the implementing class
     */
    Multimap<Class, Class> getExtensions();

    /**
     * A plugin wraps an extension point implementation (extension) and
     * provides access to the implementing clazz and the name
     */
    public class PluginExtension {
        private Class clazz;
        private String name;

        public PluginExtension(Class clazz, String name) {
            this.clazz = clazz;
            this.name = name;
        }

        public Class getClazz() {
            return clazz;
        }

        public String getName() {
            return name;
        }
    }
}
