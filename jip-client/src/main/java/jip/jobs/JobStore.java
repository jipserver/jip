package jip.jobs;

import jip.plugin.ExtensionPoint;

/**
 * Job store implementations persists pipeline runs and jobs
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@ExtensionPoint
public interface JobStore {
    /**
     * Save the given job. Note that implementations
     * have to provide a way to lock the store if necessary
     *
     * @param pipelineJob the job
     */
    public void save(PipelineJob pipelineJob);

    /**
     * Delete the pipeline and all jobs of the pipeline run
     *
     * @param pipelineJob the job
     */
    public void delete(PipelineJob pipelineJob);

    /**
     * Archive the given pipeline
     *
     * @param pipelineJob the job
     */
    public void archive(PipelineJob pipelineJob);

    /**
     * Find a single job
     * @param id the id
     * @return job the job or null
     */
    public PipelineJob get(String id);

    /**
     * List pipeline jobs stored in this store
     *
     * @param archived if true, archived jobs are listed
     * @return jobs iterable over the jobs
     */
    public Iterable<PipelineJob> list(boolean archived);
}
