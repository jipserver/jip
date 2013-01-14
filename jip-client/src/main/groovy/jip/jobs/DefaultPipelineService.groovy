package jip.jobs

import com.google.inject.Inject
import jip.dsl.JipDSL
import jip.dsl.JipDSLContext
import jip.graph.FileParameter
import jip.graph.JobEdge
import jip.graph.JobNode
import jip.graph.Pipeline
import jip.graph.PipelineGraph
import jip.tools.Parameter
import jip.tools.Tool

/**
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultPipelineService implements PipelineService{
    /**
     * The DSL Context to resolve tools
     */
    JipDSLContext context

    /**
     * Provide pipeline ids
     */
    IdService idService

    /**
     * Create a new default pipeline service. The context is used to
     * resolve tools while the id service provides next ids used for the
     * jobs
     *
     * @param context the context
     * @param idService the id service
     */
    @Inject
    DefaultPipelineService(JipDSLContext context, IdService idService) {
        this.context = context
        this.idService = idService
    }

    @Override
    PipelineJob create(String toolName, Map cfg, File cwd) throws Exception {
        if(context == null){
            throw new NullPointerException("No JIP context specified! Unable to evaluate and run pipelines");
        }
        Tool tool = context.getTools().get(toolName)
        if (tool == null){
            throw new NullPointerException("${toolName} tool not found!")
        }

        if(cwd == null) cwd = new File(".");
        // make file paths absolute
        if(cfg != null && cfg.size() > 0){
            cfg = absoluteFileParameter(tool, cwd, cfg)
        }

        Closure pipelineClosure = tool.pipeline
        if(pipelineClosure == null){
            pipelineClosure = {
                "${tool.name}"(cfg)
            }
        }
        return create(toolName, pipelineClosure, cwd, cfg)
    }

    public PipelineJob create(String name, Closure pipelineClosure, File cwd, Map cfg){
        // run pipeline
        Pipeline pipeline = new JipDSL(context).evaluateRun(cfg, pipelineClosure);
        PipelineGraph graph = new PipelineGraph(pipeline);
        graph.prepare();
        graph.reduceDependencies();

        def pipelineRunId = idService.next()
        DefaultPipelineJob job = new DefaultPipelineJob(pipelineRunId, name)
        int counter = 1;
        Map<String, DefaultJob> jobs = [:]

        // create jobs
        for (JobNode node : graph.getNodes()) {
            DefaultJob jobInstance = new DefaultJob(pipelineRunId, "${node.getNodeId()}", cwd.getAbsolutePath())
            jobInstance.setConfiguration(node.getConfiguration())
            jobInstance.setToolName(node.getPipelineJob().getToolId())
            jobs[node.getNodeId()] = jobInstance
            job.getJobs().add(jobInstance)
        }

        // translate edges
        for (JobNode node : graph.getNodes()) {
            def inEdges = graph.getGraph().incomingEdgesOf(node)
            DefaultJob j = jobs[node.getNodeId()]
            for (JobEdge e : inEdges) {
                def source = graph.findNode(e.getSourceNode())
                j.getDependenciesBefore().add(jobs[source.getNodeId()])
            }
            def outEdges = graph.getGraph().outgoingEdgesOf(node)
            for (JobEdge e : outEdges) {
                def target = graph.getGraph().getEdgeTarget(e).getNodeId()
                j.getDependenciesAfter().add(jobs[target])
            }
        }
        return job;
    }

    /**
     * Make all file parameter values absolute with respect
     * to the given directory
     *
     * @param cwd the working directory
     * @param cfg the configuration
     * @return config copy of the configuration with absolute paths
     */
    Map absoluteFileParameter(Tool tool, File cwd, Map cfg) {
        Map copy = new HashMap(cfg);
        for (Parameter parameter : tool.getParameter().values()) {
            if(parameter.isFile() && cfg.containsKey(parameter.getName())){
                if(parameter.isList()){
                    // convert list
                    ArrayList values = new ArrayList();
                    for (Object element : ((Collection)cfg.get(parameter.getName()))) {
                        values.add(toAbsolute(cwd, element.toString()));
                    }
                    copy.put(parameter.getName(), values);
                }else{
                    // single entry
                    copy.put(parameter.getName(), toAbsolute(cwd, copy.get(parameter.getName()).toString()));
                }
            }
        }
        return copy;
    }

    FileParameter toAbsolute(File cwd, String path) {

        if(path.startsWith("/")) return new FileParameter(path);
        return new FileParameter(new File(cwd, path).getAbsolutePath());
    }

}
