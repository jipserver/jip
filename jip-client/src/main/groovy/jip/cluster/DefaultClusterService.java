package jip.cluster;

import com.google.inject.Inject;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.Eval;
import groovy.util.GroovyScriptEngine;
import jip.JipConfiguration;
import jip.JipEnvironment;
import jip.jobs.Job;
import jip.plugin.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultClusterService implements ClusterService{
    /**
    * The logger
    */
    private static Logger log = LoggerFactory.getLogger(DefaultClusterService.class);

    /**
     * The plugin registry
     */
    private PluginRegistry pluginRegistry;
    /**
     * The jip environment
     */
    private JipEnvironment environment;
    /**
     * Cluster the cluster map
     */
    private Map<String, Cluster> cluster;
    /**
     * The default cluster
     */
    private String defaultCluster;
    /**
     * The script engine to apply cluster configurations
     */
    private GroovyScriptEngine scriptEngine;
    /**
     * Global configuration script
     */
    private Script globalConfig;

    /**
     * Global configuration script
     */
    private Script userConfig;

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

    @Override
    public void applyConfiguration(Job job, Cluster cluster) {
        log.info("Prepare submission for {}-{} on {}", new Object[]{job.getPipelineId(), job.getId(), cluster.getType()});
        if(scriptEngine == null){
            initializeScriptEngine();
        }
        if(globalConfig != null){
            log.info("Applying global cluster configuration to job");
            globalConfig.getBinding().setProperty("job", job);
            globalConfig.run();
        }
        if(userConfig != null){
            log.info("Applying user cluster configuration to job");
            userConfig.getBinding().setProperty("job", job);
            userConfig.run();
        }
    }

    /**
     * Initialize the script engine that is used to evaluate user/global clsuter configurations
     */
    private void initializeScriptEngine() {
        File global = new File(environment.getJipHome(false), "conf/cluster.groovy");
        File user = new File(environment.getJipHome(true), "conf/cluster.groovy");
        try {
            scriptEngine = new GroovyScriptEngine(new URL[]{
                    environment.getJipHome(false).toURI().toURL(),
                    environment.getJipHome(true).toURI().toURL()
            }, getClass().getClassLoader());
            if(global.exists()){
                globalConfig = scriptEngine.createScript(global.getAbsolutePath(), new Binding());
            }
            if(user.exists()){
                userConfig = scriptEngine.createScript(user.getAbsolutePath(), new Binding());
            }
        }catch (Exception e){
            log.error("Error while evaluating cluster configuration!", e);
        }
    }
}
