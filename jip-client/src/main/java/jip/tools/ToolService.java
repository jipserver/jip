package jip.tools;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * Implementations of the tool service allow
 * provide access to tool implementations
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface ToolService {
    /**
     * Get tool by name
     *
     * @param name the name
     * @return tool the tool or null if the tool does not exists
     */
    Tool getTool(String name);

    /**
     * Return a collection of all registered tools
     *
     * @return tools the registered tools
     */
    Collection<Tool> getTools();

    /**
     * Register a tool
     *
     * @param tool the new tool
     * @throws IllegalArgumentException in case a tool with that name already exists
     */
    void register(Tool tool);

    /**
     * Load tools and installer from file
     *
     * @param file the file
     * @return context single context with only the loaded to tools and installers
     */
    JipContext loadFrom(File file);

    /**
     * Install all installer dependencies for this tool
     *
     * @param toolName the tool name
     * @param userSpace install into user space
     */
    void installDependencies(String toolName, boolean userSpace);

    /**
     * Install the given isntaller into the specified
     * directory
     *
     * @param installer the installer
     * @param dir the directory
     */
    void install(Installer installer, File dir);

    /**
     * Returns a list of all available installer environments
     */
    List<Map<String,String>> getInstallerEnvironments();

    /**
     * Returns a list the specified installer environment and all its dependencies
     */
    List<Map<String,String>> getInstallerEnvironments(String name);
}
