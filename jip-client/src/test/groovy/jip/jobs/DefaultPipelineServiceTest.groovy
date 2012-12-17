package jip.jobs

import com.google.common.io.Files
import jip.dsl.JipDSL
import jip.dsl.JipDSLContext
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultPipelineServiceTest {
    File dir
    static JipDSLContext context
    File idfile
    FileIdService idservice


    @BeforeClass
    public static void initialize() throws Exception {
        context = new JipDSLContext(null)
        def dsl = new JipDSL(context)
        dsl.evaluateToolDefinition {
            tool("ls"){
                input("input")
                output("output")
            }
            tool("wc"){
                input("input")
                output("output")
            }
            tool("split"){
                input("input")
                option(name: "splits", defaultValue: 3)
                output(name: "output", list:true, defaultValue: { cfg->
                    return (1..cfg.splits).collect{cfg.input+"."+it}
                })
            }
            tool("ls-wc"){
                pipeline{
                    ls(input:"1.txt") | wc()
                }
            }
            tool("split-wc"){
                pipeline{
                    split(input:"1.txt") | wc()
                }
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        dir = Files.createTempDir()
        idfile = new File(dir, "ids")
        idservice = new FileIdService(idfile);
    }


    @After
    public void tearDown() throws Exception {
        if (dir != null){
            assert "rm -Rf ${dir.getAbsolutePath()}".execute().waitFor() == 0
        }
    }



    @Test
    public void testSingleToolJob() throws Exception {
        def pp = new DefaultPipelineService(context, idservice)
        PipelineJob pipelineJob = pp.create("ls", [:], dir)
        assert pipelineJob != null
        assert pipelineJob.id == "0"
        assert pipelineJob.getJobs().size() == 1
        assert pipelineJob.getName() == "ls"

        Job job = pipelineJob.getJobs().get(0)
        assert job != null
        assert job.getDependenciesAfter().isEmpty()
        assert job.getDependenciesBefore().isEmpty()
        assert job.getToolName() == "ls"
    }

    @Test
    public void testPipelineJob() throws Exception {
        def pp = new DefaultPipelineService(context, idservice)
        PipelineJob pipelineJob = pp.create("ls-wc", [:], dir)
        assert pipelineJob != null
        assert pipelineJob.id == "0"
        assert pipelineJob.getJobs().size() == 2
        assert pipelineJob.getName() == "ls-wc"

        Job ls = pipelineJob.getJobs().get(0)
        Job wc = pipelineJob.getJobs().get(1)
        assert ls != null
        assert ls.getDependenciesAfter().size() == 1
        assert ls.getDependenciesAfter().get(0) == wc
        assert ls.getDependenciesBefore().isEmpty()
        assert ls.getToolName() == "ls"

        assert wc != null
        assert wc.getDependenciesAfter().isEmpty()
        assert wc.getDependenciesBefore().get(0) == ls
        assert wc.getToolName() == "wc"
    }

    @Test
    public void testSplittingPipelineJob() throws Exception {
        def pp = new DefaultPipelineService(context, idservice)
        PipelineJob pipelineJob = pp.create("split-wc", [:], dir)
        assert pipelineJob != null
        assert pipelineJob.id == "0"
        assert pipelineJob.getJobs().size() == 4
        assert pipelineJob.getName() == "split-wc"

        Job split = pipelineJob.getJobs().get(0)
        Job wc = pipelineJob.getJobs().get(1)
        assert split != null
        assert split.getDependenciesAfter().size() == 3
        assert split.getDependenciesAfter().get(0) == wc
        assert split.getDependenciesBefore().isEmpty()
        assert split.getToolName() == "split"

        assert wc != null
        assert wc.getDependenciesAfter().isEmpty()
        assert wc.getDependenciesBefore().size() == 1
        assert wc.getDependenciesBefore().get(0) == split
        assert wc.getToolName() == "wc"
    }
}
