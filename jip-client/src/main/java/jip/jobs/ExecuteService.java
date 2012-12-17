package jip.jobs;

import java.io.File;

/**
 *
 * Implementations of the execute service are supposed to execute and
 * run a job.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface ExecuteService {

    /**
     * Run a single job in the given working directory.
     * The method blocks until the job is finished and
     * throws an exception if the execution was not successful.
     *
     * @param job run a specific jb of the pipeline
     * @param workingDir the working directory for the pipeline
     * @throws Exception in case the execution failed
     */
    public void run(Job job,  File workingDir) throws Exception;

}
