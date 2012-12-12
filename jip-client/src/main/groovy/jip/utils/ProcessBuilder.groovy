package jip.utils

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
     * The script to be executed
     */
    private String script;

    /**
     * The executable to run
     */
    private File executable
    /**
     * The interpreter
     */
    private String interpreter

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

    /**
     * Runtime of the job
     */
    private long runtime = -1;

    ProcessBuilder(File executable) {
        this(null, null)
        this.executable = executable
    }

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
        this.workingDir = new File("").absolutePath
        this.interpreter = "bash"
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            this.environment[e.key] = e.value
        }
    }

    public ProcessBuilder interpreter(String interpreter){
        this.interpreter = interpreter
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

    ProcessBuilder out(OutputStream outputStream) {
        this.stdout = outputStream
        return this
    }

    ProcessBuilder err(OutputStream outputStream) {
        this.stderr = outputStream
        return this
    }


    public int run() {
        List<String> cmd = new ArrayList<String>()
        def file
        def executable = this.executable
        if (script != null){
            file = File.createTempFile("jip", ".script")
            file.deleteOnExit()
            file.write(script)
            executable = file.getAbsolutePath()
        }

        if (interpreter != null && !interpreter.isEmpty()){
            cmd.add(interpreter)
        }
        if (interpreterOptions && interpreterOptions.length > 0){
            cmd.addAll(interpreterOptions)
        }

        cmd.add(executable.toString())

        java.lang.ProcessBuilder pb = new java.lang.ProcessBuilder()
                .directory(new File(workingDir))
                .command(cmd);
        pb.environment().putAll(environment)
        long startTime = System.currentTimeMillis();
        try {
            Process process = pb.start()
            ExecutorService executors = Executors.newFixedThreadPool(2)
            def out = executors.submit(new InputStreamGlobber(process.getInputStream(), stdout))
            def err = executors.submit(new InputStreamGlobber(process.getErrorStream(), stderr))
            out.get()
            err.get()
            executors.shutdownNow()
            return process.exitValue()
        } finally {
            runtime = System.currentTimeMillis() - startTime
            if (file != null) {
                file.delete()
            }
        }
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
    /**
     * Return total runtime of the job or -1
     *
     * @return time runtime
     */
    long getRuntime() {
        return runtime
    }
}
