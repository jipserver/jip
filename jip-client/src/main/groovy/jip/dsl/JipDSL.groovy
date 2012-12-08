package jip.dsl

import com.sun.xml.internal.ws.api.pipe.PipelineAssembler
import jip.graph.Pipeline
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
     * Create a new instance with a new context
     */
    JipDSL() {
        this(new JipDSLContext())
    }

    /**
     * Create a new instance with a given context
     *
     * @param context the context
     */
    JipDSL(JipDSLContext context) {
        this.context = context
    }

    /**
     * Evaluate the given string as a DSL script
     *
     * @param script the script
     * @param args additional arguments bound to the interpreter as args
     */
    void evaluateToolDefinition(String script, Map args){
        ExpandoMetaClass.enableGlobally()
        def binding = new Binding([
                context: context,
                args: args
        ])

        def importCustomizer = new ImportCustomizer()
        importCustomizer.addStaticStars JipDSLContext.class.name

        def config = new CompilerConfiguration()
        config.addCompilationCustomizers importCustomizer

        def shell = new GroovyShell(this.class.classLoader, binding, config)
        evaluateToolDefinition(shell.evaluate("{->\n${script}\n}"))
    }

    /**
     * Evaluate an input stream
     *
     * @param stream the stream
     * @param args the arguments
     */
    void evaluateToolDefinition(InputStream stream, Map args){
        evaluateToolDefinition(stream.text, args)
    }

    /**
     * Evaluate a file
     *
     * @param file the file
     * @param args the arguments
     */
    void evaluateToolDefinition(File file, Map args){
        evaluateToolDefinition(file.newInputStream(), args)
    }

    /**
     * Evaluate a given script closure
     *
     * @param script the script closure
     */
    void evaluateToolDefinition(Closure script){
        script.delegate = context
        script.call()
        context.validate()
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


}
