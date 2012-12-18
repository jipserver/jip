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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

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
    public void run(String[] args) {
        JSAP options = null;
        try {
            options = new JSAP();
            options.registerParameter(CLIHelper.switchParameter("help", 'h').required().help("Show help message").get());
            options.registerParameter(CLIHelper.flaggedParameter("show", 's').help("Show selected tools").get());
            options.registerParameter(CLIHelper.flaggedParameter("install", 'i').valueName("url").help("Install selected tool").get());
            options.registerParameter(CLIHelper.switchParameter("user").help("Install into user home").get());
        } catch (Exception e) {
            log.error("Error while creating options : {}", e.getMessage());
            throw new RuntimeException(e);
        }
        JSAPResult input = options.parse(args);
        if(input.userSpecified("help") || !input.success()){
            CLIHelper.printCommandError(getCommandName(), options, input);
            System.exit(1);
        }

        if(input.userSpecified("install")){
            installTool(input.getString("install"), input.userSpecified("user"));
        }else if(input.userSpecified("show")){
            showTool(input.getString("show"));
        }else{
            listTools();
        }
    }

    /**
     * List available tools
     */
    protected void listTools() {
        ArrayList<Tool> tools = new ArrayList<Tool>(toolService.getTools());
        Collections.sort(tools, new Comparator<Tool>() {
            @Override
            public int compare(Tool tool, Tool tool2) {
                return tool.getName().compareTo(tool2.getName());
            }
        });
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
