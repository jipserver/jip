package jip.jobs;

import jip.tools.Tool;

import java.io.File;
import java.util.Map;

/**
 * Implementations of the pipeline service are responsible
 * for transforming a a tool call into a PipelineJob and
 * perform validation on the execution graph.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface PipelineService {

    /**
     * Transform the given tool into a pipeline job and
     * perform validation on the execution graph.
     *
     * @param tool the tool
     * @param workingDir the working directory
     * @return pipeline job
     * @throws Exception in case the tool can not be transformed
     */
    PipelineJob create(String tool, Map configuration, File workingDir) throws Exception;
}
