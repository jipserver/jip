package jip.jobs

import jip.dsl.JipDSL
import jip.dsl.JipDSLContext
import jip.graph.FileParameter
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
        if(tool.pipeline == null){
            pipelineClosure = {
                "${tool.name}"(cfg)
            }
        }
        return create(toolName, pipelineClosure, cwd)
    }

    public PipelineJob create(String name, Closure pipelineClosure, File cwd){
        // run pipeline
        Pipeline pipeline = new JipDSL(context).evaluateRun(pipelineClosure);
        PipelineGraph graph = new PipelineGraph(pipeline);
        graph.prepare();
        graph.reduceDependencies();

        def pipelineRunId = idService.next()
        DefaultPipelineJob job = new DefaultPipelineJob(pipelineRunId, name)
        int counter = 1;
        for (JobNode node : graph.getNodes()) {
            new DefaultJob("${node.getNodeId()}")
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
