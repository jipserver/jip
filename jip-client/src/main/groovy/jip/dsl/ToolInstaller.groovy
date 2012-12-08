package jip.dsl

import jip.tools.Installer
import jip.runner.BasicScriptRunner

/**
 * Groovy based implementation of a
 * Tool installer
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ToolInstaller implements Installer{
    /**
     * The installer name
     */
    String name
    /**
     * The installation procedure
     */
    def exec
    /**
     * The check procedure
     */
    def check

    @Override
    String getName() {
        return name
    }

    @Override
    void install(File home) {
        if(exec instanceof Closure){
            this.exec.call(home)
        }else{
            int r =new BasicScriptRunner(
                    "Install ${name}".toString(),
                    BasicScriptRunner.Interpreter.bash,
                    null,
                    exec,
                    null,
                    ["CWD":home.absolutePath],
                    System.out,
                    System.err
            ).run()
            if (r != 0){
                throw new RuntimeException("Failed installing ${name}")
            }

        }
    }

    @Override
    boolean isInstalled(File home) {
        if(check != null){
            if(check instanceof Closure){
                def b = check.call(home)
                return b instanceof Boolean ? b : b != null
            }else{
                int r = new BasicScriptRunner(
                            "Checking Install ${name}".toString(),
                            BasicScriptRunner.Interpreter.bash,
                            null,
                            check,
                            null,
                            ["CWD":home.absolutePath],
                            System.out,
                            System.err
                ).run()
                return r == 0
            }
        }

        return false
    }

    /**
     * Set the execution as string or closure
     *
     * @param exec the execution
     */
    void exec(def exec){
        this.exec = exec
    }
    /**
     * Set the execution as string or closure
     *
     * @param exec the execution
     */
    void check(def check){
        this.check = check
    }

    /**
     * Ensure all mandatory properties or
     * throw in Exception if something is missing
     *
     */
    void validate() throws Exception{
        if(name == null) throw new NullPointerException("No name specified")
        if(exec == null) throw new NullPointerException("No installation procedure specified")
        if(!(exec instanceof String) && !(exec instanceof Closure)) throw new IllegalArgumentException("Execution has to be a string or a closure")
        if(check != null && (!(check instanceof String) && !(check instanceof Closure))) throw new IllegalArgumentException("Checker has to be a string or a closure")
    }
}
