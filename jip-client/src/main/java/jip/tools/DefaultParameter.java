package jip.tools;

/**
 * Default parameter implementation
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultParameter implements Parameter{
    /**
     * The name
     */
    private String name;
    /**
     * The description
     */
    private String description;
    /**
     * Represents files
     */
    private boolean file;
    /**
     * Represents a list
     */
    private boolean list;
    /**
     * Represents an output value
     */
    private boolean output;
    /**
     * Represents an input value
     */
    private boolean input;
    /**
     * Is mandatory
     */
    private boolean mandatory;
    /**
     * The data type
     */
    private Class dataType;
    /**
     * The default value
     */
    private Object defaultValue;
    /**
     * The file type/extension
     */
    private String type;
    /**
     * Valid options
     */
    private Object[] options;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isFile() {
        return file;
    }

    @Override
    public boolean isList() {
        return list;
    }

    @Override
    public boolean isOutput() {
        return output;
    }

    @Override
    public boolean isInput() {
        return input;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public Class getDataType() {
        return dataType;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Object[] getOptions() {
        return options;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public void setOutput(boolean output) {
        this.output = output;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public void setDataType(Class dataType) {
        this.dataType = dataType;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOptions(Object[] options) {
        this.options = options;
    }
}
