package jip.dsl

import jip.utils.Templates


/**
 * Basic DSL tests
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class JipDSLTest extends GroovyTestCase {

    public void testInstallerWithExec(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            installer "test-installer", {
                exec "runme"
                check "checkme"
            }
        """, [:])
        assert dsl.context.installer.size() == 1
        assert dsl.context.installer["test-installer"].closure != null
        assert dsl.context.installer["test-installer"].check != null

        dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            installer "test-installer", {
                exec = "runme"
                check= "checkme"
            }
        """, [:])
        assert dsl.context.installer.size() == 1
        assert dsl.context.installer["test-installer"].closure != null
        assert dsl.context.installer["test-installer"].check != null

        dsl = new JipDSL()
        dsl.evaluateToolDefinition("""
            installer("test-installer") {
                exec "runme"
                check "checkme"
            }
        """, [:])
        assert dsl.context.installer.size() == 1
        assert dsl.context.installer["test-installer"].closure != null
        assert dsl.context.installer["test-installer"].check != null

        dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            installer("test-installer") {
                exec = "runme"
                check= "checkme"
            }
        """, [:])
        assert dsl.context.installer.size() == 1
        assert dsl.context.installer["test-installer"].closure != null
        assert dsl.context.installer["test-installer"].check != null
    }

    public void testInstallerPassClosure(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            installer "test-installer", {
                exec {
                  println "hello"
                }
                check "checkme"
            }
        """, [:])
        assert dsl.context.installer.size() == 1
        assert (dsl.context.installer["test-installer"].closure instanceof Closure)
        assert dsl.context.installer["test-installer"].check != null
    }


    public void testParsingToolWithClosure(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            tool "test-tool", {
                description "checkme"
                exec {
                  println "hello"
                }

            }
        """, [:])
        assert dsl.context.tools.size() == 1
        assert (dsl.context.tools["test-tool"].closure instanceof Closure)

    }

    public void testParsingToolWithStringAndInterpreter(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            tool "test-tool", {
                description "checkme"
                exec "ls -la"
            }
        """, [:])
        assert dsl.context.tools.size() == 1
        assert dsl.context.tools["test-tool"].name == "test-tool"
        assert dsl.context.tools["test-tool"].closure != null

    }
    public void testParsingToolWithInputParameter(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            tool "test-tool", {
                exec "ls -la"
                input "infile"
            }
        """, [:])
        assert dsl.context.tools.size() == 1
        assert dsl.context.tools["test-tool"].parameter.size() == 1
        assert dsl.context.tools["test-tool"].parameter["infile"] != null

    }

    public void testParsingToolWithInputParameterAsMap(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            tool "test-tool", {
                exec "ls -la"
                input name:"infile", mandatory:true, defaultValue:[1,2,3]
            }
        """, [:])
        assert dsl.context.tools.size() == 1
        assert dsl.context.tools["test-tool"].parameter.size() == 1
        def p = dsl.context.tools["test-tool"].parameter["infile"]
        assert p.name == "infile"
        assert p.input
        assert p.mandatory
        assert p.defaultValue == [1,2,3]
    }

    public void testParsingToolWithInputParameterFromClosure(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            tool "test-tool", {
                exec "ls -la"
                input("infile"){
                    mandatory = true
                }
            }
        """, [:])
        assert dsl.context.tools.size() == 1
        assert dsl.context.tools["test-tool"].parameter.size() == 1
        def p = dsl.context.tools["test-tool"].parameter["infile"]
        assert p.name == "infile"
        assert p.input
        assert p.mandatory
    }

    public void testParsingToolWithInputParameterFromClosureDefaultParameter(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            tool "test-tool", {
                exec "ls -la"
                input("infile"){
                    mandatory = true
                    defaultValue = [1,2,3]
                }
            }
        """, [:])
        assert dsl.context.tools.size() == 1
        assert dsl.context.tools["test-tool"].parameter.size() == 1
        def p = dsl.context.tools["test-tool"].parameter["infile"]
        assert p.name == "infile"
        assert p.input
        assert p.mandatory
        assert p.file
        assert p.defaultValue == [1,2,3]
    }

    public void testParsingToolWithOutputParameterFromClosureDefaultParameter(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            tool "test-tool", {
                exec "ls -la"
                output("outfile"){
                    mandatory = true
                    defaultValue = [1,2,3]
                }
            }
        """, [:])
        assert dsl.context.tools.size() == 1
        assert dsl.context.tools["test-tool"].parameter.size() == 1
        def p = dsl.context.tools["test-tool"].parameter["outfile"]
        assert p.name == "outfile"
        assert !p.input
        assert p.output
        assert p.mandatory
        assert p.file
        assert p.defaultValue == [1,2,3]
    }

    public void testParsingToolWithParameterFromClosureDefaultParameter(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            tool "test-tool", {
                exec "ls -la"
                option("outfile"){
                    mandatory = true
                    defaultValue = [1,2,3]
                }
            }
        """, [:])
        assert dsl.context.tools.size() == 1
        assert dsl.context.tools["test-tool"].parameter.size() == 1
        def p = dsl.context.tools["test-tool"].parameter["outfile"]
        assert p.name == "outfile"
        assert !p.input
        assert !p.output
        assert p.mandatory
        assert !p.file
        assert p.defaultValue == [1,2,3]
    }

    public void testWritingSimpleTemplate(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition('''
            tool "test-tool", {
                exec 'I am ${name}, are you my ${target} ? ${answer}'
                option("name")
                option("target")
                option( name:"answer", defaultValue:"yes")
            }
        ''', [:])
        def script = 'I am ${name}, are you my ${target} ? ${answer}'
        def tool = dsl.context.tools["test-tool"]
        assert Templates.fillTemplate(tool, script, [:]) == "I am , are you my  ? yes"
        assert Templates.fillTemplate(tool, script, [name:"Hans", target:"enemy"]) == "I am Hans, are you my enemy ? yes"
        assert Templates.fillTemplate(tool, script, [name:"Hans", target:"enemy", answer:"no"]) == "I am Hans, are you my enemy ? no"

    }

    public void testWritingSimpleTemplateWithLoop(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition('''
            tool "test-tool", {
                exec '<% list.each{ out << "${it}" } %>'
                option( name:"list", defaultValue:[1,2,3])
            }
        ''', [:])
        def script = '<% list.each{ out << "${it}" } %>'
        def tool = dsl.context.tools["test-tool"]
        Templates.fillTemplate(tool, script, [:]) == "123"

    }

}
