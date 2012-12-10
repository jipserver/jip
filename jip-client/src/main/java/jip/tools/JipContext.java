package jip.tools;

import java.util.Map;

/**
 * JIP context that manages a set of tools and installers
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface JipContext {
    /**
     * Get map of available installer
     *
     * @return installer the map of available installer
     */
    Map<String, Installer> getInstaller();

    /**
     * Map of registered tools
     *
     * @return tools all tools registered in this context
     */
    Map<String, Tool> getTools();

}
