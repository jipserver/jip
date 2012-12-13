package jip.dsl

import jip.graph.Pipeline
import jip.graph.PipelineGraph

/**
 * Test pipeline functions for DSL
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class JipDSLPipelinesTest extends GroovyTestCase {

    public void testToolsInClosure(){
        def dsl = new JipDSL()
        def script = {
            tool("cat"){
                input(name:"infiles", list:true)
                output(name: "outfile")
            }

            tool("linecount"){
                input(name:"input")
                output(name: "output")
            }
        }

        dsl.evaluateToolDefinition(script)
        assert dsl.context.tools.size() == 2
    }

    public void testPipelineDefinition(){
        def dsl = new JipDSL()
        def script = {
            tool("cat"){
                input(name:"infiles", list:true)
                output(name: "outfile")
            }

            tool("linecount"){
                input(name:"input")
                output(name: "output")
            }

            tool("count"){
                input(name:"infiles", list:true)
                output(name: "outfile")
                pipeline{
                    c = cat(infiles:infiles, outfile:"count_out")
                    lc = linecount(input:c.outfile, outfile:"result")
                }
            }
        }

        dsl.evaluateToolDefinition(script)
        assert dsl.context.tools.size() == 3
    }

    public void testSingleToolRunEvaluation(){
        def dsl = new JipDSL()
        def script = {
            tool("cat"){
                input(name:"infiles", list:true)
                output(name: "outfile")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            cat(infiles: ["1.file", "2.file"])
        }
        assert p != null
        assert p.executions.size() == 1
        assert p.executions[0].id == "cat-1"
        assert p.executions[0].configuration.size() == 1
        assert p.executions[0].configuration['infiles'] == ["1.file", "2.file"]
    }

    public void testDependencyToolRunEvaluation(){
        def dsl = new JipDSL()
        def script = {
            tool("cat"){
                input(name:"infiles", list:true)
                output(name: "outfile")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            def c = cat(infiles: ["1.file", "2.file"], outfile: "output")
            def c2 = cat(infiles: [c.outfile])
            c2.test = "test-value"
        }
        assert p != null
        assert p.executions.size() == 2
        assert p.executions[0].id == "cat-1"
        assert p.executions[0].configuration.size() == 2
        assert p.executions[0].configuration['infiles'] == ["1.file", "2.file"]
        assert p.executions[0].configuration['outfile'] == "output"
        assert p.executions[1].id == "cat-2"
        assert p.executions[1].configuration.size() == 2
        assert p.executions[1].configuration['infiles'] == ['${cat-1.outfile}']
        assert p.executions[1].configuration['test'] == "test-value"
    }


    public void testDependenciesBasedOnDefaultValues(){
        def dsl = new JipDSL()
        def script = {
            tool("cat"){
                input(name:"infiles", list:true)
                output(name: "outfile")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            cat(infiles: ["1.file", "2.file"], outfile: "output") | cat()
        }
        assert p != null
        assert p.executions.size() == 2
        assert p.executions[0].id == "cat-1"
        assert p.executions[0].configuration.size() == 2
        assert p.executions[0].configuration['infiles'] == ["1.file", "2.file"]
        assert p.executions[0].configuration['outfile'] == "output"
        assert p.executions[1].id == "cat-2"
        assert p.executions[1].configuration.size() == 1
        assert p.executions[1].configuration['infiles'] == ['${cat-1.outfile}']
    }

    public void testDependenciesBasedOnDefaultValuesPipelineEvaluation(){
        def dsl = new JipDSL()
        def script = {
            tool("split"){
                input(name:"input")
                output(name: "output", list:true)
            }
            tool("wc"){
                input(name:"input")
                output(name: "output")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            split(input: "split.in", output:["1.file", "2.file"]) | wc(output:"wc.out")
        }
        assert p != null
        def graph = new PipelineGraph(p)
        graph.prepare()
        graph.reduceDependencies()
        assert graph.validate().size() == 0
        assert graph.validatePipeline().size() == 0
        assert graph.nodes.size() == 3
        assert graph.findNode("split-1") != null
        assert graph.findNode("wc-2_split_0") != null
        assert graph.findNode("wc-2_split_1") != null
        assert graph.findNode("wc-2_split_0").configuration["input"] == "1.file"
        assert graph.findNode("wc-2_split_1").configuration["input"] == "2.file"
    }


    public void testDependencyOutputNameGeneration(){
        def dsl = new JipDSL()
        def script = {
            tool("split"){
                input(name:"input")
                output(name: "output", list:true)
            }
            tool("wc"){
                input(name:"input")
                output(name: "output")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            split(input: "split.in", output:["1.file", "2.file"]) | wc(output:"wc.out")
        }
        assert p != null
        def graph = new PipelineGraph(p)
        graph.prepare()
        graph.reduceDependencies()
        assert graph.findNode("wc-2_split_0").configuration["output"] != graph.findNode("wc-2_split_1").configuration["output"]
        assert graph.findNode("wc-2_split_0").configuration["output"] == "wc.out.wc-2_split_0"
        assert graph.findNode("wc-2_split_1").configuration["output"] == "wc.out.wc-2_split_1"
    }

    public void testOutputTypeExtension(){
        def dsl = new JipDSL()
        def script = {
            tool("wc"){
                input(name:"input")
                output(name: "output", type:"txt")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            wc(input:["1.txt", "2.txt"], output:"wc.txt")
        }
        assert p != null
        def graph = new PipelineGraph(p)
        graph.prepare()
        graph.reduceDependencies()
        assert graph.findNode("wc-1_split_0").configuration["output"] != graph.findNode("wc-1_split_1").configuration["output"]
        assert graph.findNode("wc-1_split_0").configuration["output"] == "wc.wc-1_split_0.txt"
        assert graph.findNode("wc-1_split_1").configuration["output"] == "wc.wc-1_split_1.txt"
    }

    public void testOutputTypeDifferentExtension(){
        def dsl = new JipDSL()
        def script = {
            tool("wc"){
                input(name:"input")
                output(name: "output", type:"txt")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            wc(input:["1.txt", "2.txt"], output:"wc.out")
        }
        assert p != null
        def graph = new PipelineGraph(p)
        graph.prepare()
        graph.reduceDependencies()
        assert graph.findNode("wc-1_split_0").configuration["output"] != graph.findNode("wc-1_split_1").configuration["output"]
        assert graph.findNode("wc-1_split_0").configuration["output"] == "wc.out.wc-1_split_0.txt"
        assert graph.findNode("wc-1_split_1").configuration["output"] == "wc.out.wc-1_split_1.txt"
    }

    public void testPipelineParameterPassing(){
        def dsl = new JipDSL()
        def script = {
            tool("cat"){
                input(name:"infiles", list:true)
                output(name: "outfile")
            }

            tool("linecount"){
                input(name:"input")
                output(name: "output")
            }

            tool("count"){
                input(name:"infiles", list:true)
                output(name: "outfile")
                pipeline{
                    def c = cat(infiles:infiles, outfile:"count_out")
                    def lc = linecount(input:c.outfile, outfile:"result")
                }
            }
        }

        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            count(infiles:["1.txt", "2.txt"], outfile: "wc.out")
        }
        assert p != null
        def graph = new PipelineGraph(p)
        graph.prepare()
        graph.reduceDependencies()
        assert graph.findNode("count-1_cat-1").configuration["infiles"] == ["1.txt", "2.txt"]
        assert graph.findNode("count-1_cat-1").configuration["outfile"] == "count_out"
        assert graph.findNode("count-1_linecount-2").configuration["input"] == "count_out"
        assert graph.findNode("count-1_linecount-2").configuration["outfile"] == "result"


    }
    public void testOutputFileGeneration(){
        def dsl = new JipDSL()
        def script = {
            tool("bash"){
                exec '''${command.join(" ")} > ${output}'''
                option(name:"command", list:true)
                output(name:"output", type:"out")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            bash(command:["ls -la"])
        }
        assert p != null
        def graph = new PipelineGraph(p)
        graph.prepare()
        graph.reduceDependencies()
        assert graph.findNode("bash-1") != null
        assert graph.findNode("bash-1").configuration["output"] == "bash.bash-1.output.out"
    }

    public void testOutputFileGenerationWithRunname(){
        def dsl = new JipDSL()
        def script = {
            tool("bash"){
                exec '''${command.join(" ")} > ${output}'''
                option(name:"command", list:true)
                output(name:"output", type:"out")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            bash(id:"myrun", command:["ls -la"])
        }
        assert p != null
        def graph = new PipelineGraph(p)
        graph.prepare()
        graph.reduceDependencies()
        assert graph.findNode("myrun") != null
        assert graph.findNode("myrun").configuration["output"] == "bash.myrun.output.out"
    }

    public void testCreatingCommandLineArguments(){
        def dsl = new JipDSL()
        def script = {
            tool("bash"){
                exec '''${command.join(" ")} > ${output}'''
                args {
                    [input.join(" ")]
                }
                option(name:"command", list:true)
                input(name:"input", list:true, defaultValue:[])
                output(name:"output", type:"out")
            }
        }
        dsl.evaluateToolDefinition(script)

        Pipeline p = dsl.evaluateRun{
            bash(id:"myrun", command:["ls -la"])
        }
        assert p != null
        def graph = new PipelineGraph(p)
        graph.prepare()
        graph.reduceDependencies()
        assert graph.findNode("myrun") != null
        assert graph.findNode("myrun").configuration["output"] == "bash.myrun.output.out"
        assert graph.findNode("myrun").getPipelineJob().getToolId() == "bash"
    }

}