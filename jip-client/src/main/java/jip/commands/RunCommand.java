package jip.commands;

import com.google.inject.Inject;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
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
 * The run command executes jobs locally. The command
 * supports two modes, running command line programs and running tools
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Extension
public class RunCommand implements JipCommand{
    /**
     * The logger
     */
    private static final Logger log = LoggerFactory.getLogger(RunCommand.class);
    /**
     * The tool service
     */
    private ToolService toolService;


    @Inject
    public RunCommand(ToolService toolService) {
        this.toolService = toolService;
    }

    @Override
    public String getCommandName() {
        return "run";
    }

    @Override
    public String getShortDescription() {
        return "Run tools";
    }

    @Override
    public String getLongDescription() {
        return "Use this to run jip jobs locally";
    }

    @Override
    public void run(String[] args) {
        JSAP options = null;
        try {
            options = new JSAP();
            options.registerParameter(JSAPHelper.switchParameter("help", 'h').required().help("Show help message").get());
            options.registerParameter(JSAPHelper.flaggedParameter("tool", 't').required().help("Select the tool to run").get());
        } catch (Exception e) {
            log.error("Error while creating options : {}", e.getMessage());
            throw new RuntimeException(e);
        }
        JSAPResult input = options.parse(args);
        if(input.userSpecified("help")){
            JSAPHelper.printCommandError(getCommandName(), options, input);
            System.exit(1);
        }

        String toolName = args.length > 0 ? args[0]: null;
        List<String> rest = null;
        for (int i = 0; i < args.length; i++) {
            if(rest == null){
                if(args[i].equals("--tool") || args[i].equals("-t")){
                    if(args.length > i+1){
                        toolName = args[i+1];
                    }else{
                        break;
                    }
                    i+=1;
                    rest = new ArrayList<String>();
                }
            }else{
                rest.add(args[i]);
            }

        }
        if(toolName == null || rest == null){
            JSAPHelper.printCommandError(getCommandName(), options, input);
            System.exit(1);
        }

        Tool tool = toolService.getTool(toolName);
        if(tool == null){
            log.error("Tool {} not found", toolName);
            throw new IllegalArgumentException("Tool " + toolName + " not found!");
        }

        String[] cmdArgs = rest.toArray(new String[rest.size()]);
        log.debug("Preparing tool: {}", toolName);
        log.debug("Tool arguments : {}", Arrays.toString(cmdArgs));

        Map<String, Object> toolConfiguration = new HashMap<String, Object>();
        try {
            toolConfiguration.putAll(parseToolArguments(tool, cmdArgs));
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing tool parameter!");
        }
        // add run parameter
        toolConfiguration.put("CWD", new File(".").getAbsolutePath());

        // todo : run the tool
        log.info("Running tool " + toolName);
        try {
            tool.run(toolConfiguration);
        } catch (Exception e) {
            log.error("Error while running {} : {}", toolName, e.getMessage());
            throw new RuntimeException("Execution error for " + toolName, e);
        }
    }

    public Map<String, Object> parseToolArguments(Tool tool, String[] args) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("args", args);

        JSAP toolArgParser = new JSAP();
        for (Map.Entry<String, Parameter> entry : tool.getParameter().entrySet()) {
            String name = entry.getKey();
            JSAPHelper.FlaggedParameterBuilder pp = JSAPHelper.flaggedParameter(name);
            if(entry.getValue().isList()){
                pp.list();
            }
            toolArgParser.registerParameter(pp.get());
            if(entry.getValue().getDefaultValue() != null){
                params.put(name, entry.getValue().getDefaultValue());
            }
        }
        JSAPResult parsed = toolArgParser.parse(args);
        for (String parameter : tool.getParameter().keySet()) {
            if(parsed.userSpecified(parameter)){
                Parameter p = tool.getParameter().get(parameter);
                if(p.isList()){
                    params.put(parameter, parsed.getStringArray(parameter));
                }else{
                    params.put(parameter, parsed.getString(parameter));
                }
            }
        }
        return params;
    }
}
