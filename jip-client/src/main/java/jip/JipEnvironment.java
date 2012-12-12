package jip;

import java.io.File;

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
}
