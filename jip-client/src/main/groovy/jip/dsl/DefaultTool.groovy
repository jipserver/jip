package jip.dsl

import jip.tools.Tool
import jip.runner.BasicScriptRunner
import jip.tools.Parameter
import groovy.text.GStringTemplateEngine

/**
 * Basic tool implementation
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultTool implements Tool{
    /**
     * The tool name
     */
    private String name
    /**
     * The tool description
     */
    private String description = ""
    /**
     * The interpreter
     */
    private def interpreter
    /**
     * Executor, can be script or closure
     */
    private def exec
    /**
     * Pipeline closure
     */
    private def pipeline
    /**
     * The parameter map
     */
    private Map<String, Parameter> parameter = [:]
    /**
     * Default input parameter
     */
    private String defaultInput
    /**
     * Default output parameter
     */
    private String defaultOutput
    /**
     * Interpreter arguments template
     */
    private String args


    @Override
    String getName() {
        return name
    }

    @Override
    String getDescription() {
        return description
    }

    @Override
    void run(Map cfg) throws Exception{
        if(exec instanceof Closure){
            exec.call(cfg)
        }else{
            String script = createScript(cfg)
            String[] commandArgs = null
            if (args){
                commandArgs = fillTemplate(args, cfg).split("\\s+")
            }
            int r = new BasicScriptRunner(
                    name,
                    this.interpreter,
                    null,
                    script,
                    commandArgs,
                    cfg,
                    System.out,
                    System.err
            ).run()



        }
    }
    /**
     * Helper method to fill exec template
     *
     * @param cfg the configuration
     * @return script the filled script
     */
    protected String createScript(Map cfg) {
        String script = exec
        // fill template
        return fillTemplate(script, cfg)
    }

    /**
     * Fill a GString template with the values from cfg
     *
     * @param script the script template
     * @param cfg the binding
     * @return filled the filled template
     */
    public String fillTemplate(String script, Map cfg) {
        GStringTemplateEngine templateEngine = new GStringTemplateEngine()
        def template = templateEngine.createTemplate(script)
        for (String p : parameter.keySet()) {
            if (!cfg.containsKey(p)) {
                def defaultValue = parameter[p].defaultValue
                cfg[p] = defaultValue ? defaultValue : ""
            }
        }
        def writer = new StringWriter()
        template.make(cfg).writeTo(writer)
        return writer.toString()
    }

    @Override
    Map<String, Parameter> getParameter() {
        return parameter
    }

    @Override
    String getDefaultInput() {
        return defaultInput
    }

    @Override
    String getDefaultOutput() {
        return defaultOutput
    }
/****** DSL HELPERS *************/
    void description(String description){
        this.description = description
    }

    void exec(String exec){
        this.exec = exec
        if(this.interpreter == null){
            this.interpreter = BasicScriptRunner.Interpreter.bash
        }
    }

    void exec(Closure exec){
        this.exec = exec
    }

    void interpreter(String interpreter){
        this.interpreter = BasicScriptRunner.Interpreter.valueOf(interpreter)
    }

    void interpreter(BasicScriptRunner.Interpreter interpreter){
        this.interpreter = interpreter
    }

    DefaultParameter input(String name){
        return input([name:name], null)
    }

    DefaultParameter input(String name, Closure closure){
        return input([name:name], closure)
    }

    DefaultParameter input(Map cfg){
        return input(cfg, null)
    }

    DefaultParameter input(Map cfg, Closure closure){
        return parameter(cfg, true, false, closure)
    }

    DefaultParameter output(String name){
        return output([name:name], null)
    }

    DefaultParameter output(String name, Closure closure){
        return output([name:name], closure)
    }

    DefaultParameter output(Map cfg){
        return output(cfg, null)
    }

    DefaultParameter output(Map cfg, Closure closure){
        return parameter(cfg, false, true, closure)
    }

    DefaultParameter option(String name){
        return option([name:name], null)
    }

    DefaultParameter option(String name, Closure closure){
        return option([name:name], closure)
    }

    DefaultParameter option(Map cfg){
        return option(cfg, null)
    }

    DefaultParameter option(Map cfg, Closure closure){
        return parameter(cfg, false, false, closure)
    }

    DefaultParameter parameter(Map cfg, boolean input, boolean output, Closure closure){
        def p = new DefaultParameter(cfg)
        if(p.name == null){
            throw new NullPointerException("No parameter name specified !")
        }
        p.input = input
        p.output = output
        if(input || output)
            p.file = true
        if(p.defaultValue != null && p.defaultValue instanceof Collection){
            p.list = true
        }
        if(closure != null){
            closure.delegate = p
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call()
        }
        this.parameter[p.name] = p

        // set defaults
        if(input && (defaultInput == null || p.defaultInput)){
            defaultInput = p.name
        }
        if(output && (defaultOutput == null || p.defaultOutput)){
            defaultOutput = p.name
        }
        return p
    }
    /**
     * Add a pipeline definition closure
     *
     * @param definition the pipeline closure
     */
    void pipeline(Closure definition){
        this.pipeline = definition
    }
    /**
     * Specify the argument
     *
     * @param args the argument
     */
    void args(String args){
        this.args = args
    }
}
