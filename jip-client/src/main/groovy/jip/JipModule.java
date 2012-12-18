package jip;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import groovy.util.ConfigObject;
import jip.jobs.IdService;
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
        bind(IdService.class).toProvider(new Provider<IdService>() {
            @Override
            public IdService get() {
                Object className = cfg(cfg(jip.getConfiguration(), "jobs"), "idservice").get("service");
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
    }

    private ConfigObject cfg(ConfigObject source, String id){
        return (ConfigObject) source.get(id);
    }


}
