package jip.tools;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import groovy.lang.Closure;
import jip.dsl.JipDSL;
import jip.dsl.JipDSLContext;
import jip.tools.DefaultTools;
import jip.plugin.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 *
 * Default tool service implementation
 *
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

        // search from folders
        // 1. $JIP_HOME/tools/*/jip-tool.groovy
        // 2. $JIP_HOME/tools/*.groovy
        // 4. $USER_HOME/tools/*/jip-tool.groovy
        // 5. $USER_HOME/tools/*.groovy
        String jipHome = System.getProperty("jip.home");
        String userHome = System.getProperty("user.home") + "/.jip";
        JipDSL dsl = new JipDSL(toolContext);
        if(jipHome != null){
            collectTools(dsl, new File(jipHome, "tools"));
        }
        if(userHome != null){
            collectTools(dsl, new File(userHome, "tools"));
        }
    }

    /**
     * Collect tools from the given base directory
     *
     * @param dsl dsl used to evaluate tools
     * @param baseDir the base directory
     */
    protected void collectTools(JipDSL dsl, File baseDir) {
        if(baseDir == null || !baseDir.exists() || !baseDir.isFile()) return;
        log.info("Collecting tools from {}", baseDir.getAbsolutePath());
        File[] files = baseDir.listFiles();
        if(files == null) return;
        for (File file : files) {
            if(file.getName().endsWith(".groovy")){
                log.info("Loading tools from {}", file.getAbsolutePath());
                dsl.evaluateToolDefinition(file, Collections.emptyMap());
                if(file.isDirectory()){
                    File jiptoolsFile = new File(file, "jip-tool.groovy");
                    if(jiptoolsFile.exists()){
                        log.info("Loading tools from {}", jiptoolsFile.getAbsolutePath());
                        dsl.evaluateToolDefinition(jiptoolsFile, Collections.emptyMap());
                    }
                }
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
