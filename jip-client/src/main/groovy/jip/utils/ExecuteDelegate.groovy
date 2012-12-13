package jip.utils

import com.sun.tools.example.debug.bdi.MethodNotFoundException

import java.lang.ProcessBuilder

/**
 * A delegate that provides helpers to run scripts
 * with various interpreters
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ExecuteDelegate {

    Boolean failOnError

    ExecuteDelegate() {
        this(true)
    }
    ExecuteDelegate(Boolean failOnError) {
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
        def env = cfg['env']
        def pb = null
        if (exec){
            pb = new jip.utils.ProcessBuilder(exec)
        }else{
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
}
