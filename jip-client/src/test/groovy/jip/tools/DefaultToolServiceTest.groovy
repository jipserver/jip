package jip.tools

import jip.dsl.JipDSL
import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultToolServiceTest {


    @Test
    public void testLoadingDefaultTools() throws Exception {
        def service = new DefaultToolService(null, null, null)
        assert service.getTools().size() == 1
        assert service.getTool("bash") != null
    }


    @Test
    public void testParsingOptions() throws Exception {
        def tools = {
            tool("fastqc"){
                description = "FastQC stats"
                version = "10.1"
                exec '''echo "fastqc ${format ? "--format "+format : ""} ${!extract ? "--noextract" : ""} ${input.join(" ")}"'''
                input(name:"input", list:true, mandatory:true)
                option(name:"format", options:["fastq", "bam", "sam"])
                option(name:"extract", defaultValue:false)
            }
        }

        def dsl = new JipDSL()
        def context = dsl.evaluateToolDefinition(tools)
        assert context != null
        assert context.tools.size() == 1
        def fqc = context.tools.get("fastqc")
        assert fqc != null
        assert fqc.name == "fastqc"
        assert fqc.description == "FastQC stats"
        assert fqc.getParameter() != null
        assert fqc.getParameter().size() == 3
        assert fqc.getDefaultOutput() == null
        assert fqc.getDefaultInput() == "input"
        assert fqc.closure != null


    }
}
