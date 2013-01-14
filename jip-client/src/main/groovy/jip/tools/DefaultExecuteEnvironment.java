package jip.tools;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * The job queue
     */
    private String queue;

    /**
     * The job queue
     */
    private String priority;

    public DefaultExecuteEnvironment() {
        this.threads = 1;
        this.maxMemory = 0;
        this.maxTime = 0;
    }

    public DefaultExecuteEnvironment(Map config) {
        this();
        if(config.containsKey("threads")) this.threads = ((Number)config.get("threads")).intValue();
        if(config.containsKey("maxMemory")) this.maxMemory = ((Number)config.get("maxMemory")).longValue();
        if(config.containsKey("maxTime")) this.maxTime = ((Number)config.get("maxTime")).longValue();
        if(config.containsKey("queue")) this.queue = (String)config.get("queue");
        if(config.containsKey("priority")) this.priority = (String)config.get("priority");
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

    @Override
    public void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    @Override
    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    @Override
    public String getQueue() {
        return queue;
    }

    @Override
    public void setQueue(String queue) {
        this.queue = queue;
    }

    @Override
    public String getPriority() {
        return priority;
    }

    @Override
    public void setPriority(String priority) {
        this.priority = priority;
    }

    public static Map toMap(ExecuteEnvironment env){
        HashMap map = new HashMap();
        map.put("threads", env.getThreads());
        map.put("maxMemory", env.getMaxMemory());
        map.put("maxTime", env.getMaxTime());
        map.put("queue", env.getQueue());
        map.put("priority", env.getPriority());
        return map;
    }
}
