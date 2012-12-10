package jip.dsl

import jip.runner.BasicScriptRunner
import jip.runner.JipExecutor

/**
 * ExecuteUtils can be used as an additional delegate
 * that executes specific tools within a given folder
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ExecuteUtils implements JipExecutor{
    /**
     * The ExecuteUtils working directory
     */
    private File workingDir
    /**
     * Optional environment extension
     */
    private Map<String, String> environmentExtension

    /**
     * Create a new instance of execute utils
     *
     * @param workingDir the working directory
     * @param environmentExtension optional environment extension
     */
    ExecuteUtils(File workingDir, Map<String, String> environmentExtension) {
        this.environmentExtension = environmentExtension
        this.workingDir = workingDir
    }

    /**
     * Run a simple bash command/script
     */
    int bash(String script){
        return run(BasicScriptRunner.Interpreter.bash, script, null)
    }

    /**
     * Run a simple bash command/script
     */
    int bash(String script, Closure closure){
        return run(BasicScriptRunner.Interpreter.bash, script, closure)
    }

    /**
     * Run a simple bash command/script
     */
    int python(String script){
        return run(BasicScriptRunner.Interpreter.python, script, null)
    }

    /**
     * Run a simple bash command/script
     */
    int python(String script, Closure closure){
        return run(BasicScriptRunner.Interpreter.python, script, closure)
    }

    /**
     * Run a simple bash command/script
     */
    int pip(String pipPackage){
        return run(BasicScriptRunner.Interpreter.bash, """
            pip install --install-option="--home="${workingDir.absolutePath}\"" ${pipPackage}
        """, null)
    }

    jip.utils.ProcessBuilder getBuilder(BasicScriptRunner.Interpreter interpreter, String script, Closure definition){
        def builder = new jip.utils.ProcessBuilder(script)
        builder.interpreter(interpreter)
        builder.dir(workingDir.absolutePath)
        if(definition != null){
            definition.delegate = builder
            definition.setResolveStrategy(Closure.OWNER_FIRST)
            definition.call(builder)
        }
        builder.extendEnvironment(environmentExtension)
        return builder
    }
    /**
     * Run a simple bash command/script
     */
    int run(BasicScriptRunner.Interpreter interpreter, String script, Closure definition){
        getBuilder(interpreter, script, definition).get().run()
    }


}
