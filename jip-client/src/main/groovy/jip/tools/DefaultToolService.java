package jip.tools;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import groovy.lang.Closure;
import jip.dsl.JipDSLContext;
import jip.tools.DefaultTools;
import jip.plugin.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Singleton
public class DefaultToolService implements ToolService {

    /**
     * The logger
     */
    private static Logger log = LoggerFactory.getLogger(DefaultToolService.class);
    /**
     * The plugin registry
     */
    private PluginRegistry pluginRegistry;

    /**
     * The tool context
     */
    private JipDSLContext toolContext;

    /**
     * Creates a new instance of the tools service. The
     * instance is not initialized yet. Initialization
     * will happen lazily on first access.
     *
     * @param pluginRegistry the plugin registry to search for tools
     */
    @Inject
    public DefaultToolService(PluginRegistry pluginRegistry) {
        this.pluginRegistry = pluginRegistry;

    }

    @Override
    public Tool getTool(String name) {
        if(toolContext == null) initialize();
        return toolContext.getTools().get(name);
    }

    /**
     * Load tools from different locations. This checks:
     * <pre>
     *     1. JIP_HOME
     *     2. Users .jip folder
     * </pre>
     *
     */
    protected synchronized void initialize() {
        if(toolContext != null) return;
        this.toolContext = new JipDSLContext();
        log.info("Initializing tools");

        log.info("Loading default tools");
        Closure defaultTools = DefaultTools.tools;
        defaultTools.setDelegate(toolContext);
        defaultTools.call();

        if(pluginRegistry != null){
            log.info("Searching for tools implemented in plugins");
            Set<Tool> pluginTools = pluginRegistry.getInstances(Tool.class);
            for (Tool pluginTool : pluginTools) {
                register(pluginTool);
            }
        }

    }

    @Override
    public Collection<Tool> getTools() {
        if(toolContext == null) initialize();
        return Collections.unmodifiableCollection(toolContext.getTools().values());
    }

    @Override
    public void register(Tool tool) {
        if(tool == null) throw new NullPointerException("NULL tool can not be registered");
        if(toolContext == null) initialize();
        String name = tool.getName();
        if(toolContext.getTools().containsKey(name)){
            log.error("Tool {} already registered", name);
            throw new IllegalArgumentException("Tool '"+name+"' is already registered");
        }
        log.info("Register tool {}", name);
        toolContext.getTools().put(name, tool);
    }
}
