package jip.dsl

import jip.graph.Pipeline
import jip.graph.PipelineJob
import jip.tools.Parameter
import jip.tools.Tool

/**
 * Context to evaluate pipeline runs
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class JipDSLPipelineContext {
    /**
     * The tool context
     */
    JipDSLContext toolContext
    /**
     * The pipeline that will be created and filled with pipeline jobs
     */
    Pipeline pipeline
    /**
     * The current job id
     */
    int currentJobId = 1
    /**
     * Global properties
     */
    private Map properties

    JipDSLPipelineContext(JipDSLContext toolContext) {
        this(toolContext, [:])
    }

    JipDSLPipelineContext(JipDSLContext toolContext, Map properties) {
        if (properties == null) properties = [:]
        this.properties = properties
        this.toolContext = toolContext
        def mc = new ExpandoMetaClass(JipDSLPipelineContext, false, true)
        mc.initialize()
        this.metaClass = mc
        this.pipeline = new Pipeline()
    }

    def propertyMissing(String name){
        if (this.properties.containsKey(name)){
            return this.properties[name]
        }
        throw new MissingPropertyException(name, JipDSLPipelineContext)
    }

    /**
     * Translates a tool to a pipeline job and returns the pipeline job instance
     * after adding it to the pipeline. The job gets a default id
     * and property access is catched here and translated to variables
     * that can be resolved later by the pipeline
     *
     * @param name
     * @param args
     * @return
     */
    def methodMissing(String name, args) {
        def tool = toolContext.tools[name]
        if (tool != null) {
            // Add new method to class with metaClass.
            def create = { a ->
                def jobid = "${name}-${currentJobId++}"
                if (a && a['id']){
                    jobid = a['id']
                }
                def job = new PipelineJob(jobid, name)
                // add property translation methods
                def mc = new ExpandoMetaClass(PipelineJob, false, true)
                mc.initialize()
                job.metaClass = mc
                job.metaClass."propertyMissing" = {String n ->
                    return "\${${job.id}.${n}}"
                }
                job.metaClass."propertyMissing" = {String n , value->
                    job.configuration[n] = value
                }
                def or = { PipelineJob next ->
                    // delegate defaults parameter
                    String defaultInput = null
                    String defaultOutput = null
                    boolean isList = false
                    for (jip.graph.Parameter next_param : next.getParameters()) {
                        if (next_param.defaultInput){
                            defaultInput = next_param.name
                            isList = next_param.list
                        }
                    }
                    for (jip.graph.Parameter prev_param : job.getParameters()) {
                        if (prev_param.defaultOutput){
                            defaultOutput = prev_param.name
                        }
                    }
                    if (defaultInput && defaultOutput){
                        def simple = "\${${job.id}.${defaultOutput}}"

                        next.configuration[defaultInput] = simple
                        if(isList){next.configuration[defaultInput] = [simple]}

                    }
                    return next
                }
                job.metaClass."or" = or


                if(a != null){
                    for (String key : a.keySet()) {
                        job.configuration[key] = a[key]
                    }
                }

                // translate parameters
                for (Parameter p : tool.parameter.values()) {
                    jip.graph.Parameter pp = new jip.graph.Parameter([
                            name: p.getName(),
                            description: p.getDescription(),
                            list: p.isList(),
                            mandatory: p.isMandatory(),
                            file: p.isFile(),
                            output: p.isOutput(),
                            defaultInput: p.name == tool.defaultInput,
                            defaultOutput: p.name == tool.defaultOutput,
                            defaultValue: p.getDefaultValue(),
                            options: p.getOptions(),
                            type: p.type
                    ])
                    job.getParameters().add(pp)
                }

                // prepare sub piplines
                if(tool.pipeline != null){
                    def subContext = new JipDSLPipelineContext(toolContext, job.getConfiguration())
                    tool.pipeline.delegate = subContext
                    tool.pipeline.setResolveStrategy(Closure.DELEGATE_FIRST)
                    tool.pipeline.call()
                    job.pipeline = subContext.pipeline
                }


                pipeline.executions.add(job)
                return job
            }
            this.metaClass."$name" = create
            def cfg = [:]
            if (args.size() > 0){
                cfg = args[0]
            }
            return create(cfg)
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }

}
