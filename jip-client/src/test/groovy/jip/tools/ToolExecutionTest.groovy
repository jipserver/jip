package jip.tools

import com.google.common.io.Files
import jip.dsl.JipDSL
import jip.graph.Pipeline
import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ToolExecutionTest {

    @Test
    public void testWorkingDirExecution() throws Exception {
        def tools = {
            tool("touch"){
                exec '''touch ${input}'''
                input(name:"input", mandatory:true)
            }
        }

        def tool = new JipDSL().evaluateToolDefinition(tools).getTools().get("touch")
        def dir = Files.createTempDir()
        try{
            tool.run(dir, [input: "testfile"])
            assert new File(dir, "testfile").exists()
        }finally {
            "rm -Rf ${dir.absolutePath}".execute().waitFor()
        }
    }


    @Test
    public void testParameterClosureExtension() throws Exception {
        def tools = {
            tool("fastqc"){
                exec '''touch ${input.toString()}'''
                input(name:"input", list:true)
                output(name:"output", list:true, defaultValue:"\${input.name}")
            }
            tool("count"){
                exec '''ls ${input.join(" ")} > ${output}'''
                input(name:"input", list: true)
                output(name:"output")
            }
            tool("pipe"){
                pipeline{
                    fastqc(input:["1.txt", "2.txt", "3.txt"]) | count(output:"count-out.txt")
                }
            }

        }

        def tool = new JipDSL().evaluateToolDefinition(tools).getTools().get("pipe")
        def dir = Files.createTempDir()
        try{
            tool.run(dir, [:])
            assert new File(dir, "count-out.txt").exists()
            assert new File(dir, "count-out.txt").text.trim() == "1.txt\n2.txt\n3.txt"
        }finally {
            "rm -Rf ${dir.absolutePath}".execute().waitFor()
        }


    }

    @Test
    public void testPipelineExecution() throws Exception {
        def tools = {
            tool("create"){
                exec '''echo "Hello\nWorld" > ${output}'''
                output(name:"output", mandatory:true)
            }
            tool("count"){
                exec '''cat ${input} | wc -l  > ${output}'''
                input(name:"input", mandatory:true)
                output(name:"output", mandatory:true)
            }
            tool("pipe"){
                pipeline{
                    create(output:"create-out.txt") | count(output:"count-out.txt")
                }
            }
        }

        def dsl = new JipDSL()
        def tool = dsl.evaluateToolDefinition(tools).getTools().get("pipe")
        def dir = Files.createTempDir()
        try{
            tool.run(dir, [:])
            assert new File(dir, "create-out.txt").exists()
            assert new File(dir, "count-out.txt").exists()
            assert new File(dir, "count-out.txt").text.trim() == "2"
        }finally {
            "rm -Rf ${dir.absolutePath}".execute().waitFor()
        }
    }


    @Test
    public void testPipelineExecutionWithAutomaticOutputNaming() throws Exception {
        def tools = {
            tool("create"){
                exec '''echo "Hello\nWorld" > ${output}'''
                output(name:"output")
            }
            tool("count"){
                exec '''cat ${input} | wc -l  > ${output}'''
                input(name:"input", mandatory:true)
                output(name:"output", mandatory:true)
            }
            tool("pipe"){
                pipeline{
                    create() | count(output:"count-out.txt")
                }
            }
        }

        def dsl = new JipDSL()
        def tool = dsl.evaluateToolDefinition(tools).getTools().get("pipe")
        def dir = Files.createTempDir()
        try{
            tool.run(dir, [:])
            assert new File(dir, "count-out.txt").exists()
            assert new File(dir, "count-out.txt").text.trim() == "2"
        }finally {
            "rm -Rf ${dir.absolutePath}".execute().waitFor()
        }


    }
}
