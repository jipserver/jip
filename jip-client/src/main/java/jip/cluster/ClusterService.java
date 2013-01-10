package jip.cluster;

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
}
