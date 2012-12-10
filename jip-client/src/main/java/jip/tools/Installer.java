package jip.tools;

import java.io.File;
import java.util.Map;

/**
 * Tool installer bae interface
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface Installer {
    /**
     * Return a name for this installer
     *
     * @return name the name for this installer
     */
    String getName();

    /**
     * Get the version name
     *
     * @return version the version
     */
    String getVersion();

    /**
     * List of installers this installer depends on
     *
     * @return dependencies list of installer names this installer depends on
     */
    String[] getDependencies();

    /**
     * Install the tool into the given JIP home. It is
     * up to the implementation to decide where to put
     * the tool within the home folder
     *
     * @param home the home folder
     */
    public void install(File home);

    /**
     * Check if the tool is installed.
     *
     * @param home the jip home
     * @return installed true if the tool is installed
     */
    public boolean isInstalled(File home);

    /**
     * Get additional environment information defined by this installer
     *
     * @return environment additional environment
     */
    public Map<String, String> getEnvironment(File home);
}
