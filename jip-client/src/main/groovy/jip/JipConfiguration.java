package jip;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;

/**
 * Load JIP configurations
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class JipConfiguration {
    /**
     * The logger
     */
    private static Logger log = LoggerFactory.getLogger(JipConfiguration.class);

    /**
     * Load the current configuration
     *
     * @param jipHome the jip installation location
     * @param userhome the jip user home
     * @return
     */
    public static ConfigObject load(File jipHome, File userhome) {
        // create default configuration
        ConfigSlurper slurper = new ConfigSlurper();

        ConfigObject defaultConfig = new ConfigObject();
        ConfigObject jip = new ConfigObject();
        defaultConfig.put("jip", jip);
        jip.put("home", jipHome);
        jip.put("userdir", userhome);

        slurper.setBinding(defaultConfig);

        // load default configuration
        defaultConfig.merge(slurper.parse(JipConfiguration.class.getResource("/Configuration.groovy")));

        // load config from jip home
        File jipConfig = new File(jipHome, "conf/Config.groovy");
        if(jipConfig.exists()){
            try {
                defaultConfig.merge(slurper.parse(jipConfig.toURI().toURL()));
            } catch (MalformedURLException e) {
                log.error("Unable to load jip configuration from {}", jipConfig.getAbsolutePath());
            }
        }

        File userConfig = new File(userhome, "conf/Config.groovy");
        if(userConfig.exists()){
            try {
                defaultConfig.merge(slurper.parse(userConfig.toURI().toURL()));
            } catch (MalformedURLException e) {
                log.error("Unable to load jip configuration from {}", userConfig.getAbsolutePath());
            }
        }

        return defaultConfig;
    }
}
