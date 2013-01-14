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
     * Update the jobs state and save it
     *
     * Also update the jobs dates with respect to the state
     *
     * @param pipelineId the pipeline id
     * @param jobId the job id
     * @param state the job state
     * @param reason optional state reason
     */
    void setState(String pipelineId, String jobId, JobState state, String reason);

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

    /**
     * Save a full job
     * @param job the job
     */
    void save(Job job);

    /**
     * Add a message to a job
     *
     * @param pipelineId the pipeline id
     * @param jobId the job id
     * @param type the message type
     * @param message the message
     */
    void addMessage(String pipelineId, String jobId, MessageType type, String message);

    /**
     * Set job progress
     *
     * @param pipelineId the pipeline id
     * @param jobId the job id
     * @param progress the current progress
     */
    void setProgress(String pipelineId, String jobId, int progress);
}
