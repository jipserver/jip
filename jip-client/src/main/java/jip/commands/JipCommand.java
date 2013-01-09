package jip.commands;

import jip.plugin.ExtensionPoint;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * CLI command interface extension point to define commands that can be triggered
 * from teh JIP command line
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@ExtensionPoint
public interface JipCommand {
    /**
     * The name of the command
     *
     * @return name the name of the command
     */
    String getCommandName();

    /**
     * Short description of the command
     *
     * @return shortDescription the short description of the command
     */
    String getShortDescription();

    /**
     * Get commands long description
     *
     * @return longDescription the commands long description
     */
    String getLongDescription();

    /**
     * Run the command and its arguments
     *
     * @param args arguments
     * @param parsed parsed name space
     */
    void run(String[] args, Namespace parsed);

    /**
     * Add arguments to the command line parser
     *
     * @param parser the parser
     */
    void populateParser(Subparser parser);
}
