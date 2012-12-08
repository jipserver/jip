package jip.graph;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static junit.framework.Assert.*;

public class PipelineGraphTest{

    @Test
    public void testSplitterPattern() throws Exception {
        Matcher bla = PipelineGraph.CONFIG_SPLITTER.matcher("bla");
        assertFalse(bla.matches());
        Matcher m = PipelineGraph.CONFIG_SPLITTER.matcher("${a.b}");
        assertTrue(m.matches());
        assertEquals("a",m.group(1));
        assertEquals("b",m.group(2));
    }


    @Test
    public void testGraphConstructionWithManualEdges(){
        PipelineJob job_a = new PipelineJob("a");
        job_a.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_a").setDescription("input a parameter").setList(false).setMandatory(false).createParameter(),
                new ParameterBuilder().setName("output_a").setDescription("output a parameter").setList(false).setMandatory(false).setOutput(true).createParameter()
        )
        );
        PipelineJob job_b = new PipelineJob("b");
        job_a.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_b").setDescription("input a parameter").setList(false).setMandatory(false).createParameter(),
                new ParameterBuilder().setName("output_b").setDescription("output a parameter").setList(false).setMandatory(false).setOutput(true).createParameter()
            )
        );
        job_b.setAfter(Arrays.asList("a"));
        Pipeline pipeline_a = new Pipeline();
        pipeline_a.setExecutions(Arrays.asList(job_a, job_b));
        PipelineGraph graph = new PipelineGraph(pipeline_a);
        graph.prepare();
        List<JobNode> nodes = graph.getNodes();
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertEquals(0, graph.validatePipeline().size());
        assertEquals(0, graph.validate().size());
        assertEquals(1, graph.getGraph().edgeSet().size());
        assertNotNull(graph.getGraph().inDegreeOf(graph.findNode("b")));
    }
    @Test
    public void testParameterValidation(){
        PipelineJob job_a = new PipelineJob("a");
        job_a.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_a").setDescription("input a parameter").setList(false).setMandatory(true).createParameter(),
                new ParameterBuilder().setName("output_a").setDescription("output a parameter").setList(false).setMandatory(true).setOutput(true).createParameter()
            )
        );
        PipelineJob job_b = new PipelineJob("b");
        job_b.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_b").setDescription("input a parameter").setList(false).setMandatory(true).createParameter(),
                new ParameterBuilder().setName("output_b").setDescription("output a parameter").setList(false).setMandatory(true).setOutput(true).createParameter()
            )
        );
        job_b.setAfter(Arrays.asList("a"));
        Pipeline pipeline_a = new Pipeline();
        pipeline_a.setExecutions(Arrays.asList(job_a, job_b));
        PipelineGraph graph = new PipelineGraph(pipeline_a);
        assertEquals(4, graph.validate().size());
        ArrayList<String> messages = new ArrayList<String>();
        for (Error error : graph.validate()) {
            messages.add(error.getMessage());
        }
        assertEquals(Arrays.asList(
                "Parameter input_a for node a is not set but is marked as mandatory!",
                "Parameter output_a for node a is not set but is marked as mandatory!",
                "Parameter input_b for node b is not set but is marked as mandatory!",
                "Parameter output_b for node b is not set but is marked as mandatory!"
        ), messages);
    }

    @Test
    public void testNodeExpansion(){
        PipelineJob job_a = new PipelineJob("a");
        job_a.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_a").setDescription("input a parameter").setList(false).createParameter()
        )
        );
        List<String> inputs = Arrays.asList("input-1", "input-2", "input-3");
        job_a.getConfiguration().put("input_a", inputs);

        Pipeline pipeline_a = new Pipeline();
        pipeline_a.setExecutions(Arrays.asList(job_a));

        PipelineGraph graph = new PipelineGraph(pipeline_a);
        assertEquals(1, graph.getNodes().size());
        graph.prepare();
        List<jip.graph.JobNode> nodes = graph.getNodes();
        assertEquals(3, nodes.size());
        assertEquals("input-1", graph.findNode("a_split_0").getConfiguration().get("input_a"));
        assertEquals("input-2", graph.findNode("a_split_1").getConfiguration().get("input_a"));
        assertEquals("input-3", graph.findNode("a_split_2").getConfiguration().get("input_a"));
    }

    @Test
    public void testNodeExpansionWithPipelineConfiguration(){
        PipelineJob job_a = new PipelineJob("a");
        job_a.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_a").setDescription("input a parameter").setList(false).createParameter()
        )
        );
        List<String> inputs = Arrays.asList("input-1", "input-2", "input-3");


        Pipeline pipeline_a = new Pipeline();
        pipeline_a.setExecutions(Arrays.asList(job_a));
        pipeline_a.getConfiguration().put("input_a", inputs);

        PipelineGraph graph = new PipelineGraph(pipeline_a);
        assertEquals(1, graph.getNodes().size());
        graph.prepare();
        List<jip.graph.JobNode> nodes = graph.getNodes();
        assertEquals(3, nodes.size());
        assertEquals("input-1", graph.findNode("a_split_0").getConfiguration().get("input_a"));
        assertEquals("input-2", graph.findNode("a_split_1").getConfiguration().get("input_a"));
        assertEquals("input-3", graph.findNode("a_split_2").getConfiguration().get("input_a"));
    }

    @Test
    public void testDependencyCreationFromConfiguration(){
        PipelineJob job_a = new PipelineJob("a");
        job_a.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_a").setDescription("input a parameter").setList(false).setMandatory(true).createParameter(),
                new ParameterBuilder().setName("output_a").setDescription("output a parameter").setList(false).setMandatory(true).setOutput(true).createParameter()
        )
        );
        PipelineJob job_b = new PipelineJob("b");
        job_b.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_b").setDescription("input a parameter").setList(false).setMandatory(true).createParameter(),
                new ParameterBuilder().setName("output_b").setDescription("output a parameter").setList(false).setMandatory(true).setOutput(true).createParameter()
        )
        );
        Pipeline pipeline_a = new Pipeline();
        pipeline_a.setExecutions(Arrays.asList(job_a, job_b));

        job_a.getConfiguration().put("input_a", "input-1");
        job_a.getConfiguration().put("output_a", "output-1");
        job_b.getConfiguration().put("input_b", "${a.output_a}");
        job_b.getConfiguration().put("output_b", "${a.output_a}");

        PipelineGraph graph = new PipelineGraph(pipeline_a);
        assertEquals(2, graph.getNodes().size());
        graph.prepare();
        graph.reduceDependencies();
        assertEquals(1, graph.getGraph().edgeSet().size());
        assertEquals(1, graph.getGraph().inDegreeOf(graph.findNode("b")));
        assertEquals("output-1", graph.findNode("b").getConfiguration().get("input_b"));
        assertEquals("output-1", graph.findNode("b").getConfiguration().get("output_b"));


    }
    @Test
    public void testDependencyCreationAndExpandCollapse(){
        PipelineJob job_a = new PipelineJob("a");
        job_a.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_a").setDescription("input a parameter").setList(false).setMandatory(true).createParameter(),
                new ParameterBuilder().setName("output_a").setDescription("output a parameter").setList(false).setMandatory(true).setOutput(true).createParameter()
        )
        );
        PipelineJob job_b = new PipelineJob("b");
        job_b.setParameters(Arrays.asList(
                new ParameterBuilder().setName("input_b").setDescription("input a parameter").setList(true).setMandatory(true).createParameter(),
                new ParameterBuilder().setName("output_b").setDescription("output a parameter").setList(false).setMandatory(true).setOutput(true).createParameter()
        )
        );
        Pipeline pipeline_a = new Pipeline();
        pipeline_a.setExecutions(Arrays.asList(job_a, job_b));

        job_a.getConfiguration().put("input_a", Arrays.asList("input-1", "input-2", "input-3"));
        job_a.getConfiguration().put("output_a", Arrays.asList("output-1", "output-2", "output-3"));
        job_b.getConfiguration().put("input_b", "${a.output_a}");
        job_b.getConfiguration().put("output_b", "output-b-1");

        PipelineGraph graph = new PipelineGraph(pipeline_a);
        assertEquals(2, graph.getNodes().size());
        graph.prepare();
        graph.reduceDependencies();
        assertEquals(3, graph.getGraph().edgeSet().size());
        assertEquals(3, graph.getGraph().inDegreeOf(graph.findNode("b")));
        assertEquals(4, graph.getNodes().size());
        assertEquals("input-1", graph.findNode("a_split_0").getConfiguration().get("input_a"));
        assertEquals("input-2", graph.findNode("a_split_1").getConfiguration().get("input_a"));
        assertEquals("input-3", graph.findNode("a_split_2").getConfiguration().get("input_a"));
        assertEquals(Arrays.asList("output-1", "output-2", "output-3"), graph.findNode("b").getConfiguration().get("input_b"));
        assertEquals("output-b-1", graph.findNode("b").getConfiguration().get("output_b"));
    }
}
