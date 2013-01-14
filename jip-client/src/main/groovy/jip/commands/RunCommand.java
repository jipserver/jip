package jip.commands;

import com.google.inject.Inject;
import jip.CLIHelper;
import jip.dsl.ExecuteEnvironmentDelegate;
import jip.jobs.PipelineJob;
import jip.jobs.RunService;
import jip.plugin.Extension;
import jip.tools.DefaultExecuteEnvironment;
import jip.tools.Tool;
import jip.tools.ToolService;
import jip.utils.Resources;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

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

    /**
     * The run service
     */
    private RunService runService;

    @Inject
    public RunCommand(ToolService toolService, RunService runService) {
        this.toolService = toolService;
        this.runService = runService;
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
        return Resources.text("/help/commands/run.txt");
    }

    @Override
    public void run(String[] args, Namespace parsed) {

        // pipeline


        if(parsed.get("tool") != null){
            log.info("Running tool {}", parsed.get("tool"));
            File cwd = new File("");
            if(parsed.get("cwd") != null){
                cwd = new File(parsed.getString("cwd"));
            }

            String toolName = parsed.getString("tool");

            if(parsed.get("cluster") == null || ! parsed.getBoolean("cluster")){
                log.debug("Starting local run");
                try {
                    runService.run(toolName, parsed.getAttrs(), cwd);
                } catch (Exception e) {
                    log.error("Error running tool {} : {}", toolName, e.getMessage());
                    throw new RuntimeException(e);
                }
            }else{
                log.debug("Submitting job");
                try {

                    DefaultExecuteEnvironment execEnv = new DefaultExecuteEnvironment();
                    ExecuteEnvironmentDelegate delegate = new ExecuteEnvironmentDelegate(execEnv);
                    delegate.time(parsed.getString("time"));
                    delegate.threads(parsed.getInt("threads"));
                    delegate.memory(parsed.getString("memory"));
                    delegate.priority(parsed.getString("priority"));
                    delegate.queue(parsed.getString("queue"));

                    PipelineJob job = runService.submit(
                            toolName,
                            parsed.getAttrs(),
                            cwd,
                            null,
                            execEnv);// default cluster
                    System.out.println(job.getId() + " submitted");
                } catch (Exception e) {
                    log.error("Error submitting tool {} : {}", toolName, e.getMessage());
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }else{
            // run pipeline
        }
    }

    @Override
    public void populateParser(Subparser parser) {
        parser.addArgument("-s", "--submit").dest("cluster").action(Arguments.storeTrue()).help("Submit pipeline to cluster");
        parser.addArgument("-o", "--output").dest("stdout").help("Job logfile").type(String.class);
        parser.addArgument("-e", "--error").dest("stderr").help("Job error logfile").type(String.class);
        parser.addArgument("-c", "--cpus").setDefault(1).dest("threads").help("Number of threads/cpus assigned to this job").type(Integer.class);
        parser.addArgument("-q", "--queue").dest("queue").help("The cluster queue").type(String.class);
        parser.addArgument("-p", "--priority").dest("priority").help("The cluster priority").type(String.class);
        parser.addArgument("-m", "--max-mem").dest("memory").help("Maximum memory. You can specify in megabytes or use G suffix for gigabytes").type(String.class);
        parser.addArgument("-t", "--time").dest("time").help("Wall clock time in m or hh:mm and more *see help").type(String.class);
        parser.addArgument("-d", "--cwd").dest("cwd").help("Jobs working directory").type(String.class);

        Subparsers toolsCommands = parser.addSubparsers();
        toolsCommands.dest("tool");
        toolsCommands.description("The tool to execute");
        toolsCommands.metavar("tool");
        for (Tool tool : toolService.getTools()) {
            Subparser toolparser = toolsCommands.addParser(tool.getName(), true);
            toolparser.description(tool.getDescription());
            CLIHelper.populateParser(tool, toolparser);
        }
    }
}
