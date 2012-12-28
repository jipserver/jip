package jip.commands;

import jip.plugin.Extension;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.Arrays;

/**
 * Provision jip home
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Extension
public class ProvisionCommand implements JipCommand{
    @Override
    public String getCommandName() {
        return "provision";
    }

    @Override
    public String getShortDescription() {
        return "Provision the JIP home directory and prepare the environment";
    }

    @Override
    public String getLongDescription() {
        return "This command intitialized the JIP home and prepares the\n" +
                "basic structures needed by JIP. You have to run this command\n" +
                "with write permission to JIP_HOME.";
    }

    @Override
    public void run(String[] args) {
        System.err.println("Running provisioning with " + Arrays.toString(args));
    }

    @Override
    public void populateParser(Subparser parser) {

    }
}
