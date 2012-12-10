package jip.utils

import jip.runner.BasicScriptRunner
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Create a BasicScriptRunner
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ProcessBuilder {
    /**
     * The name of this runner
     */
    private final String name;
    /**
     * The interpreter used to run the script
     */
    private BasicScriptRunner.Interpreter interpreter;
    /**
     * The script to be executed
     */
    private String script;
    /**
     * The arguments passed to the script
     */
    private String[] arguments;
    /**
     * The script environment
     */
    private Map<String, String> environment;
    /**
     * The scrip runs standard out
     */
    private OutputStream stdout;
    /**
     * The scrip runs standard error
     */
    private OutputStream stderr;

    /**
     * Additional interpreter options
     */
    private String[] interpreterOptions;

    /**
     * The working directory
     */
    private String workingDir;

    ProcessBuilder(String script) {
        this(script, null)
    }

    /**
     * Create a new Process builder
     * @param name
     */
    ProcessBuilder(String script, String name) {
        this.name = name
        this.stderr = System.err
        this.stdout = System.out
        this.script = script
        this.environment = new HashMap<String, String>()
        this.interpreter = BasicScriptRunner.Interpreter.bash
        this.workingDir = new File("").absolutePath
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            this.environment[e.key] = e.value
        }
    }

    public ProcessBuilder interpreter(BasicScriptRunner.Interpreter interpreter){
        this.interpreter = interpreter
        return this
    }

    public ProcessBuilder interpreter(String interpreter){
        this.interpreter = BasicScriptRunner.Interpreter.valueOf(interpreter)
        return this
    }

    public ProcessBuilder script(String script){
        this.script = script
        return this
    }

    public ProcessBuilder arguments(String...args){
        this.arguments = args;
        return this;
    }

    public ProcessBuilder interpreterArguments(String...args){
        this.interpreterOptions = args;
        return this;
    }

    public ProcessBuilder environment(Map environment){
        this.environment = environment
        return this
    }

    public ProcessBuilder environment(Closure environment){
        def delegate = new EnvironmentDelegate(this.environment)
        environment.delegate = delegate
        environment()
        return this
    }

    public ProcessBuilder dir(String dir){
        this.workingDir = dir
        return this
    }

    BasicScriptRunner get() {
        def r = new BasicScriptRunner(name, interpreter, interpreterOptions, script, arguments, environment, stdout, stderr, workingDir)
        return r;
    }

    void extendEnvironment(Map<String, String> env) {
        if(env != null){
            for (String k : env.keySet()) {
                def v = env.get(k)
                def ov = environment.get(k)
                if (ov == null){
                    environment.put(k, v)
                }else{
                    environment.put(k, "${ov}:${v}".toString())
                }
            }
        }
    }
}
