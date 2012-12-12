package jip.dsl

import jip.utils.Templates


/**
 * Basic DSL tests
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class JipDSLTest extends GroovyTestCase {

    public void testInstallerDefinitionErrors(){
        def dsl = new JipDSL()
        // test error on instantiation without exec
        try {
            dsl.evaluateToolDefinition("""
                installer "test-installer", {
                }
            """, [:])
            fail()
        } catch (Exception error) {
            assert error.message == "No installation procedure specified"
        }


        dsl = new JipDSL()
        try {
            dsl.evaluateToolDefinition("""
                installer "test-installer"
            """, [:])
            fail()
        } catch (Exception error) {
            assert error.message == "No installation procedure specified"
        }

        dsl = new JipDSL()
        try {
            dsl.evaluateToolDefinition("""
                installer "test-installer", {
                    exec 1
                }
            """, [:])
            fail()
        } catch (Exception error) {
            assert error.message == "Execution has to be a string or a closure"
        }

        dsl = new JipDSL()
        try {
            dsl.evaluateToolDefinition("""
                installer "test-installer", {
                    check 1
                }
            """, [:])
            fail()
        } catch (Exception error) {
            assert error.message == "No installation procedure specified"
        }
    }
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
        assert dsl.context.installer["test-installer"].exec == "runme"
        assert dsl.context.installer["test-installer"].check == "checkme"

        dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            installer "test-installer", {
                exec = "runme"
                check= "checkme"
            }
        """, [:])
        assert dsl.context.installer.size() == 1
        assert dsl.context.installer["test-installer"].exec == "runme"
        assert dsl.context.installer["test-installer"].check == "checkme"

        dsl = new JipDSL()
        dsl.evaluateToolDefinition("""
            installer("test-installer") {
                exec "runme"
                check "checkme"
            }
        """, [:])
        assert dsl.context.installer.size() == 1
        assert dsl.context.installer["test-installer"].exec == "runme"
        assert dsl.context.installer["test-installer"].check == "checkme"

        dsl = new JipDSL()
        // test error on instantiation without exec
        dsl.evaluateToolDefinition("""
            installer("test-installer") {
                exec = "runme"
                check= "checkme"
            }
        """, [:])
        assert dsl.context.installer.size() == 1
        assert dsl.context.installer["test-installer"].exec == "runme"
        assert dsl.context.installer["test-installer"].check == "checkme"
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
        assert (dsl.context.installer["test-installer"].exec instanceof Closure)
        assert dsl.context.installer["test-installer"].check == "checkme"
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
        assert dsl.context.tools["test-tool"].description == "checkme"
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
        assert dsl.context.tools["test-tool"].closure == "ls -la"
        assert dsl.context.tools["test-tool"].interpreter == "bash"

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
        def tool = dsl.context.tools["test-tool"]
        assert Templates.fillTemplate(tool, tool.exec, [:]) == "I am , are you my  ? yes"
        assert Templates.fillTemplate(tool, tool.exec, [name:"Hans", target:"enemy"]) == "I am Hans, are you my enemy ? yes"
        assert Templates.fillTemplate(tool, tool.exec, [name:"Hans", target:"enemy", answer:"no"]) == "I am Hans, are you my enemy ? no"

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
        def tool = dsl.context.tools["test-tool"]
        //assert tool.createScript([:]) == "I am , are you my  ? yes"
        println tool.createScript([:]) == "123"

    }

}
