package jip.commands;

import com.google.inject.Inject;
import jip.CLIHelper;
import jip.jobs.Job;
import jip.jobs.JobStore;
import jip.jobs.PipelineJob;
import jip.jobs.RunService;
import jip.plugin.Extension;
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

/**
 * Internal command that executes jobs submitted on the cluster
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Extension
public class ExecuteCommand implements JipCommand{
    /**
     * The logger
     */
    private static final Logger log = LoggerFactory.getLogger(ExecuteCommand.class);

    /**
     * The run service
     */
    private RunService runService;

    /**
     * The job store
     */
    private JobStore jobStore;

    @Inject
    public ExecuteCommand(RunService runService, JobStore jobStore) {
        this.runService = runService;
        this.jobStore = jobStore;
    }

    @Override
    public String getCommandName() {
        return "execute";
    }

    @Override
    public String getShortDescription() {
        return "Execute stored jobs";
    }

    @Override
    public String getLongDescription() {
        return Resources.text("/help/commands/execute.txt");
    }

    @Override
    public void run(String[] args, Namespace parsed) {
        if(parsed.get("pipeline") == null) throw new NullPointerException("No pipeline ID specified");
        if(parsed.get("job") == null) throw new NullPointerException("No job ID specified");
        String pipelineJobId = parsed.getString("pipeline");
        String jobId = parsed.getString("job");

        log.info("Executing {}-{}", pipelineJobId, jobId);

        PipelineJob pipelineJob = jobStore.get(pipelineJobId);
        if(pipelineJob == null){
            throw new RuntimeException("Pipeline " + pipelineJobId + " not found !");
        }
        log.debug("Loaded pipeline from store : {}", pipelineJob);

        boolean found = false;
        for (Job job : pipelineJob.getJobs()) {
            if(job.getId().equals(jobId)){
                try {
                    runService.execute(job, true);
                } catch (Exception e) {
                    log.error("Error while executing job {}", jobId, e);
                    throw new RuntimeException(e);
                }
                found = true;
                break;
            }
        }
        if(!found){
            throw new RuntimeException("Job " + jobId + " not found !");
        }
    }

    @Override
    public void populateParser(Subparser parser) {
        parser.addArgument("-p", "--pipeline").dest("pipeline").required(true).type(String.class).help("The pipeline id");
        parser.addArgument("-j", "--job").dest("job").required(true).type(String.class).help("The job id");
    }
}
