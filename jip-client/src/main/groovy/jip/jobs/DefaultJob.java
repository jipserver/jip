package jip.jobs;

import jip.tools.DefaultExecuteEnvironment;
import jip.tools.ExecuteEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultJob implements Job{
    private String id;
    private String remoteId;
    private String log;
    private String errorLog;
    private String toolName;
    private ExecuteEnvironment executeEnvironment;
    private Map<String, String> environment;
    private Map<String, Object> configuration;
    private List<Job> dependenciesBefore;
    private List<Job> dependenciesAfter;
    private int progress;
    private List<Message> messages;
    private JobState state;
    private String stateReason;
    private String workingDirectory;
    private JobStats jobStats;

    public DefaultJob(String id, String workingDirectory) {
        this.id = id;
        this.workingDirectory = workingDirectory;
        this.jobStats = new DefaultJobStats();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getRemoteId() {
        return remoteId;
    }

    @Override
    public String getLog() {
        return log;
    }

    @Override
    public String getErrorLog() {
        return errorLog;
    }

    @Override
    public String getToolName() {
        return toolName;
    }

    @Override
    public ExecuteEnvironment getExecuteEnvironment() {
        if(executeEnvironment == null){
            this.executeEnvironment = new DefaultExecuteEnvironment();
        }
        return executeEnvironment;
    }

    @Override
    public Map<String, String> getEnvironment() {
        if(this.environment == null){
            this.environment = new HashMap<String, String>(System.getenv());
        }
        return environment;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        if(configuration == null){
            this.configuration = new HashMap<String, Object>();
        }
        return configuration;
    }

    @Override
    public List<Job> getDependenciesBefore() {
        if(dependenciesBefore == null){
            this.dependenciesBefore = new ArrayList<Job>();
        }
        return dependenciesBefore;
    }

    @Override
    public List<Job> getDependenciesAfter() {
        if(dependenciesAfter == null){
            this.dependenciesAfter = new ArrayList<Job>();
        }
        return dependenciesAfter;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public List<Message> getMessages() {
        if(this.messages == null){
            this.messages = new ArrayList<Message>();
        }
        return messages;
    }

    @Override
    public JobState getState() {
        return state;
    }

    @Override
    public String getStateReason() {
        return stateReason;
    }

    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public JobStats getJobStats() {
        return jobStats;
    }
}
