package jip;

import com.google.gson.Gson;
import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

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
     * The configuration
     */
    private static Map<String, Object> configuration;

    /**
     * Load the current configuration
     *
     * @param jipHome the jip installation location
     * @param userhome the jip user home
     * @return config the configuration map
     */
    public static Map<String, Object> load(File jipHome, File userhome) {
        if(configuration == null){
            // load default configuration
            URL defaultConfiguration = JipConfiguration.class.getResource("/Configuration.json");
            Gson parser = new Gson();
            try {
                configuration = parser.fromJson(new InputStreamReader(defaultConfiguration.openStream()), Map.class);
                File jipConfig = new File(jipHome, "conf/Config.groovy");
                if(jipConfig.exists()){
                    configuration.putAll(parser.fromJson(new FileReader(jipConfig), Map.class));
                }
                File userConfig = new File(userhome, "conf/Config.groovy");
                if(userConfig.exists()){
                    configuration.putAll(parser.fromJson(new FileReader(userConfig), Map.class));
                }
            } catch (IOException e) {
                log.error("Error while reading configuration : {}", e.getMessage());
                throw new RuntimeException(e);
            }


        }
        return configuration;
    }

    public static Object get(Map<String, Object> cfg, String... keys){
        if(keys.length == 1){
            return cfg.get(keys[0]);
        }else{
            return get((Map<String, Object>) cfg.get(keys[0]), Arrays.asList(keys).subList(1, keys.length).toArray(new String[keys.length-1]));
        }
    }
}
