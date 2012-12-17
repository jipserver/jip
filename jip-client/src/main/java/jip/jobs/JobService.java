package jip.jobs;

import jip.graph.Pipeline;
import jip.tools.Tool;

import java.util.Map;

/**
 *
 * Job communication
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface JobService {

    /**
     * Create a pipeline job from a pipeline or throw an exception
     * if the pipeline is not configured properly
     *
     * @param pipeline the source pipeline
     * @return pipelineJob the pipeline job
     */
    public PipelineJob create(Pipeline pipeline);

}
