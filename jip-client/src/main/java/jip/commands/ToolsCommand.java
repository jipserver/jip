package jip.commands;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import jip.CLIHelper;
import jip.JipEnvironment;
import jip.plugin.Extension;
import jip.tools.JipContext;
import jip.tools.Tool;
import jip.tools.ToolService;
import jip.utils.SimpleTablePrinter;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * List, show, and install tools
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Extension
public class ToolsCommand implements JipCommand{
    /**
     * The logger
     */
    private static final Logger log = LoggerFactory.getLogger(ToolsCommand.class);
    /**
     * The JIP runtime environment
     */
    private JipEnvironment runtime;
    /**
     * The tool service
     */
    private ToolService toolService;


    @Inject
    public ToolsCommand(JipEnvironment runtime, ToolService toolService) {
        this.runtime = runtime;
        this.toolService = toolService;
    }

    @Override
    public String getCommandName() {
        return "tools";
    }

    @Override
    public String getShortDescription() {
        return "List/show/install tools";
    }

    @Override
    public String getLongDescription() {
        return "Managed installed tools";
    }

    @Override
    public void populateParser(Subparser parser) {
        parser.addArgument("--user").help("Install into user home").action(Arguments.storeTrue());
        parser.addArgument("-s", "--show").help("Show selected tool").dest("tool").metavar("tool").type(String.class);
        parser.addArgument("-i", "--install").help("Run selected installer").dest("installer").metavar("installer").type(String.class);
    }

    @Override
    public void run(String[] args, Namespace parsed) {
        if(parsed.get("tool") != null){

        }else{
            // list tools as a default action
            listTools();
        }

//        if(input.userSpecified("install")){
//            installTool(input.getString("install"), input.userSpecified("user"));
//        }else if(input.userSpecified("show")){
//            showTool(input.getString("show"));
//        }else{
//            listTools();
//        }
    }

    /**
     * List available tools
     */
    protected void listTools() {
        log.debug("Preparing tools table");
        ArrayList<Tool> tools = new ArrayList<Tool>(toolService.getTools());
        Collections.sort(tools, new Comparator<Tool>() {
            @Override
            public int compare(Tool tool, Tool tool2) {
                return tool.getName().compareTo(tool2.getName());
            }
        });
        log.debug("Found {} tools", tools.size());
        System.out.println("");
        SimpleTablePrinter tooltable = new SimpleTablePrinter(
                Arrays.asList("Name", "Description", "Version"),
                true, true, true);
        for (Tool tool : tools) {
            tooltable.addRow(tool.getName(), tool.getDescription(), tool.getVersion());
        }
        System.out.println(tooltable);
        System.out.println("You can get more information about a tool");
        System.out.println("using the show command, for example");
        System.out.println("");
        System.out.println("  jip tools -s bash");
        System.out.println("");

    }

    /**
     * Print tool information to console
     *
     * @param tool the name of the tool
     */
    protected void showTool(String tool) {

    }

    /**
     * Install tool from URL
     *
     * @param toolUrl the url to the tool
     * @param installToUser install tool into user space
     */
    protected void installTool(String toolUrl, boolean installToUser) {
        log.info("Trying to install tool from " + toolUrl);
        if(toolUrl.startsWith("/") || new File(toolUrl).exists()){
            File sourceFile = new File(toolUrl);
            File toolsDir = new File(runtime.getJipHome(installToUser), "tools");
            File target = new File(toolsDir, sourceFile.getName());
            if(!target.getParentFile().exists()){
                if(!target.getParentFile().mkdirs()){
                    log.error("Unable to create tools folder {}", target.getParentFile());
                    throw new RuntimeException("Unable to create tools folder "+ target.getParentFile());
                }
            }
            log.info("Install tool from {} to {}", toolUrl, target.getAbsolutePath());
            try {
                Files.copy(sourceFile, target);
            } catch (IOException e) {
                log.error("Error while copying tool file : {}", e.getMessage());
                throw new RuntimeException("Unable to copy tool file : " + e.getMessage());
            }
            JipContext loaded = toolService.loadFrom(target);
            log.info("Loaded " + loaded.getTools().size() + " Tools");
            log.info("Loaded " + loaded.getInstaller().size() + " Installer");
            for (Map.Entry<String, Tool> toolEntry : loaded.getTools().entrySet()) {
                Tool tool = toolService.getTool(toolEntry.getKey());
                if(tool.getInstaller() != null && tool.getInstaller().size() > 0){
                    log.info("Installing dependencies");
                    toolService.installDependencies(tool.getName(), installToUser);
                }
            }
        }
    }


}
