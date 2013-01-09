package jip;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.martiansoftware.jsap.JSAP;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import jip.commands.JipCommand;
import jip.commands.JipCommandService;
import jip.plugin.PluginBootstrapper;
import jip.plugin.PluginRegistry;
import jip.tools.ToolService;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import org.apache.log4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static net.sourceforge.argparse4j.impl.Arguments.version;

/**
 * JIP Client main class
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class Jip implements JipEnvironment{
    /**
     * The logger
     */
    private static Logger log = LoggerFactory.getLogger(Jip.class);

    /**
     * The tool service
     */
    private ToolService toolService;

    /**
     * The current configuration
     */
    private Map<String, Object> configuration;
    /**
     * The plugin registry
     */
    private PluginRegistry pluginRegistry;
    /**
     * The global log layout
     */
    private static PatternLayout logLayout;

    /**
     * Get the JIP directory, wither global or user specific
     *
     * @param user user specific directory
     * @return dir the jip dir
     */
    public File getJipHome(boolean user){
        if(!user){
            String property = System.getProperty("jip.home");
            if(property == null){
                property = ".";
            }
            return new File(property);
        }else{
            return new File(System.getProperty("user.home") + "/.jip");
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        if(configuration == null){
            configuration = JipConfiguration.load(getJipHome(false), getJipHome(true));
        }
        return configuration;
    }

    /**
     * The JIP main methods delegates to run
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new Jip().run(args);
        log.debug("Jip finished in {}ms", System.currentTimeMillis() - start);
    }

    /**
     * Access the plugin registry
     * @return
     */
    public PluginRegistry getPluginRegistry() {
        return pluginRegistry;
    }

    /**
     * Run the JIP client
     *
     * @param args the command line arguments
     */
    void run(String[] args) {
        Properties properties = new Properties();
        String userHome = new File(System.getProperty("user.home", ".") + "/.jip").getAbsolutePath();
        String jipHome = new File(System.getProperty("jip.home", "")).getAbsolutePath();
        if (jipHome != null) {
            properties.setProperty("jip.home", jipHome);
        }
        properties.setProperty("jip.user.home", userHome);

        try {
            configureLogger(this, getConfiguration());
        } catch (IOException e) {
            log.error("Error while initializing logging system : {}", e.getMessage());
        }

        log.debug("JIP home: {}", jipHome);
        log.debug("JIP user dir: {}", userHome);
        log.info("Starting plugin system");
        PluginBootstrapper pluginBootstrapper = new PluginBootstrapper(properties, new File(userHome, "jip.cfg"), Arrays.<Module>asList(new JipModule(this)));
        Injector injector = null;
        try {
            injector = pluginBootstrapper.bootstrap(true);
            pluginBootstrapper.startPlugins();
        } catch (Exception e) {
            log.error("Error while starting plugin system", e);
            throw new RuntimeException("Failed to start plugin system", e);
        }

        pluginRegistry = injector.getInstance(PluginRegistry.class);
        JipCommandService commandService = injector.getInstance(JipCommandService.class);
        this.toolService = injector.getInstance(ToolService.class);

        ArgumentParser argparser = createOptions(commandService);
        Namespace parsed = null;
        try {
            parsed = argparser.parseArgs(args);
        } catch (ArgumentParserException e) {
            System.err.println("");
            System.err.println("Error parsing arguments: " + e.getMessage());
            System.err.println("");
            argparser.printHelp(new PrintWriter(System.err));
            return;
        }

        if(parsed.get("help") != null && parsed.getBoolean("help")){
            argparser.printHelp();
            return;
        }

        if(parsed.get("version") != null && parsed.getBoolean("version")){
            createVersionString();
            return;
        }

        if(parsed.get("loglevel") != null && System.getProperty("jip.log.level", "").isEmpty()){
            org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
            rootLogger.addAppender(new ConsoleAppender(logLayout, ConsoleAppender.SYSTEM_ERR));
            rootLogger.setLevel(Level.toLevel(parsed.getString("loglevel")));
            log.debug("Set log level to " + parsed.getString("loglevel"));
        }
        Object command = parsed.get("command");
        if(command != null){
            JipCommand cmd = commandService.get(command.toString());
            if(cmd == null){
                log.error("Command {} not found!", command);
                return;
            }
            cmd.run(args, parsed);
        }


    }

    /**
     * Create the command line options
     *
     *
     * @param commandService the command service to list available commands
     * @return options command line options
     */
    ArgumentParser createOptions(JipCommandService commandService) {
        ArgumentParser args = ArgumentParsers.newArgumentParser("jip", true);
        args.version(createVersionString());
        args.addArgument("-v", "--version").action(version()).help("Show version information");
        args.addArgument("--loglevel").choices("info", "warn", "error", "debug").help("Enable console " +
                "logging and set the log level");

        Subparsers commandParser = args.addSubparsers();
        commandParser.dest("command");
        commandParser.description("JIP commands to run different tasks");
        commandParser.metavar("Commands");

        for (JipCommand jipCommand : commandService.getCommands()) {
            log.debug("Adding sub-command {}", jipCommand.getCommandName());
            Subparser cmdParser = commandParser.addParser(jipCommand.getCommandName());
            cmdParser.description(jipCommand.getShortDescription());
            cmdParser.help(jipCommand.getShortDescription());
            jipCommand.populateParser(cmdParser);
        }
        return args;
    }


    String createVersionString() {

        BufferedReader bufferedReader = null;
        try {
            URL versionResource = getClass().getResource("/jip-client-build.properties");
            Properties versionInfo = new Properties();
            versionInfo.load(versionResource.openStream());
            String version = versionInfo.getProperty("library.version", "unknown");
            URL versionText = getClass().getResource("/cli/version.txt");
            bufferedReader = new BufferedReader(new InputStreamReader(versionText.openStream()));
            StringBuilder b = new StringBuilder();
            String l = null;
            while((l = bufferedReader.readLine()) != null){
                l = l.replaceAll("\\$\\{version\\}", version);
                b.append(l).append("\n");
            }
            return b.toString();
        } catch (Exception e) {
            log.error("Error while preparing version information", e);
            return "That is embarrassing, I cant find the version information. Sorry.";
        } finally {
            if (bufferedReader != null) {
                try {bufferedReader.close();} catch (IOException ignore) {}
            }
        }

    }
    private void showUsage(JSAP jsap) {
        System.err.println();
        System.err.println(jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp(150));
        System.exit(1);
    }

    static void configureLogger(JipEnvironment env, Map<String, Object> config) throws IOException {
        Object logFileInConfig = JipConfiguration.get(config, "jip", "logging", "logfile");

        if(logFileInConfig == null){
            System.err.println("No logfile specified !");
            return;
        }
        if(!logFileInConfig.toString().startsWith("/")){
            logFileInConfig = new File(env.getJipHome(true), logFileInConfig.toString()).getAbsolutePath();
        }
        File logfile = new File(logFileInConfig.toString());

        File logDir = logfile.getParentFile();
        if(!logDir.exists() && !logDir.mkdirs()){
            System.err.println("Unable to create log directory !");
            return;
        }

        if(!logDir.canWrite()){
            System.err.println("Unable to write to  log directory " + logDir.getAbsolutePath());
            return;
        }

        // configure log4j
        logLayout = new PatternLayout(JipConfiguration.get(config, "jip", "logging", "pattern").toString());
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        Map logging = (Map) JipConfiguration.get(config, "jip", "logging");
        Properties loggingProperties = new Properties();
        loggingProperties.putAll(logging);

        PropertyConfigurator.configure(loggingProperties);
        rootLogger.removeAllAppenders();
        rootLogger.addAppender(new RollingFileAppender(
                logLayout,
                logfile.getAbsolutePath(),
                true
        ));

        String loglevel = System.getProperty("jip.log.level", "");
        if(!loglevel.isEmpty()){
            rootLogger.addAppender(new ConsoleAppender(logLayout, ConsoleAppender.SYSTEM_ERR));
            rootLogger.setLevel(Level.toLevel(loglevel));
            log.debug("Set log level to " + loglevel);
        }


        SLF4JBridgeHandler.install();
    }
}
