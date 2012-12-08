package jip.tools;

/**
 *
 * Tool parameter
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface Parameter {
    /**
     * Get the parameter name
     *
     * @return name the parameter name
     */
    String getName();

    /**
     * Get parameter description
     *
     * @return description the description
     */
    String getDescription();

    /**
     * Returns true if this is a file parameter
     *
     * @return file true if this is a file parameter
     */
    boolean isFile();

    /**
     * True if this is a list
     *
     * @return list true if this is a list
     */
    boolean isList();

    /**
     * True if this is an output parameter
     *
     * @return input true if this is an output parameter
     */
    boolean isOutput();

    /**
     * True if this is an output parameter
     *
     * @return input true if this is an output parameter
     */
    boolean isInput();

    /**
     * True if this parameter is mandatory
     *
     * @return mandatory true if mandatory
     */
    boolean isMandatory();

    /**
     * Return an optional default value
     *
     * @return default optional default
     */
    Object getDefaultValue();

    /**
     * Get optional type definition
     *
     * @return type the parameter type
     */
    String getType();

}
