package jip.cluster;

import jip.jobs.Job;

/**
 * Provides access to cluster implementations
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface ClusterService {

    /**
     * Get the cluster by name
     *
     * @param name the cluster name
     */
    public Cluster getCluster(String name);

    /**
     * Get name of the default cluster
     *
     * @return the default cluster
     */
    public Cluster getDefault();

    /**
     * Apply optional cluster configuration to the given job
     *
     * @param job the job
     * @param cluster the cluster
     */
    public void applyConfiguration(Job job, Cluster cluster);

}
