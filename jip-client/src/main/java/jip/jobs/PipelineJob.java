package jip.jobs;

import java.util.List;

/**
 * A pipeline job wraps around a set of jobs with dependencies
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface PipelineJob {

    /**
     * Get the pipeline id
     *
     * @return id the pipeline id
     */
    public String getId();

    /**
     * (Optional) name of this pipeline job
     *
     * @return name optional name of this job
     */
    public String getName();

    /**
     * Get the list of jobs for this pipeline
     *
     * @return jobs the list of jobs associated with this pipeline
     */
    public List<Job> getJobs();

}
