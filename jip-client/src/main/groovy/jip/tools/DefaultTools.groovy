package jip.tools

/**
 * Default tool implementations
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultTools {

    public static Closure tools = {
        tool("bash"){
            exec '''${args.join(" ")}'''
            option(name:"args", list:true)
        }
    }
}
