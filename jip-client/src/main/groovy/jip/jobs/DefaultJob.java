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

    /**
     * Initialize from map
     *
     * @param config the configuration
     */
    public DefaultJob(Map config) {
        this.id = (String) config.get("id");
        this.workingDirectory = (String) config.get("workingDirectory");
        if(config.containsKey("remoteId")) this.remoteId = (String) config.get("remoteId");
        if(config.containsKey("log")) this.log = (String) config.get("log");
        if(config.containsKey("errorLog")) this.errorLog = (String) config.get("errorLog");
        if(config.containsKey("toolName")) this.toolName = (String) config.get("toolName");
        if(config.containsKey("stateReason")) this.stateReason = (String) config.get("stateReason");
        if(config.containsKey("state")) this.state = JobState.valueOf((String) config.get("state"));
        if(config.containsKey("progress")) this.progress = ((Number) config.get("progress")).intValue();
        if(config.containsKey("jobStats")) this.jobStats = new DefaultJobStats((Map) config.get("jobStats"));
        if(config.containsKey("messages")){
            List<Map> msgMap = (List<Map>) config.get("messages");
            for (Map map : msgMap) {
                getMessages().add(new DefaultMessage(map));
            }
        }
        if(config.containsKey("environment"))this.environment = (Map<String, String>) config.get("environment");
        if(config.containsKey("configuration"))this.configuration = (Map<String, Object>) config.get("configuration");
        if(config.containsKey("executeEnvironment"))this.executeEnvironment = new DefaultExecuteEnvironment((Map) config.get("executeEnvironment"));
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

    public void setId(String id) {
        this.id = id;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public void setExecuteEnvironment(ExecuteEnvironment executeEnvironment) {
        this.executeEnvironment = executeEnvironment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public void setDependenciesBefore(List<Job> dependenciesBefore) {
        this.dependenciesBefore = dependenciesBefore;
    }

    public void setDependenciesAfter(List<Job> dependenciesAfter) {
        this.dependenciesAfter = dependenciesAfter;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public void setStateReason(String stateReason) {
        this.stateReason = stateReason;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void setJobStats(JobStats jobStats) {
        this.jobStats = jobStats;
    }


    public static Map toMap(Job job){
        HashMap map = new HashMap();
        map.put("id", job.getId());
        map.put("workingDirectory", job.getWorkingDirectory());
        map.put("toolName", job.getToolName());
        map.put("stateReason", job.getStateReason());
        map.put("state", job.getState() == null ? null : job.getState().toString());
        map.put("progress", job.getProgress());

        ArrayList messageList = new ArrayList();
        for (Message message : job.getMessages()) {
            messageList.add(DefaultMessage.toMap(message));
        }
        map.put("messages", messageList);
        if(job.getJobStats() != null) map.put("jobStats", DefaultJobStats.toMap(job.getJobStats()));
        if(job.getExecuteEnvironment() != null) map.put("executeEnvironment", DefaultExecuteEnvironment.toMap(job.getExecuteEnvironment()));
        map.put("errorLog", job.getErrorLog());
        map.put("log", job.getLog());
        map.put("environment", job.getEnvironment());
        map.put("configuration", job.getConfiguration());
        map.put("dependenciesAfter", toDependencyList(job.getDependenciesAfter()));
        map.put("dependenciesBefore", toDependencyList(job.getDependenciesBefore()));
        return map;
    }

    private static List<String> toDependencyList(List<Job> dependencies) {
        ArrayList<String> list = new ArrayList<String>();
        for (Job dependency : dependencies) {
            list.add(dependency.getId());
        }
        return list;
    }

}
