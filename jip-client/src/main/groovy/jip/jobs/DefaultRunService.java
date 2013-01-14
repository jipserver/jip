package jip.jobs;

import com.google.inject.Inject;
import jip.JipEnvironment;
import jip.cluster.Cluster;
import jip.cluster.ClusterJobState;
import jip.cluster.ClusterService;
import jip.tools.ExecuteEnvironment;
import jip.tools.Tool;
import jip.tools.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
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

    /**
     * The jip runtime environment
     */
    private JipEnvironment environment;

    @Inject
    public DefaultRunService(ToolService toolService, PipelineService pipelineService, ClusterService clusterService, JobStore jobStore, JipEnvironment environment) {
        this.toolService = toolService;
        this.pipelineService = pipelineService;
        this.clusterService = clusterService;
        this.jobStore = jobStore;
        this.environment = environment;
    }

    @Override
    public void execute(Job job, boolean updateInStore) throws Exception {
        log.info("Running job " + job.getId() + " with tool " + job.getToolName() + " in pipeline " + job.getPipelineId());
        Tool jobTool = toolService.getTool(job.getToolName());
        if(updateInStore){
            jobStore.setState(job.getPipelineId(), job.getId(), JobState.Running, null);
        }
        try {
            jobTool.run(new File(job.getWorkingDirectory()), job.getConfiguration(), job);
            jobStore.setState(job.getPipelineId(), job.getId(), JobState.Done, null);
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
    public PipelineJob submit(String tool, Map configuration, File directory, String clusterName, ExecuteEnvironment executeEnvironment) throws Exception {
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

        if(executeEnvironment != null){
            log.info("updating execution environment for jobs");
            for (Job job : pipelineJob.getJobs()) {
                ExecuteEnvironment je = job.getExecuteEnvironment();
                if(executeEnvironment.getThreads() > 1){
                    je.setThreads(executeEnvironment.getThreads());
                }
                if(executeEnvironment.getMaxMemory() > 0){
                    je.setMaxMemory(executeEnvironment.getMaxMemory());
                }
                if(executeEnvironment.getMaxTime() > 0){
                    je.setMaxTime(executeEnvironment.getMaxTime());
                }
                if(executeEnvironment.getPriority() != null){
                    je.setPriority(executeEnvironment.getPriority());
                }
                if(executeEnvironment.getQueue() != null){
                    je.setQueue(executeEnvironment.getQueue());
                }
            }
        }

        log.info("Saving pipeline job {}", pipelineJob.getId());
        for (Job job : pipelineJob.getJobs()) {
            job.setState(JobState.Submitted);
        }
        jobStore.save(pipelineJob);

        PipelineJob.ExecutionGraph graph = pipelineJob.getGraph();
        for (Job job : graph) {
            submit(job, cluster);
        }
        return pipelineJob;
    }

    public void submit(Job job, Cluster cluster) {
        try {
            clusterService.applyConfiguration(job, cluster);
            log.info("Submitting {}-{}", job.getPipelineId(), job.getId());
            cluster.submit(job);
            // save job
            jobStore.save(job);
            jobStore.setState(job.getPipelineId(), job.getId(), JobState.Queued, null);
        } catch (Exception e) {
            log.error("Error submitting job : " + e.getMessage(), e);
            throw new RuntimeException("Unable to submit job : " + e.getMessage(), e);
        }
    }

    @Override
    public void cancel(PipelineJob job) {
        log.info("Canceling {}", job.getId());
        for (Job j : job.getGraph()) {
            if(!j.getState().isDoneState()){
                try {
                    clusterService.getDefault().cancel(Arrays.asList(j));
                } catch (Exception e) {
                    log.warn("Error while canceling " + j.getId(), e);
                }
                jobStore.setState(j.getPipelineId(), j.getId(), JobState.Canceled, null);
            }
        }
    }

    @Override
    public void checkJobs() {
        log.info("Checking job status");
        Cluster cluster = clusterService.getDefault();
        try {
            Map<String,ClusterJobState> states = cluster.list();
            log.debug("Job states : {}", states);
            for (PipelineJob pipelineJob : jobStore.list(false)) {
                for (Job job : pipelineJob.getJobs()) {
                    if(!job.getState().isDoneState()){
                        log.info("Checking state for {}-{}", pipelineJob.getId(), job.getId());
                        if(!states.containsKey(job.getRemoteId()) || !states.get(job.getRemoteId()).isExecutionState()){
                            log.info("Updating state for {}-{}", pipelineJob.getId(), job.getId());
                            // job is not running any more
                            if(!states.containsKey(job.getRemoteId())){
                                // out of list, assume failed!
                                jobStore.setState(job.getPipelineId(), job.getId(), JobState.Failed, "");
                            }else{
                                switch (states.get(job.getRemoteId())){
                                    case Canceled:
                                        jobStore.setState(job.getPipelineId(), job.getId(), JobState.Canceled, "");
                                        break;
                                    case Done:
                                        jobStore.setState(job.getPipelineId(), job.getId(), JobState.Done, "");
                                        break;
                                    case Error:
                                        jobStore.setState(job.getPipelineId(), job.getId(), JobState.Failed, "");
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed checking job status on cluster", e);
        }
    }
}
