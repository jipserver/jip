package jip.tools;

import java.io.File;

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
}
