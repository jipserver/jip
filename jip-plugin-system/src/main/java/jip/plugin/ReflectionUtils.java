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

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to search in the classpath or
 * in META-INF/reflections for annotated classes.
 * <p>
 *     If the system property "jip.plugins.search" is set,
 *     the scanner actively searches the classpath, otherwise,
 *     the META-INF/reflections/*-reflections.xml files are
 *     loaded from all jars in classpath.
 * </p>
 *
 */
public class ReflectionUtils {
    /**
     * The logger
     */
    public static Logger log = LoggerFactory.getLogger(ReflectionUtils.class);
    /**
     * The instance
     */
    static Reflections reflections;

    /**
     * Get the initialized reflections instance
     *
     * @return reflections the initialized reflections instance
     */
    public static Reflections get() {
        if (reflections == null) {
            if(!System.getProperty("jip.plugins.search", "false").equals("false")){
                reflections = new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forJavaClassPath())
                        .addUrls(ClasspathHelper.forManifest())
                        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("jip")))
                );
            }else{
                reflections = Reflections.collect();
            }
            log.debug("Reflection loaded found {} keys and {} values",
                    reflections.getStore().getKeysCount(),
                    reflections.getStore().getValuesCount());
        }
        return reflections;
    }


}
