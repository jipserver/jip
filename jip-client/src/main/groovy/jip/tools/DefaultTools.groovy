package jip.tools

/**
 * Default tool implementations
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultTools {

    public static Closure tools = {
        tool("bash"){
            description '''Run bash commands'''
            version '1.0'
            exec '''${args.join(" ")}'''
            option(name:"args", list:true)
        }
    }
}
