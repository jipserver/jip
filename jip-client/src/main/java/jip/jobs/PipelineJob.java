package jip.jobs;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

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

    /**
     * Get the execution graph representation
     *
     * @return graph the execution graph
     */
    public ExecutionGraph getGraph();

    class ExecutionGraph extends DirectedAcyclicGraph<Job, String> implements Iterable<Job>{

        public ExecutionGraph() {
            super(String.class);
        }

    }

}
