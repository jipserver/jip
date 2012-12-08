package jip.dsl

import org.slf4j.LoggerFactory
import org.slf4j.Logger
import jip.tools.Installer
import jip.tools.Tool

/**
 * The JIP DSL context that defines
 * methods and attributes available in a DSL run
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class JipDSLContext {
    /**
     * The logger
     */
    public static Logger log = LoggerFactory.getLogger(JipDSLContext.class)
    /**
     * The registered installer
     */
    Map<String, Installer> installer = [:]
    /**
     * Registered tools
     */
    Map<String, Tool> tools = [:]

    def installer(String name){
        if(installer.containsKey(name)){
            throw new RuntimeException("Installer ${name} is already defined!")
        }
        ToolInstaller implementation = new ToolInstaller(name: name)
        installer.put(name, implementation)

        return implementation
    }

    def installer(String name, Closure definition){
        if(installer.containsKey(name)){
            throw new RuntimeException("Installer ${name} is already defined!")
        }
        log.info("Adding installer ${name}")
        ToolInstaller implementation = new ToolInstaller(name: name)
        definition.delegate = implementation
        definition.resolveStrategy = Closure.DELEGATE_FIRST
        definition.call()
        implementation.validate()
        installer.put(name, implementation)
    }
    def tool(String name){
        if(tools.containsKey(name)){
            throw new RuntimeException("Tool ${name} is already defined!")
        }
        DefaultTool implementation = new DefaultTool(name: name)
        tools.put(name, implementation)
        return implementation
    }

    def tool(String name, Closure definition){
        if(tools.containsKey(name)){
            throw new RuntimeException("Tool ${name} is already defined!")
        }
        log.info("Adding tool ${name}")
        DefaultTool implementation = new DefaultTool(name: name)
        definition.delegate = implementation
        definition.resolveStrategy = Closure.DELEGATE_FIRST
        definition.call()
        tools.put(name, implementation)
    }

    void validate(){
        // validate installer
        for (Installer i: installer.values()) {
            i.validate()
        }
    }
}
