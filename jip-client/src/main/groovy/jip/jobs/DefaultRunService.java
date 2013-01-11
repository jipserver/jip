package jip.jobs;

import com.google.inject.Inject;
import jip.cluster.Cluster;
import jip.cluster.ClusterService;
import jip.tools.Tool;
import jip.tools.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultRunService implements RunService{
    /**
    * The logger
    */
    private static Logger log = LoggerFactory.getLogger(DefaultRunService.class);
    /**
     * The pipeline service
     */
    private PipelineService pipelineService;

    /**
     * The tool service
     */
    private ToolService toolService;

    /**
     * The cluster service
     */
    private ClusterService clusterService;
    /**
     * The job store
     */
    private JobStore jobStore;

    @Inject
    public DefaultRunService(ToolService toolService, PipelineService pipelineService, ClusterService clusterService, JobStore jobStore) {
        this.toolService = toolService;
        this.pipelineService = pipelineService;
        this.clusterService = clusterService;
        this.jobStore = jobStore;
    }

    @Override
    public void execute(Job job, boolean updateInStore) throws Exception {
        log.info("Running job " + job.getId() + " with tool " + job.getToolName() + " in pipeline " + job.getPipelineId());
        Tool jobTool = toolService.getTool(job.getToolName());
        if(updateInStore){
            jobStore.setState(job.getPipelineId(), job.getId(), JobState.Running, null);
        }
        try {
            jobTool.run(new File(job.getWorkingDirectory()), job.getConfiguration());
        } catch (Exception e) {
            log.error("Job execution for {}-{} failed : {}", new Object[]{job.getPipelineId(), job.getId(), e.getMessage()});
            jobStore.setState(job.getPipelineId(), job.getId(), JobState.Failed, e.getMessage());
        }
    }

    @Override
    public void run(String tool, Map configuration, File directory) throws Exception {
        log.info("Creating pipeline graph");
        PipelineJob pipelineJob = pipelineService.create(tool, configuration, directory);
        log.info("Pipeline with {} jobs created", pipelineJob.getJobs().size());
        PipelineJob.ExecutionGraph graph = pipelineJob.getGraph();
        // iterates in topological order
        for (Job job : graph) {
            execute(job, false);
        }
    }

    @Override
    public void submit(String tool, Map configuration, File directory, String clusterName) throws Exception {
        Cluster cluster = null;
        if(clusterName == null){
            cluster = clusterService.getDefault();
        }else{
            cluster = clusterService.getCluster(clusterName);
        }
        if(cluster == null){
            throw new RuntimeException("Cluster " + clusterName + " not found!");
        }

        log.info("Creating pipeline graph");
        PipelineJob pipelineJob = pipelineService.create(tool, configuration, directory);
        log.info("Pipeline with {} jobs created", pipelineJob.getJobs().size());


        log.info("Saving pipeline job {}", pipelineJob.getId());
        for (Job job : pipelineJob.getJobs()) {
            job.setState(JobState.Submitted);
        }
        jobStore.save(pipelineJob);

        PipelineJob.ExecutionGraph graph = pipelineJob.getGraph();
        for (Job job : graph) {
            submit(job, cluster);
        }
    }

    public void submit(Job job, Cluster cluster) {
        try {
            log.info("Submitting {}-{}", job.getPipelineId(), job.getId());
            cluster.submit(job);
            jobStore.setState(job.getPipelineId(), job.getId(), JobState.Queued, null);
        } catch (Exception e) {
            log.error("Error submitting job : " + e.getMessage(), e);
            throw new RuntimeException("Unable to submit job : " + e.getMessage(), e);
        }
    }
}
