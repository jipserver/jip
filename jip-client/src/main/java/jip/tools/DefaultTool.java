package jip.tools;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultTool implements Tool {
    private String description;
    private String name;
    private Map<String, Parameter> parameter;
    private String defaultInput;
    private String defaultOutput;
    private List<String> installer;
    private Closure closure;
    private Closure pipeline;
    private String version;
    private Closure args;

    @Override
    public String getName() {
        return name; 
    }
    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void run(Map cfg) throws Exception {
        closure.call(cfg);
    }

    @Override
    public Map<String, Parameter> getParameter() {
        if(parameter == null){
            parameter = new HashMap<String, Parameter>();
        }
        return parameter;
    }

    @Override
    public String getDefaultInput() {
        return defaultInput;
    }

    @Override
    public String getDefaultOutput() {
        return defaultOutput;
    }

    @Override
    public List<String> getInstaller() {
        return installer;
    }

    public Closure getArgs() {
        return args;
    }

    public Closure getPipeline() {
        return pipeline;
    }

    public void setPipeline(Closure pipeline) {
        this.pipeline = pipeline;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameter(Map<String, Parameter> parameter) {
        this.parameter = parameter;
    }

    public void setDefaultInput(String defaultInput) {
        this.defaultInput = defaultInput;
    }

    public void setDefaultOutput(String defaultOutput) {
        this.defaultOutput = defaultOutput;
    }

    public void setInstaller(List<String> installer) {
        this.installer = installer;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setArgs(Closure args) {
        this.args = args;
    }

}
