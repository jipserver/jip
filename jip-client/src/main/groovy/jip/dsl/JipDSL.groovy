package jip.dsl

import com.sun.xml.internal.ws.api.pipe.PipelineAssembler
import jip.JipEnvironment
import jip.graph.Pipeline
import jip.tools.Tool
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * JIP Tools and Pipelines domain language
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class JipDSL {

    /**
     * the current context
     */
    JipDSLContext context

    /**
     * The runtime
     */
    JipEnvironment jipRuntime

    /**
     * Create a new instance with a new context
     */
    JipDSL() {
        this(new JipDSLContext(null))
    }

    /**
     * Create a new instance with a given context
     *
     * @param context the context
     */
    JipDSL(JipDSLContext context) {
        this.context = context
        this.jipRuntime = context.jipRuntime
        this.context.dsl = this
    }

    /**
     * Evaluate the given string as a DSL script
     *
     * @param script the script
     * @param args additional arguments bound to the interpreter as args
     */
    JipDSLContext evaluateToolDefinition(String script, Map args){
        ExpandoMetaClass.enableGlobally()
        GroovyShell shell = createShell(args)
        return evaluateToolDefinition(shell.evaluate("{->\n${script}\n}"))
    }

    Closure evaluate(String script){
        return createShell().evaluate("{it->\n${script}\n}") as Closure
    }

    private GroovyShell createShell(Map args) {
        def binding = new Binding([
                context: context,
                args: args,
                dsl: this
        ])

        def importCustomizer = new ImportCustomizer()
        importCustomizer.addStaticStars JipDSLContext.class.name

        def config = new CompilerConfiguration()
        config.addCompilationCustomizers importCustomizer

        def shell = new GroovyShell(this.class.classLoader, binding, config)
        shell
    }

    /**
     * Evaluate an input stream
     *
     * @param stream the stream
     * @param args the arguments
     */
    JipDSLContext evaluateToolDefinition(InputStream stream, Map args){
        return evaluateToolDefinition(stream.text, args)
    }

    /**
     * Evaluate a file
     *
     * @param file the file
     * @param args the arguments
     */
    JipDSLContext evaluateToolDefinition(File file, Map args){
        return evaluateToolDefinition(file.newInputStream(), args)
    }

    /**
     * Evaluate a given script closure
     *
     * @param script the script closure
     */
    JipDSLContext evaluateToolDefinition(Closure script){
        JipDSLContext privateContext = new JipDSLContext(jipRuntime)
        privateContext.dsl = this
        script.delegate = privateContext
        script.call()
        context.installer.putAll(privateContext.installer)
        context.tools.putAll(privateContext.tools)
        return privateContext
    }
    /**
     * Evaluate a pipeline run closure
     *
     * @param run the run closure
     * @return pipeline the pipeline
     */
    Pipeline evaluateRun(Closure run){
        def pipelineContext = new JipDSLPipelineContext(context)
        run.delegate = pipelineContext
        run.setResolveStrategy(Closure.DELEGATE_FIRST)
        run.call()
        return pipelineContext.pipeline
    }

    /**
     * Evaluate a pipeline run closure in tool context
     *
     * @param run the run closure
     * @return pipeline the pipeline
     */
    Pipeline evaluateRun(Map config, Closure run){
        def pipelineContext = new JipDSLPipelineContext(context)
        run.delegate = pipelineContext
        run.setResolveStrategy(Closure.DELEGATE_FIRST)
        def ret = run.call(config)
        if(ret instanceof Pipeline) return ret
        return pipelineContext.pipeline
    }


}
