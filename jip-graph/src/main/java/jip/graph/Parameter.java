/*
 * Copyright (C) 2012 Thasso Griebel
 *
 * This file is part of JIP.
 *
 * JIP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JIP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package jip.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a parameter where the name is used as identifying key
 */
public class Parameter {
    /**
     * Parallel expansion
     */
    public static final String EXPAND_PARALLEL = "parallel";
    /**
     * Serial expansion
     */
    public static final String EXPAND_SERIAL = "serial";

    /**
     * The parameter name
     */
    private String name ="";

    /**
     * The parameter description
     */
    private String description ="";

    /**
     * Does this represent a list
     */
    private boolean list;

    /**
     * Is the parameter mandatory
     */
    private boolean mandatory = false;
    /**
     * Represents a file or a list of files
     */
    private boolean file = false;

    /**
     * True if this is an output parameter
     */
    private boolean output;

    /**
     * Optional default value for this parameter
     */
    private Object defaultValue;

    /**
     * Valid values
     */
    private String[] options;

    /**
     * Expansion type
     */
    private String expand;

    /**
     * Serial expansion value
     */
    private String expandValue;

    /**
     * Optional datatype
     */
    private String type;

    /**
     * True if this is the default input parameter
     */
    private boolean defaultInput = false;
    /**
     * True if this is the default output parameter
     */
    private boolean defaultOutput = false;

    /**
     * Default constructor
     */
    public Parameter() {
    }

    /**
     * Create from map
     *
     * @param data the data map
     */
    public Parameter(Map data) {
        name = (String) data.get("name");
        description = (String) data.get("description");
        list = (Boolean) data.get("list");
        mandatory = (Boolean) data.get("mandatory");
        file = (Boolean) data.get("file");
        output = (Boolean) data.get("output");
        defaultInput = (Boolean) data.get("defaultInput");
        defaultOutput = (Boolean) data.get("defaultOutput");
        defaultValue = data.get("defaultValue");
        options = (String[]) data.get("options");
        type = (String) data.get("type");
    }

    /**
     * Create parameter
     *
     * @param name     the name
     * @param description the description
     * @param list is list
     * @param mandatory is mandatory
     */
    public Parameter(String name, String description, boolean list, boolean mandatory) {
        this.name = name;
        this.description = description;
        this.list = list;
        this.mandatory = mandatory;
    }



    /**
     * Copy constructor
     *
     * @param parameter the source parameter
     */
    public Parameter(Parameter parameter) {
        this.name = parameter.name;
        this.description = parameter.description;
        this.list = parameter.list;
        this.mandatory = parameter.mandatory;
        this.file = parameter.file;
        this.output = parameter.output;
        this.options = parameter.options;
        this.type = parameter.type;
        this.defaultInput = parameter.defaultInput;
        this.defaultOutput = parameter.defaultOutput;
        if(parameter.defaultValue instanceof List){
            this.defaultValue = new ArrayList((List) parameter.defaultValue);
        }else{
            this.defaultValue = parameter.defaultValue;
        }
    }

    /**
     * The name of the parameter
     *
     * @return name the name of the parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the parameter
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description
     *
     * @return description the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return true if this represents a list
     *
     * @return list true if list parameter
     */
    public boolean isList() {
        return list;
    }

    /**
     * Set list parameter
     *
     * @param list the list parameter
     */
    public void setList(boolean list) {
        this.list = list;
    }

    /**
     * Returns true if this parameter is mandatory
     *
     * @return mandatory true if mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Set the parameter to mandatory
     *
     * @param mandatory mandatory
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * true if this is a file parameter
     * @return file the file parameter
     */
    public boolean isFile() {
        return file;
    }

    /**
     * Set file
     * @param file represents a file
     */
    public void setFile(boolean file) {
        this.file = file;
    }

    /**
     * Returns true if this is an output parameter
     *
     * @return output true if output
     */
    public boolean isOutput() {
        return output;
    }

    /**
     * Specify if this is an output parameter
     *
     * @param output the output
     */
    public void setOutput(boolean output) {
        this.output = output;
    }

    /**
     * Get the default value for this parameter
     *
     * @return defaultValue the default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value for this parameter
     *
     * @param defaultValue the default value
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Optional set of available options for this parameter
     *
     * @return options available options for this parameter
     */
    public String[] getOptions() {
        return options;
    }

    /**
     * Set the available options
     *
     * @param options the options
     */
    public void setOptions(String[] options) {
        this.options = options;
    }

    /**
     * Get the expansion option
     *
     * @return expand the expansion option
     */
    public String getExpand() {
        return expand;
    }

    /**
     * Set the expansion option
     *
     * @param expand the expansion option
     */
    public void setExpand(String expand) {
        if(expand != null && !expand.equals(EXPAND_PARALLEL) && ! expand.equals(EXPAND_SERIAL))
            throw new IllegalArgumentException("Expansion " + expand + " is not a valid expansion type!");
        this.expand = expand;
    }

    /**
     * Get the expand value. This is used to link parameters
     * in case of serial execution
     *
     * @return expandValue the expand value
     */
    public String getExpandValue() {
        return expandValue;
    }

    /**
     * Set the expand value
     *
     * @param expandValue expand value
     */
    public void setExpandValue(String expandValue) {
        this.expandValue = expandValue;
    }

    /**
     * Get the data type
     * @return type the data type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the data type
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    public boolean isDefaultInput() {
        return defaultInput;
    }

    public void setDefaultInput(boolean defaultInput) {
        this.defaultInput = defaultInput;
    }

    public boolean isDefaultOutput() {
        return defaultOutput;
    }

    public void setDefaultOutput(boolean defaultOutput) {
        this.defaultOutput = defaultOutput;
    }

}
