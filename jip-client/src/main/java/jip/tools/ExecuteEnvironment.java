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
}
