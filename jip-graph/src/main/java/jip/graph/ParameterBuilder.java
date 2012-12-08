package jip.graph;

public class ParameterBuilder {
    private String name;
    private String description;
    private boolean list;
    private boolean mandatory;
    private boolean file;
    private boolean output;
    private Object defaultValue;
    private String[] options;
    private String expand;
    private String expandValue;
    private String type;
    private boolean defaultInput;
    private boolean defaultOutput;

    public ParameterBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ParameterBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ParameterBuilder setList(boolean list) {
        this.list = list;
        return this;
    }

    public ParameterBuilder setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public ParameterBuilder setFile(boolean file) {
        this.file = file;
        return this;
    }

    public ParameterBuilder setOutput(boolean output) {
        this.output = output;
        return this;
    }

    public ParameterBuilder setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ParameterBuilder setOptions(String[] options) {
        this.options = options;
        return this;
    }

    public ParameterBuilder setExpand(String expand) {
        this.expand = expand;
        return this;
    }

    public ParameterBuilder setExpandValue(String expandValue) {
        this.expandValue = expandValue;
        return this;
    }

    public ParameterBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public ParameterBuilder setDefaultInput(boolean defaultInput) {
        this.defaultInput = defaultInput;
        return this;
    }

    public ParameterBuilder setDefaultOutput(boolean defaultOutput) {
        this.defaultOutput = defaultOutput;
        return this;
    }

    public Parameter createParameter() {
        Parameter p = new Parameter();
        p.setName(name);
        p.setDescription(description);
        p.setList(list);
        p.setMandatory(mandatory);
        p.setFile(file);
        p.setOutput(output);
        p.setDefaultValue(defaultValue);
        p.setOptions(options);
        p.setExpand(expand);
        p.setExpandValue(expandValue);
        p.setType(type);
        p.setDefaultInput(defaultInput);
        p.setDefaultOutput(defaultOutput);
        return p;
    }
}