package jip.cluster;

import jip.jobs.Job;
import jip.plugin.ExtensionPoint;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Base interface to provide support for different cluster systems
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@ExtensionPoint
public interface Cluster {
    /**
     * Cancel the given jobs
     *
     * @param jobs the jobs
     * @throws Exception in case of an error
     */
    void cancel(List<Job> jobs) throws Exception;

    /**
     * Submits the given list of jobs
     *
     * @param jobs the jobs to be submitted
     * @throws Exception
     */
    void submit(List<Job> jobs) throws Exception;

    /**
     * Submits the given list of jobs
     *
     * @param jobs the jobs to be submitted
     * @throws Exception
     */
    void hold(List<Job> jobs) throws Exception;

    /**
     * Query the grid and returns a map from the clusterId to the current status
     *
     * @return jobs currently queued or running jobs
     * @throws Exception in case the list could not be fetched
     */
    Map<String, ClusterJobState> list() throws Exception;

    /**
     * Get cluster/grid engine identifier to simplify configuration
     *
     * @return identifier
     */
    public String getType();

}
