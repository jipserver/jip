package jip.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jip.plugin.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provide access to commands
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Singleton
public class JipCommandService {
    /**
     * The logger
     */
    private static Logger log = LoggerFactory.getLogger(JipCommandService.class);
    /**
     * The commands
     */
    private Map<String, JipCommand> commands;

    /**
     * Create a new Command service
     *
     * @param registry the plugin registry
     */
    @Inject
    public JipCommandService(PluginRegistry registry) {
        this.commands = new HashMap<String, JipCommand>();
        for (JipCommand jipCommand : registry.getInstances(JipCommand.class)) {
            if(commands.containsKey(jipCommand.getCommandName())){
                log.error("Duplicated command name {}, skipping second entry!", jipCommand.getCommandName());
            }else{
                commands.put(jipCommand.getCommandName(), jipCommand);
            }
        }
        log.debug("Registered {} commands", commands.size());
    }

    /**
     * Get the command with the given name or null
     *
     * @param name the command name
     * @return command the command or null
     */
    public JipCommand get(String name){
        return commands.get(name);
    }

    /**
     * Get a collection of all registered commands
     *
     * @return commands the registered commands
     */
    public Collection<JipCommand> getCommands(){
        return Collections.unmodifiableCollection(commands.values());
    }

}
