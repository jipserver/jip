package jip.jobs;

import jip.tools.ExecuteEnvironment;

import java.util.List;
import java.util.Map;

/**
 * A JIP job references a single job in a pipeline run
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface Job {

    /**
     * Get the pipeline id of this job
     *
     * @return id pipeline id
     */
    public String getPipelineId();

    /**
     * Get the JIP id for this job
     *
     * @return id the jip job id
     */
    public String getId();

    /**
     * Optional remote cluster id of this job
     *
     * @return the remote id
     */
    public String getRemoteId();

    /**
     * Get path to the stdout log file
     *
     * @return log the path to the stdout log file
     */
    public String getLog();

    /**
     * Get path to the stderr log file
     *
     * @return log the path to the stderr log file
     */
    public String getErrorLog();

    /**
     * Get the tool identifier
     *
     * @return toolName the tool name
     */
    public String getToolName();

    /**
     * Get the execute environment for this job
     *
     * @return executeEnvironment the execute environment for this job
     */
    public ExecuteEnvironment getExecuteEnvironment();

    /**
     * Get the job environment
     *
     * @return environment the job environment
     */
    public Map<String, String> getEnvironment();

    /**
     * Get the tool configuration
     *
     * @return configuration the tool configuration
     */
    public Map<String, Object> getConfiguration();

    /**
     * Get list of jobs that this job depends on and that
     * have to be executed before this job
     *
     * @return dependencies list of jobs this job depends on
     */
    public List<Job> getDependenciesBefore();

    /**
     * Get list of jobs that depend on this
     * job and should be executed after this job completed
     *
     * @return dependencies list of jobs this job depends on
     */
    public List<Job> getDependenciesAfter();

    /**
     * Get current progress of this job
     *
     * @return progress current progress of this job or -1 if no progress is available
     */
    public int getProgress();

    /**
     * Get the messages send by this job
     *
     * @return messages the messages send by this job
     */
    public List<Message> getMessages();

    /**
     * Get current state of this job
     *
     * @return state the state of this job
     */
    public JobState getState();

    /**
     * Optional reason for the current state
     *
     * @return reason reason for the current state
     */
    public String getStateReason();

    /**
     * The working directory for this job
     *
     * @return workingDirectory the working directory for this job
     */
    public String getWorkingDirectory();

    /**
     * Get the job stats for this job
     *
     * @return stat the job stats
     */
    public JobStats getJobStats();

    /**
     * Set the path to the log file
     * @param log path to the log file
     */
    public void setLog(String log);

    /**
     * Set the path to the error log file
     * @param log path to the error log file
     */
    public void setErrorLog(String log);

    /**
     * Set the remote id
     *
     * @param remoteId the remote id
     */
    public void setRemoteId(String remoteId);
}
