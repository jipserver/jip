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

import com.google.inject.Module;

import javax.inject.Singleton;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

/**
 * General plugin interface
 */
@ExtensionPoint
@Singleton
public interface Plugin {
    /**
     * Called when the plugin is started
     *
     * @throws Exception in case of an error
     */
    public void start() throws Exception;

    /**
     * Called when the plugin is stopped
     *
     * @throws Exception in case of an error
     */
    public void stop() throws Exception;

    /**
     * Return the name of the plugin
     *
     * @return name the name of the plugin
     */
    public String getName();

    /**
     * return a key that identifies the plugin. The key is also
     * used as a prefix for the configuration entries
     *
     * @return key plugin key
     */
    public String getKey();

    /**
     * Returns a list of modules provided by this plugin. Can return null
     * if no modules are provided
     *
     * @return modules the guice modules
     */
    public List<Module> getModules();

    /**
     * Get the plugin properties. Do not add the plugin key here, it is added
     * by the plugin system automatically at startup
     *
     * @return properties the properties
     *
     */
    public Properties getConfiguration();

}
