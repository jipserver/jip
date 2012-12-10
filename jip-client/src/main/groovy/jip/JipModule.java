package jip;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jip.tools.DefaultToolService;
import jip.tools.ToolService;

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
    }

}
