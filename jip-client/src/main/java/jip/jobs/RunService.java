package jip.jobs;

import jip.cluster.Cluster;
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
     */
    public void submit(Job job, Cluster cluster);

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
     * @throws Exception in case something went wrong
     */
    public PipelineJob submit(String tool, Map configuration, File directory, String cluster) throws Exception;

    /**
     * Cancel teh given pipeline job
     *
     * @param job the job
     */
    void cancel(PipelineJob job);
}
