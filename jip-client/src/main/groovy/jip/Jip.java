package jip;

import com.google.common.io.Files;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import groovy.util.ConfigObject;
import jip.commands.JipCommand;
import jip.commands.JipCommandService;
import jip.plugin.PluginBootstrapper;
import jip.plugin.PluginRegistry;
import jip.tools.ToolService;
import jip.utils.SimpleTablePrinter;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.apache.log4j.*;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

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
    private ConfigObject configuration;
    /**
     * The plugin registry
     */
    private PluginRegistry pluginRegistry;

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
    public ConfigObject getConfiguration() {
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
        new Jip().run(args);
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
        String userHome = System.getProperty("user.home") + "/.jip";
        String jipHome = System.getProperty("jip.home");
        if(jipHome != null){
            properties.setProperty("jip.home", jipHome);
        }
        properties.setProperty("jip.user.home", userHome);

        try {
            configureLogger(getConfiguration());
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
        try {
            argparser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argparser.handleError(e);
            System.exit(1);
        }


//        JSAPResult options = jsap.parse(args);
//        if(options.userSpecified("version")){
//            showUsage(jsap);
//            System.exit(1);
//        }
//
//        if(args.length < 1){
//            System.err.println("No command specified");
//            showUsage(jsap);
//        }else{
//            String command = args[0];
//            String[] rest = new String[]{};
//            if(args.length > 1){
//                rest = new String[args.length-1];
//                System.arraycopy(args,1, rest, 0, args.length-1);
//            }
//            JipCommand jipCommand = commandService.get(command);
//            if(jipCommand == null){
//                log.error("Command {} not found !");
//                showUsage(jsap);
//            }
//            jipCommand.run(rest);
//        }
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
        args.addArgument("-v", "--version").action(storeTrue()).help("Show version information");
        return args;
//        args.
//
//
//        JSAP jsap = new JSAP();
//        try {
//
//            jsap.registerParameter(CLIHelper.switchParameter("help", 'h').help("Show the help message").get());
//            jsap.registerParameter(CLIHelper.switchParameter("version", 'v').help("Show version information").get());
//            jsap.registerParameter(CLIHelper.unflaggedParameter("command").help("The JIP command to run").required().get());
//
//            jsap.setUsage("jip ");
//            StringBuilder helpBuilder = new StringBuilder("JIP command line tools can be used to interact with jip.\n" +
//                    "The following commands are supported:\n");
//            helpBuilder.append("\n");
//            SimpleTablePrinter table = new SimpleTablePrinter(Arrays.asList("Command", "Description"));
//            for (JipCommand jipCommand : commandService.getCommands()) {
//                String shortDescription = jipCommand.getShortDescription();
//                if(shortDescription == null) shortDescription = "";
//                table.addRow(jipCommand.getCommandName(), shortDescription);
//            }
//            helpBuilder.append(table);
//            jsap.setHelp(helpBuilder.toString());
//        } catch (JSAPException e) {
//            throw new RuntimeException(e);
//        }
//        return jsap;
    }


    void showVersion(){
        GStringTemplateEngine engine = new GStringTemplateEngine();
        try {
            Template template = engine.createTemplate(getClass().getResource("/cli/version.txt"));
            HashMap binding = new HashMap();
            binding.put("version", "Development");
            URL versionResource = getClass().getResource("jip-client-build.properties");
            if(versionResource != null){
                Properties versionInfo = new Properties();
                versionInfo.load(versionResource.openStream());
                binding.put("version", versionInfo.getProperty("library.version", "Development"));
            }
            template.make(binding).writeTo(new PrintWriter(System.err));
        } catch (Exception e) {
            System.err.println("That is embarrassing, I cant find the version information. Sorry.");
        }

    }
    private void showUsage(JSAP jsap) {
        System.err.println();
        System.err.println(jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp(150));
        System.exit(1);
    }

    static void configureLogger(ConfigObject config) throws IOException {
        Map allFlat = config.flatten();
        Object logFileInConfig = allFlat.get("jip.logging.logfile");

        if(logFileInConfig == null){
            System.err.println("No logfile specified !");
            return;
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
        PatternLayout logLayout = new PatternLayout(allFlat.get("jip.logging.pattern").toString());
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        // get rid of the reflections errors
        //org.apache.log4j.Logger.getLogger(Reflections.class).setLevel(Level.FATAL);

        Map logging = ((ConfigObject)((ConfigObject)config.get("jip")).get("logging")).flatten();
        Properties loggingProperties = new Properties();
        loggingProperties.putAll(logging);

        PropertyConfigurator.configure(loggingProperties);
        rootLogger.removeAllAppenders();
        rootLogger.addAppender(new RollingFileAppender(
                logLayout,
                logfile.getAbsolutePath(),
                true
        ));
        SLF4JBridgeHandler.install();
    }
}
