package jip;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import groovy.util.ConfigObject;
import jip.cluster.ClusterService;
import jip.cluster.DefaultClusterService;
import jip.dsl.JipDSLContext;
import jip.jobs.*;
import jip.plugin.PluginRegistry;
import jip.tools.DefaultToolService;
import jip.tools.ToolService;

import java.util.Set;

/**
 * Jip default bindings
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class JipModule extends AbstractModule{
    /**
     * The JIP instance
     */
    private Jip jip;

    public JipModule(Jip jip) {
        this.jip = jip;
    }

    @Override
    protected void configure() {
        bind(JipEnvironment.class).toInstance(jip);
        bind(ToolService.class).to(DefaultToolService.class).in(Scopes.SINGLETON);
        bind(RunService.class).to(DefaultRunService.class).in(Scopes.SINGLETON);
        bind(PipelineService.class).to(DefaultPipelineService.class).in(Scopes.SINGLETON);
        bind(ClusterService.class).to(DefaultClusterService.class).in(Scopes.SINGLETON);
        bind(JipDSLContext.class).toProvider(new Provider<JipDSLContext>() {
            @Inject
            JipEnvironment runtime;
            @Override
            public JipDSLContext get() {
                return new JipDSLContext(runtime);
            }
        }).in(Scopes.SINGLETON);
        bind(IdService.class).toProvider(new Provider<IdService>() {
            @Override
            public IdService get() {
                Object className = jip.getConfiguration().get("jobs.idservice.service");
                if (className == null) {
                    className = "jip.jobs.FileIdService";
                }
                Set<IdService> services = jip.getPluginRegistry().getInstances(IdService.class);
                for (IdService service : services) {
                    if(service.getClass().getName().equals(className.toString())){
                        return service;
                    }
                }
                return null;
            }
        }).in(Scopes.SINGLETON);

        bind(JobStore.class).toProvider(new Provider<JobStore>() {
            @Inject
            Provider<PluginRegistry> pluginRegistry;

            @Override
            public JobStore get() {
                String storeClassName = (String) JipConfiguration.get(jip.getConfiguration(), "storage", "store");
                PluginRegistry registry = pluginRegistry.get();
                for (PluginRegistry.PluginExtension pluginExtension : registry.getExtensions(JobStore.class)) {
                    if (pluginExtension.getClazz().getName().equals(storeClassName)) {
                        return (JobStore) registry.getInstance(pluginExtension);
                    }
                }
                throw new RuntimeException("No implementation found for job store!");
            }
        }).in(Scopes.SINGLETON);
    }

    private ConfigObject cfg(ConfigObject source, String id){
        return (ConfigObject) source.get(id);
    }


}
