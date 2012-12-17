package jip.tools;

import groovy.lang.Closure;
import jip.utils.ExecuteDelegate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Default installer implementation
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultInstaller implements Installer{
    /**
     * The name
     */
    private String name;
    /**
     * The version
     */
    private String version;
    /**
     * Installer dependencies
     */
    private String[] dependencies;

    /**
     * The install closure
     */
    private Closure closure;

    /**
     * The install check
     */
    private Closure check;
    /**
     * Additional environment
     */
    private Map<String, String> environemnt;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String[] getDependencies() {
        return dependencies;
    }

    @Override
    public void install(File home) {
        closure.setDelegate( new ExecuteDelegate(home, true));
        closure.call(home);
    }

    @Override
    public boolean isInstalled(File home) {
        if(check != null){
            check.setDelegate(new ExecuteDelegate(home, false));
            Object result = check.call(home);
            return result != null && ((result instanceof Boolean) ? ((Boolean) result) : (result instanceof Integer) && ((Integer) result) == 0);
        }
        return false;
    }

    @Override
    public Map<String, String> getEnvironment(File home) {
        if(environemnt == null){
            environemnt = new HashMap<String, String>();
        }
        return environemnt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    public void setCheck(Closure check) {
        this.check = check;
    }

    public void setEnvironemnt(Map<String, String> environemnt) {
        this.environemnt = environemnt;
    }

    public Closure getClosure() {
        return closure;
    }

    public Closure getCheck() {
        return check;
    }

    public Map<String, String> getEnvironemnt() {
        return environemnt;
    }
}
