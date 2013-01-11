package jip.cluster;

import com.google.inject.Inject;
import jip.JipConfiguration;
import jip.JipEnvironment;
import jip.plugin.PluginRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultClusterService implements ClusterService{

    private PluginRegistry pluginRegistry;

    private JipEnvironment environment;
    /**
     * Cluster the cluster map
     */
    private Map<String, Cluster> cluster;
    /**
     * The default cluster
     */
    private String defaultCluster;

    @Inject
    public DefaultClusterService(PluginRegistry pluginRegistry, JipEnvironment environment) {
        this.pluginRegistry = pluginRegistry;
        this.environment = environment;
    }

    @Override
    public Cluster getCluster(String name) {
        if(cluster == null) initialize();
        return cluster.get(name);
    }

    private void initialize() {
        cluster = new HashMap<String, Cluster>();
        if(pluginRegistry != null && environment != null){
            String name = JipConfiguration.get(environment.getConfiguration(), "cluster", "name").toString();
            String type = JipConfiguration.get(environment.getConfiguration(), "cluster", "type").toString();
            if(!name.isEmpty() && !type.isEmpty()){
                for (Cluster c : pluginRegistry.getInstances(Cluster.class)) {
                    if(c.getType().equals(type)){
                        cluster.put(name, c);
                        c.configure(environment, (Map) JipConfiguration.get(environment.getConfiguration(), "cluster", "configuration"));
                        if(defaultCluster == null){
                            defaultCluster = name;
                        }
                    }
                }
                if(cluster.size() == 0){
                    throw new RuntimeException("Unknown Cluster type " + type);
                }
            }
        }
    }

    @Override
    public Cluster getDefault() {
        if(cluster == null) initialize();
        return getCluster(defaultCluster);
    }
}
