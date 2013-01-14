package jip.jobs;

import jip.cluster.Cluster;
import jip.tools.ExecuteEnvironment;
import jip.tools.Tool;

import java.io.File;
import java.util.Map;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface RunService {
    /**
     * Execute a single pipeline job
     *
     * @param job the pipeline job
     * @param updateJobStore if true job information in the job store is updated
     * @throws Exception
     */
    public void execute(Job job, boolean updateJobStore) throws Exception;

    /**
     * Submit a single job to a cluster
     *
     * @param job the job
     * @param cluster the cluster
     * @throws Exception in case the job could not be submitted
     */
    public void submit(Job job, Cluster cluster) throws Exception;

    /**
     * Run a tool
     *
     * @param tool the tool
     * @param configuration the tool configuration
     * @param directory the tool directory
     * @throws Exception in case something went wrong
     */
    public void run(String tool, Map configuration, File directory) throws Exception;

    /**
     * Run a tool on a cluster
     *
     * @param tool the tool
     * @param configuration the tool configuration
     * @param directory the tool directory
     * @param cluster the cluster name
     * @param executeEnvironment the execute environment
     * @throws Exception in case something went wrong
     */
    public PipelineJob submit(String tool, Map configuration, File directory, String cluster, ExecuteEnvironment executeEnvironment) throws Exception;

    /**
     * Cancel teh given pipeline job
     *
     * @param job the job
     */
    void cancel(PipelineJob job);

    /**
     * Check job status
     */
    void checkJobs();
}
