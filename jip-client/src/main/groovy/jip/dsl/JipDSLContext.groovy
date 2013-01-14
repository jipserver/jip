package jip.dsl

import com.google.inject.Inject
import jip.JipEnvironment
import jip.tools.DefaultInstaller
import jip.tools.DefaultTool
import jip.tools.JipContext
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
class JipDSLContext implements JipContext{
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

    /**
     * The jip runtime
     */
    JipEnvironment jipRuntime

    /**
     * The DSL
     */
    JipDSL dsl

    @Inject
    JipDSLContext(JipEnvironment jipRuntime) {
        this.jipRuntime = jipRuntime
    }

    def installer(String name, Closure definition){
        if(installer.containsKey(name)){
            throw new RuntimeException("Installer ${name} is already defined!")
        }
        log.info("Adding installer ${name}")
        DefaultInstaller installer = new DefaultInstaller()
        installer.setName(name)
        if(definition != null){
            definition.delegate = new InstallerDelegate(installer)
            definition.resolveStrategy = Closure.DELEGATE_FIRST
            definition.call()
        }
        this.installer.put(name, installer)
    }

    def tool(String name, Closure definition){
        if(tools.containsKey(name)){
            throw new RuntimeException("Tool ${name} is already defined!")
        }
        log.info("Adding tool ${name}")
        DefaultTool implementation = new DefaultTool(name, this)
        definition.delegate = new ToolDelegate(implementation)
        definition.resolveStrategy = Closure.DELEGATE_FIRST
        definition.call()
        tools.put(name, implementation)
    }

}
