package jip.tools;

import java.util.Collection;

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
}
