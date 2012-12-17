package jip.tools;

/**
 *
 * Default implementation of an execute environment
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultExecuteEnvironment implements ExecuteEnvironment{

    /**
     * Number of requested threads
     */
    private int threads;

    /**
     * Number of maximum memory
     */
    private long maxMemory;

    /**
     * Max time requested by the job
     */
    private long maxTime;

    public DefaultExecuteEnvironment() {
        this.threads = 1;
        this.maxMemory = 0;
        this.maxTime = 0;
    }

    @Override
    public int getThreads() {
        return threads;
    }

    @Override
    public long getMaxMemory() {
        return maxMemory;
    }

    @Override
    public long getMaxTime() {
        return maxTime;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }
}
