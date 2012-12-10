package jip.commands;

import com.google.inject.Inject;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import jip.JSAPHelper;
import jip.plugin.Extension;
import jip.tools.Parameter;
import jip.tools.Tool;
import jip.tools.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
     * The tool service
     */
    private ToolService toolService;


    @Inject
    public ToolsCommand(ToolService toolService) {
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
            options.registerParameter(JSAPHelper.switchParameter("help", 'h').required().help("Show help message").get());
            options.registerParameter(JSAPHelper.flaggedParameter("show", 's').help("Show selected tools").get());
            options.registerParameter(JSAPHelper.flaggedParameter("install", 'i').valueName("url").help("Install selected tool").get());
            options.registerParameter(JSAPHelper.switchParameter("user").help("Install into user home").get());
        } catch (Exception e) {
            log.error("Error while creating options : {}", e.getMessage());
            throw new RuntimeException(e);
        }
        JSAPResult input = options.parse(args);
        if(input.userSpecified("help") || !input.success()){
            JSAPHelper.printCommandError(getCommandName(), options, input);
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
        throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
    }


}
