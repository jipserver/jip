package jip.tools;

/**
 *
 * The interface describes the external execution environment
 * of a job. This covers cpus, memory, runtime and various other
 * execution related properties
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface ExecuteEnvironment {
    /**
     * Number of threads available to the job
     *
     * @return threads number of threads available to the job
     */
    public int getThreads();

    /**
     * Maximum RAM in MB requested by the job
     *
     * @return mem max ram in mb requested
     */
    public long getMaxMemory();

    /**
     * Get maximum wall clock time in seconds requested by the job
     *
     * @return time max wall clock time in seconds
     */
    public long getMaxTime();

    /**
     * Set the number of threads used by this job
     *
     * @param threads number of threads
     */
    void setThreads(int threads);

    /**
     * Max memory in MB
     *
     * @param maxMemory memory in MB
     */
    void setMaxMemory(long maxMemory);

    /**
     * Max time in seconds
     *
     * @param maxTime time max time in seconds
     */
    void setMaxTime(long maxTime);

    /**
     * The queue this job is submitted to
     *
     * @return queue the queue
     */
    String getQueue();

    /**
     * Set the queue this job is submitted to
     *
     * @param queue the queue this job is submitted to
     */
    void setQueue(String queue);

    /**
     * The priority for this jobs
     *
     * @return priority the job priority
     */
    String getPriority();

    /**
     * Set the priority this job is submitted to
     *
     * @param priority the jobs priority
     */
    void setPriority(String priority);
}
