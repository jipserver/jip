package jip.tools

import jip.dsl.JipDSLPipelineContext

/**
 * Default tool implementations
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultTools {

    public static Closure tools = {
        tool("bash"){
            description '''Run bash commands'''
            version '1.0'
            exec '''${args.join(" ")}'''
            option(name:"args", list:true, positional:true)
        }

        tool("pipeline"){
            description '''Dynamically create pipelines'''
            version '1.0'
            option(name:"pipeline", positional:true)
            pipeline {
                Closure run = dsl.evaluate("${it.pipeline}")
                def pipelineContext = new JipDSLPipelineContext(context)
                run.delegate = pipelineContext
                run.setResolveStrategy(Closure.DELEGATE_FIRST)
                run.call(it)
                def pipeline = pipelineContext.pipeline
                return pipeline
            }
        }
    }
}
