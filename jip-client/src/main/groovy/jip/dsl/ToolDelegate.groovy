package jip.dsl

import jip.tools.DefaultParameter
import jip.tools.DefaultTool
import jip.tools.Parameter

/**
 * Basic tool implementation
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ToolDelegate{
    private DefaultTool tool

    ToolDelegate(DefaultTool tool) {
        this.tool = tool
    }

    def propertyMissing(String name) {
        return tool."${name}"
    }

    def propertyMissing(String name, def arg) {
        tool."${name}" = arg
    }

    void description(String description){
        tool.setDescription(description)
    }

    void exec(String exec){
        tool.setClosure({ cfg->
            bash(exec, cfg)
        })
    }

    void exec(Closure exec){
        tool.setClosure(exec)
    }

    void pipeline(Closure exec){
        tool.setPipeline(exec)
    }

    void args(Closure args){
        tool.setArgs(args)
    }

    Parameter input(String name){
        return input([name:name], null)
    }

    Parameter input(String name, Closure closure){
        return input([name:name], closure)
    }

    Parameter input(Map cfg){
        return input(cfg, null)
    }

    Parameter input(Map cfg, Closure closure){
        return parameter(cfg, true, false, closure)
    }

    Parameter output(String name){
        return output([name:name], null)
    }

    Parameter output(String name, Closure closure){
        return output([name:name], closure)
    }

    Parameter output(Map cfg){
        return output(cfg, null)
    }

    Parameter output(Map cfg, Closure closure){
        return parameter(cfg, false, true, closure)
    }

    Parameter option(String name){
        return option([name:name], null)
    }

    Parameter option(String name, Closure closure){
        return option([name:name], closure)
    }

    Parameter option(Map cfg){
        return option(cfg, null)
    }

    Parameter option(Map cfg, Closure closure){
        return parameter(cfg, false, false, closure)
    }

    Parameter parameter(Map cfg, boolean input, boolean output, Closure closure){
        DefaultParameter p = new DefaultParameter()
        for (Object key : cfg.keySet()) {
            Object value = cfg.get(key)
            try {
                p."${key}" = value
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        if(p.name == null){
            throw new NullPointerException("No parameter name specified !")
        }

        p.input = input
        p.output = output
        if(input || output)
            p.file = true
        if(p.defaultValue != null && p.defaultValue instanceof Collection){
            p.list = true
        }
        if(closure != null){
            closure.delegate = new ParameterDelegate(p)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call()
        }
        tool.getParameter().put(p.getName(), p)

        // set defaults
        if(input && (tool.getDefaultInput() == null)){
            tool.setDefaultInput(p.name)
        }
        if(output && (tool.getDefaultOutput() == null)){
            tool.setDefaultOutput(p.name)
        }
        return p
    }
}
