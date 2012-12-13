package jip.utils

import com.sun.tools.example.debug.bdi.MethodNotFoundException
import jip.tools.Tool

import java.lang.ProcessBuilder

/**
 * A delegate that provides helpers to run scripts
 * with various interpreters.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ExecuteDelegate {

    Boolean failOnError
    private File workingDir
    private Tool tool
    private Map toolConfig

    /**
     * Create a new delegate with current working directory as
     * working dir and throwing an exception if the
     * execution exists with non 0 value.
     */
    ExecuteDelegate() {
        this(true)
    }

    /**
     * Create a new delegate with current working directory as
     * working dir.
     *
     * @param failOnError throw an exception if run exists with non 0
     */
    ExecuteDelegate(Boolean failOnError) {
        this(new File("."), failOnError)
    }

    /**
     * Create a new execute delegate that uses the given working directory
     *
     * @param workingDir working directory for the execution
     * @param failOnError throw an exception if run exists with non 0
     */
    ExecuteDelegate(File workingDir, Boolean failOnError) {
        this.workingDir = workingDir
        this.failOnError = failOnError

    }

    def methodMissing(String name, def args) {
        if (args == null || args.size() == 0) throw new MethodNotFoundException(name + " not found");
        Map cfg = [:]
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Map) cfg.putAll(args[i])
            else{
                cfg.script = args[i].toString()
            }
        }
        return run(name, cfg)
    }

    def run(String interpreter, Map cfg){
        def exec = toFile(cfg['exec'])
        def script = cfg['script']
        def name = cfg['name']
        def args = cfg['args']
        def interpreterArgs = cfg['interpreterArgs']
        def cwd = toFile(cfg['cwd'])
        if(!cwd){
            cwd = this.workingDir
        }
        def env = cfg['env']
        def pb = null
        if (exec){
            pb = new jip.utils.ProcessBuilder(exec)
        }else{
            if(tool != null){
                if (toolConfig == null){
                    toolConfig = [:]
                }
                script = Templates.fillTemplate(tool, script.toString(), toolConfig)
            }
            pb = new jip.utils.ProcessBuilder(script.toString())
        }
        pb.name(name)
        pb.dir(cwd)
        pb.extendEnvironment(env)
        pb.arguments(args)
        pb.interpreter(interpreter)
        pb.interpreterArguments(interpreterArgs)
        pb.out(cfg['out'] ? cfg.out : System.out)
        pb.err(cfg['err'] ? cfg.err : System.err)
        def exitValue = pb.run()
        if (exitValue != 0 && failOnError){
            throw new RuntimeException("Execution failed with exit value ${exitValue}")
        }
        return exitValue
    }

    private File toFile(arg){
        if (!arg) return null
        if (arg instanceof File) return arg
        if (arg instanceof String) return new File(arg)
        throw new IllegalArgumentException("Can not convert "+ arg + " with type " + arg.getClass() + " to file!")
    }

    /**
     * Make this execute delegate aware of the underlying tool and
     * its configuration so that templates can be filled accordingly.
     *
     * @param tool the underlying tool
     * @param toolConfig the configuration
     */
    void setTemplateConfiguration(Tool tool, Map toolConfig) {
        this.toolConfig = toolConfig
        this.tool = tool
    }
}
