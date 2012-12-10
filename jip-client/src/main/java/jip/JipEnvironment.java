package jip;

import jip.runner.JipExecutor;

import java.io.File;
import java.util.List;

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
     * Get execute utils initialized with the environments
     * for a given list of installer
     *
     * @param workingDir the working directory of the execute utilities
     * @param installer optional list of installer
     * @return executeUtils the execute utilities
     */
    public JipExecutor getExecuteUtilities(File workingDir, List<String> installer);
}
