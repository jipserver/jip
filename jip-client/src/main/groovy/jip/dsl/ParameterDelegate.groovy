package jip.dsl

import jip.tools.DefaultParameter

/**
 * Delegate for parameter
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ParameterDelegate {
    DefaultParameter parameter

    ParameterDelegate(DefaultParameter parameter) {
        this.parameter = parameter
    }

    def propertyMissing(String name) {
        return parameter."${name}"
    }

    def propertyMissing(String name, def arg) {
        parameter."${name}" = arg
    }
}
