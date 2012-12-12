package jip.tools;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import groovy.lang.Closure;
import jip.JipEnvironment;
import jip.dsl.JipDSL;
import jip.dsl.JipDSLContext;

import jip.plugin.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
     * The JIP runtime environment
     */
    private JipEnvironment runtime;

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
     * @param runtime the jip runtime environment
     */
    @Inject
    public DefaultToolService(PluginRegistry pluginRegistry, JipEnvironment runtime) {
        this.pluginRegistry = pluginRegistry;
        this.runtime = runtime;
    }

    /**
     * Get the tool by name or null
     * @param name the name
     * @return tool the tool or null
     */
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
        this.toolContext = new JipDSLContext(runtime);
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
        if(runtime != null){
            String jipHome = runtime.getJipHome(false).getAbsolutePath();
            String userHome = runtime.getJipHome(true).getAbsolutePath();
            JipDSL dsl = new JipDSL(toolContext);
            if(jipHome != null){
                collectTools(dsl, new File(jipHome, "tools"));
            }
            if(userHome != null){
                collectTools(dsl, new File(userHome, "tools"));
            }
        }
    }

    /**
     * Collect tools from the given base directory
     *
     * @param dsl dsl used to evaluate tools
     * @param baseDir the base directory
     */
    protected void collectTools(JipDSL dsl, File baseDir) {
        if(baseDir == null || !baseDir.exists() || baseDir.isFile()) return;
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

    public JipDSLContext loadFrom(File file){
        if(toolContext == null) initialize();
        JipDSL dsl = new JipDSL(toolContext);
        return dsl.evaluateToolDefinition(file, Collections.emptyMap());
    }

    @Override
    public void installDependencies(String toolName, boolean userSpace) {
        if(toolName == null) throw new NullPointerException("NULL tool name not supported");
        Tool tool = getTool(toolName);
        if(tool == null) throw new NullPointerException("Tool " + toolName + " not found!");


        File jipHome = runtime.getJipHome(false);
        File userHome = runtime.getJipHome(true);

        if(tool.getInstaller() != null && tool.getInstaller().size() > 0){
            for (String installerName : tool.getInstaller()) {
                checkAndInstall(installerName, jipHome, userHome, userSpace);
            }
        }
    }

    /**
     * Check the given installer and install it and all its dependencies if it is not installed
     *
     * @param installerName the name of the installer
     * @param jipHome global home
     * @param userHome user home
     * @param userSpace install into user space
     */
    private void checkAndInstall(String installerName, File jipHome, File userHome, boolean userSpace) {
        Installer installer = toolContext.getInstaller().get(installerName);
        if(installer == null){
            throw new RuntimeException("Installer " + installerName + " not found!");
        }
        if(installer.getDependencies() != null && installer.getDependencies().length > 0){
            for (String dependency : installer.getDependencies()) {
                checkAndInstall(dependency, jipHome, userHome, userSpace);
            }
        }
        if(!installer.isInstalled(getInstallerDir(installer, jipHome))){
            // not installed in jip home
            if(!installer.isInstalled(getInstallerDir(installer, userHome))){
                // not installed at all
                log.info("Installing {}", installerName);
                File baseDirectory = userSpace ? userHome : jipHome;
                File installerDir = getInstallerDir(installer, baseDirectory);
                if(!installerDir.exists()){
                    if(!installerDir.mkdirs()){
                        throw new RuntimeException("Unable to create installer directory in " + installerDir.getAbsolutePath());
                    }
                }
                installer.install(installerDir);
            }
        }
    }

    @Override
    public List<Map<String, String>> getInstallerEnvironments() {
        List<Map<String, String>> envs = new ArrayList<Map<String, String>>();
        File jipHome = runtime.getJipHome(false);
        File userHome = runtime.getJipHome(true);

        for (Installer installer : toolContext.getInstaller().values()) {
            Map<String, String> env = getInstallerEnvironment(jipHome, userHome, installer);
            envs.add(env);
        }
        return envs;
    }

    /**
     * Get all the installer environment
     *
     * @param jipHome the jip home
     * @param userHome the user home
     * @param installer the installer
     * @return env the envirnoment
     */
    private Map<String, String> getInstallerEnvironment(File jipHome, File userHome, Installer installer) {
        File jipDir = getInstallerDir(installer, jipHome);
        File home = getInstallerDir(installer, userHome);
        if(installer.isInstalled(jipDir)){
            home = jipDir;
        }
        Map<String, String> env = installer.getEnvironment(home);
        if(env == null){
            env = new HashMap<String, String>();
            // put default bin folder and python path for lib/python
            env.put("PATH", home.getAbsolutePath()+"/bin");
            env.put("PYTHONPATH", home.getAbsolutePath()+"/lib/python");
        }
        return env;
    }

    @Override
    public List<Map<String, String>> getInstallerEnvironments(String name) {
        File jipHome = runtime.getJipHome(false);
        File userHome = runtime.getJipHome(true);
        return getInstallerEnvironments(name, jipHome, userHome, null);
    }

    /**
     * Internal recursive method that collects installer environments following dependencies.
     *
     * @param name the current name
     * @param jipHome global jip hom
     * @param userHome user jip home
     * @param discovered set of already checked installers (initially null)
     * @return list list of installer environments
     */
    private List<Map<String, String>> getInstallerEnvironments(String name, File jipHome, File userHome, Set<String> discovered) {
        Installer installer = toolContext.getInstaller().get(name);
        if(installer == null) throw new NullPointerException("Installer " + name + " not found!");
        List<Map<String, String>> envs = new ArrayList<Map<String, String>>();
        if(discovered == null || !discovered.contains(name)){
            if(discovered == null) discovered = new TreeSet<String>();
            envs.add(getInstallerEnvironment(jipHome, userHome, installer));
            discovered.add(name);
            if(installer.getDependencies() != null && installer.getDependencies().length > 0){
                for (String s : installer.getDependencies()) {
                    envs.addAll(getInstallerEnvironments(s, jipHome, userHome, discovered));
                }
            }
        }
        return envs;
    }

    /**
     * Get the path to an installed tool
     *
     * @param installer the installer
     * @param baseDirectory the base directory
     * @return path path to the tool
     */
    public File getInstallerDir(Installer installer, File baseDirectory){
        String version = installer.getVersion();
        if(version == null || version.isEmpty()) version = "default";
        String installerDirectory = installer.getName() + "-" + version;
        File toolsDir = new File(baseDirectory, "tools");
        return new File(toolsDir, installerDirectory);
    }


}
