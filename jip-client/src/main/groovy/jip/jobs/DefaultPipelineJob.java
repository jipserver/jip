package jip.jobs;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Create a new pipeline job
     *
     * @param id the job id
     */
    public DefaultPipelineJob(String id) {
        this(id, null);
    }

    /**
     * Create a new job
     *
     * @param id the id
     * @param name optional name
     */
    public DefaultPipelineJob(String id, String name) {
        if(id == null) throw new NullPointerException("NULL id not permitted");
        this.id = id;
        this.name = name;
    }

    /**
     * Initialize from map
     *
     * @param config the configuration
     */
    public DefaultPipelineJob(Map config){
        this.id = (String) config.get("id");
        this.name = config.containsKey("name") ? (String) config.get("name") : null;


        if(config.containsKey("jobs")){
            List<Map> jobMap = (List<Map>) config.get("jobs");
            Map<String, DefaultJob> id2job = new HashMap<String, DefaultJob>();
            for (Map map : jobMap) {
                DefaultJob jj = new DefaultJob(map);
                id2job.put(jj.getId(), jj);
                getJobs().add(jj);
            }
            for (Map map : jobMap) {
                DefaultJob j = id2job.get(map.get("id"));
                if(map.containsKey("dependenciesBefore")){
                    List<String> deps = (List<String>) map.get("dependenciesBefore");
                    for (String dep : deps) {
                        j.getDependenciesBefore().add(id2job.get(dep));
                    }
                }
                if(map.containsKey("dependenciesAfter")){
                    List<String> deps = (List<String>) map.get("dependenciesAfter");
                    for (String dep : deps) {
                        j.getDependenciesAfter().add(id2job.get(dep));
                    }
                }
            }
        }
    }

    @Override
    public ExecutionGraph getGraph() {
        ExecutionGraph graph = new ExecutionGraph();
        for (Job job : jobs) {
            graph.addVertex(job);
        }
        for (Job job : jobs) {
            for (Job before : job.getDependenciesBefore()) {
                try {
                    graph.addDagEdge(before, job, before.getId());
                } catch (DirectedAcyclicGraph.CycleFoundException e) {
                    throw new RuntimeException("Cycle in graph ?");
                }
            }
        }
        return graph;
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

    public static Map<String, Object> toMap(PipelineJob job){
        HashMap map = new HashMap();
        map.put("id", job.getId());
        map.put("name", job.getName());
        ArrayList joblist = new ArrayList();
        for (Job j : job.getJobs()) {
            joblist.add(DefaultJob.toMap(j));
        }
        map.put("jobs", joblist);
        return map;
    }
}
