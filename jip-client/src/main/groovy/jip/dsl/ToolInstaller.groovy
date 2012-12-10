package jip.dsl

import jip.JipEnvironment
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
     * The tools version
     */
    String version = "default"

    /**
     * The installation procedure
     */
    def exec

    /**
     * The check procedure
     */
    def check

    /**
     * List of dependencies
     */
    def dependsOn = []

    /**
     * Additional environment
     */
    def environment
    /**
     * The runtime environment
     */
    JipEnvironment runtime

    @Override
    String getName() {
        return name
    }

    /**
     * Get the version name
     *
     * @return version the version name
     */
    @Override
    String getVersion(){
        return version
    }
    /**
     * Get the dependencies
     *
     * @return dependencies the dependencies
     */
    @Override
    String[] getDependencies() {
        return dependsOn
    }

    @Override
    void install(File home) {
        if(exec instanceof Closure){
            this.exec.delegate = runtime.getExecuteUtilities(home, [name])
            this.exec.call(home)
        }else{
            def executeUtils = runtime.getExecuteUtilities(home, [name])
            if (executeUtils.bash(exec.toString()) != 0){
                throw new RuntimeException("Failed installing ${name}")
            }
        }
    }

    @Override
    boolean isInstalled(File home) {
        if(check != null){
            if(check instanceof Closure){
                check.delegate = runtime.getExecuteUtilities(home,null)
                def b = check.call(home)
                return b instanceof Boolean ? b : b != null
            }else{
                def executeUtils = runtime.getExecuteUtilities(home, null)
                return executeUtils.bash(check.toString()) == 0
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
     * Set dependencies
     *
     * @param dependsOn
     */
    void dependsOn(def dependsOn){
        this.dependsOn = dependsOn
    }
    /**
     * Set the installer version
     *
     * @param version the version
     */
    void version(String version){
        this.version = version
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

    @Override
    Map<String, String> getEnvironment(File home) {
        if (environment instanceof Closure){
            return environment.call(home)
        }
        return environment
    }
}
