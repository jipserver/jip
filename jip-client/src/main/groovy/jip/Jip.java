package jip;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import jip.JSAPHelper;
import jip.JipModule;
import jip.commands.JipCommand;
import jip.commands.JipCommandService;
import jip.dsl.ExecuteUtils;
import jip.plugin.PluginBootstrapper;
import jip.plugin.PluginRegistry;
import jip.runner.JipExecutor;
import jip.tools.ToolService;
import jip.utils.SimpleTablePrinter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
     * Get the JIP directory, wither global or user specific
     *
     * @param user user specific directory
     * @return dir the jip dir
     */
    public File getJipHome(boolean user){
        if(!user){
            return new File(System.getProperty("jip.home"));
        }else{
            return new File(System.getProperty("user.home") + "/.jip");
        }
    }

    @Override
    public JipExecutor getExecuteUtilities(File workingDir, List<String> installer) {
        if(installer != null && installer.size() >0){
            Map<String, String> merged = new HashMap<String, String>();
            for (String name : installer) {
                for (Map<String, String> map : toolService.getInstallerEnvironments(name)) {
                    for (String k : map.keySet()) {
                        if(!merged.containsKey(k)){
                            merged.put(k, map.get(k));
                        }else{
                            merged.put(k, merged.get(k) + ":" + map.get(k));
                        }
                    }
                }
            }
            return new ExecuteUtils(workingDir, merged);
        }
        return new ExecuteUtils(workingDir, null);
    }

    /**
     * The JIP main methods delegates to run
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        new Jip().run(args);
    }

    void run(String[] args) {
        Properties properties = new Properties();
        String userHome = System.getProperty("user.home") + "/.jip";
        String jipHome = System.getProperty("jip.home");
        if(jipHome != null){
            properties.setProperty("jip.home", jipHome);
        }
        properties.setProperty("jip.user.home", userHome);

        try {
            configureLogger(new File(userHome));
        } catch (IOException e) {
            log.error("Error while initializing logging system : {}", e.getMessage());
        }

        log.debug("JIP home: {}", jipHome);
        log.debug("JIP user dir: {}", userHome);
        log.debug("Starting plugin system");
        PluginBootstrapper pluginBootstrapper = new PluginBootstrapper(properties, new File(userHome, "jip.cfg"), Arrays.<Module>asList(new JipModule(this)));
        Injector injector = null;
        try {
            injector = pluginBootstrapper.bootstrap(true);
            pluginBootstrapper.startPlugins();
        } catch (Exception e) {
            log.error("Error while starting plugin system", e);
            throw new RuntimeException("Failed to start plugin system", e);
        }

        final PluginRegistry pluginRegistry = injector.getInstance(PluginRegistry.class);
        JipCommandService commandService = injector.getInstance(JipCommandService.class);
        this.toolService = injector.getInstance(ToolService.class);

        JSAP jsap = new JSAP();
        try {
            jsap.registerParameter(JSAPHelper.switchParameter("help", 'h').help("Show the help message").get());
            jsap.registerParameter(JSAPHelper.unflaggedParameter("command").help("The JIP command to run").required().get());
            jsap.setUsage("jip <command> [-h|--help]");
            StringBuilder helpBuilder = new StringBuilder("JIP command line tools can be used to interact with jip.\n" +
                    "The following commands are supported:\n");
            helpBuilder.append("\n");
            SimpleTablePrinter table = new SimpleTablePrinter(Arrays.asList("Command", "Description"));
            for (JipCommand jipCommand : commandService.getCommands()) {
                String shortDescription = jipCommand.getShortDescription();
                if(shortDescription == null) shortDescription = "";
                table.addRow(jipCommand.getCommandName(), shortDescription);
            }
            helpBuilder.append(table);
            jsap.setHelp(helpBuilder.toString());
        } catch (JSAPException e) {
            throw new RuntimeException(e);
        }

        if(args.length < 1){
            log.error("No arguments specified");
            showUsage(jsap);
        }else{
            String command = args[0];
            String[] rest = new String[]{};
            if(args.length > 1){
                rest = new String[args.length-1];
                System.arraycopy(args,1, rest, 0, args.length-1);
            }
            JipCommand jipCommand = commandService.get(command);
            if(jipCommand == null){
                log.error("Command {} not found !");
                showUsage(jsap);
            }
            jipCommand.run(rest);
        }
    }

    private void showUsage(JSAP jsap) {
        System.err.println();
        System.err.println(jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp(150));
        System.exit(1);
    }

    static void configureLogger(File baseDirectory) throws IOException {
        File logDir = new File(baseDirectory, "logs");

        if(!logDir.exists() && !logDir.mkdirs()){
            System.err.println("Unable to create log directory !");
            return;
        }

        if(!logDir.canWrite()){
            System.err.println("Unable to write to  log directory " + logDir.getAbsolutePath());
            return;
        }

        // configure log4j
        PatternLayout logLayout = new PatternLayout("[%-5p] [%t] [%d{dd MMM yyyy HH:mm:ss,SSS}] [%c{2}] : %m%n");
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        // get rid of teh reflections errors
        org.apache.log4j.Logger.getLogger(Reflections.class).setLevel(Level.FATAL);

        rootLogger.removeAllAppenders();
        rootLogger.addAppender(new RollingFileAppender(
                logLayout,
                new File(logDir, "jip.log").getAbsolutePath(),
                true
        ));
        if(System.getProperty("jip.debug") != null){
            ConsoleAppender appender = new ConsoleAppender(new PatternLayout("[%-5p] [%t] [%d{dd MMM yyyy HH:mm:ss,SSS}] [%c{2}] : %m%n"));
            rootLogger.addAppender(appender);
            rootLogger.setLevel(Level.toLevel(System.getProperty("jip.debug")));
        }else{
            rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
        }
        SLF4JBridgeHandler.install();
        rootLogger.setLevel(Level.INFO);
    }
}
