package jip.commands;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import jip.CLIHelper;
import jip.jobs.*;
import jip.plugin.Extension;
import jip.utils.Resources;
import jip.utils.SimpleTablePrinter;
import jip.utils.Time;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

/**
 * Send job messages and progress information
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Extension
public class MessageCommand implements JipCommand{
    /**
     * The logger
     */
    private static final Logger log = LoggerFactory.getLogger(MessageCommand.class);

    /**
     * The job store
     */
    private JobStore jobStore;

    @Inject
    public MessageCommand(JobStore jobStore) {
        this.jobStore = jobStore;
    }

    @Override
    public String getCommandName() {
        return "message";
    }

    @Override
    public String getShortDescription() {
        return "Let jobs send messages";
    }

    @Override
    public String getLongDescription() {
        return Resources.text("/help/commands/messages.txt");
    }

    @Override
    public void run(String[] args, Namespace parsed) {
        String pipelineId = parsed.getString("pipeline");
        String jobId = parsed.getString("job");

        if(parsed.get("info") != null && parsed.getList("info").size() > 0){
            jobStore.addMessage(pipelineId, jobId, MessageType.Info, Joiner.on(" ").join(parsed.getList("info")));
        }

        if(parsed.get("error") != null && parsed.getList("error").size() > 0){
            jobStore.addMessage(pipelineId, jobId, MessageType.Error, Joiner.on(" ").join(parsed.getList("error")));
        }

        if(parsed.get("warn") != null && parsed.getList("warn").size() > 0){
            jobStore.addMessage(pipelineId, jobId, MessageType.Warn, Joiner.on(" ").join(parsed.getList("warn")));
        }

        if(parsed.get("progress") != null){
            jobStore.setProgress(pipelineId, jobId, parsed.getInt("progress"));
        }
    }


    @Override
    public void populateParser(Subparser parser) {
        parser.addArgument("-j", "--job").dest("job").type(String.class).help("The job id").required(true);
        parser.addArgument("-p", "--pipeline").dest("pipeline").type(String.class).help("The pipeline id").required(true);
        parser.addArgument("--info").dest("info").nargs("+").type(String.class).help("Info message");
        parser.addArgument("--error").dest("error").nargs("+").type(String.class).help("Warning message");
        parser.addArgument("--warn").dest("warn").nargs("+").type(String.class).help("Error message");
        parser.addArgument("--progress").dest("progress").type(Integer.class).help("Progress between 1 and 100");

    }
}
