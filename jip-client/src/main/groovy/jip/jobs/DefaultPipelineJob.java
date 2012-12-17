package jip.jobs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultPipelineJob implements PipelineJob{
    /**
     * The Pipeline ID
     */
    private String id;
    /**
     * The name
     */
    private String name;
    /**
     * The jobs
     */
    private List<Job> jobs;

    public DefaultPipelineJob(String id) {
        this(id, null);
    }

    public DefaultPipelineJob(String id, String name) {
        if(id == null) throw new NullPointerException("NULL id not permitted");
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Job> getJobs() {
        if(this.jobs == null){
            this.jobs = new ArrayList<Job>();
        }
        return this.jobs;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}
