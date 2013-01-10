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

    @Inject
    public DefaultRunService(ToolService toolService, PipelineService pipelineService, ClusterService clusterService) {
        this.toolService = toolService;
        this.pipelineService = pipelineService;
        this.clusterService = clusterService;
    }

    @Override
    public void execute(Job job) throws Exception {
        log.info("Running job " + job.getId() + " with tool " + job.getToolName());
        Tool jobTool = toolService.getTool(job.getToolName());
        jobTool.run(new File(job.getWorkingDirectory()), job.getConfiguration());
    }

    @Override
    public void run(String tool, Map configuration, File directory) throws Exception {
        log.info("Creating pipeline graph");
        PipelineJob pipelineJob = pipelineService.create(tool, configuration, directory);
        log.info("Pipeline with {} jobs created", pipelineJob.getJobs().size());
        PipelineJob.ExecutionGraph graph = pipelineJob.getGraph();
        // iterates in topological order
        for (Job job : graph) {
            execute(job);
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
        PipelineJob.ExecutionGraph graph = pipelineJob.getGraph();
        for (Job job : graph) {
            submit(job, cluster);
        }
    }

    public void submit(Job job, Cluster cluster) {
        try {
            cluster.submit(job);
        } catch (Exception e) {
            log.error("Error submitting job : " + e.getMessage(), e);
            throw new RuntimeException("Unable to submit job : " + e.getMessage(), e);
        }
    }
}
