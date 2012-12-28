package jip;

import groovy.util.ConfigObject;

import java.io.File;
import java.util.Map;

/**
 * JIP runtime environment
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface JipEnvironment {

    /**
     * Get the path to the jip home
     *
     * @param user if true, the user specific home is returned
     * @return home the jip home
     */
    public File getJipHome(boolean user);

    /**
     * Get the current JIP configuration
     *
     * @return config the current configuration
     */
    public Map<String, Object> getConfiguration();
}
