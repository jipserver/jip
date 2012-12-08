package jip.dsl

import jip.tools.Parameter

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultParameter implements Parameter{
    String name
    String description
    boolean file
    boolean list
    boolean output
    boolean input
    boolean mandatory
    boolean defaultInput
    boolean defaultOutput
    def defaultValue
    String type

    @Override
    String getName() {
        return name
    }

    @Override
    String getDescription() {
        return description;
    }

    @Override
    boolean isFile() {
        return file
    }

    @Override
    boolean isList() {
        return list
    }

    @Override
    boolean isOutput() {
        return output
    }

    @Override
    boolean isInput() {
        return input;
    }

    @Override
    boolean isMandatory() {
        return mandatory
    }

    @Override
    Object getDefaultValue() {
        return defaultValue
    }

    String getType() {
        return type
    }

    void setDefaultValue(Object defaultValue){
        this.defaultValue = defaultValue
        if(defaultValue != null && defaultValue instanceof Collection){
            this.list = true
        }
    }
}
