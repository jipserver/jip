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
class FileJobStoreTest {
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
    public void testToJsonConversion() throws Exception {
        def pp = new DefaultPipelineService(context, idservice)
        PipelineJob pipelineJob = pp.create("split-wc", [:], dir)
        def store = new FileJobStore(dir)
        store.save(pipelineJob)
        assert new File(dir, pipelineJob.getId()+".job").exists()

        PipelineJob loaded = store.get(pipelineJob.getId())
        assert loaded != null
        assert loaded.id == "0"
        assert loaded.name == "split-wc"
        assert loaded.getJobs() != null
        assert loaded.getJobs().size() == 4

        assert loaded.getJobs().get(0).id == "split-1"
        assert loaded.getJobs().get(0).workingDirectory == dir.getAbsolutePath()
        assert loaded.getJobs().get(0).configuration == pipelineJob.getJobs().get(0).configuration
        assert loaded.getJobs().get(0).environment == pipelineJob.getJobs().get(0).environment
        assert loaded.getJobs().get(0).dependenciesAfter.size() == 3
        assert loaded.getJobs().get(0).dependenciesAfter.get(0) == loaded.getJobs().get(1)
        assert loaded.getJobs().get(0).dependenciesAfter.get(1) == loaded.getJobs().get(2)
        assert loaded.getJobs().get(0).dependenciesAfter.get(2) == loaded.getJobs().get(3)


        assert loaded.getJobs().get(1).id == "wc-2_split_0"
        assert loaded.getJobs().get(1).workingDirectory == dir.getAbsolutePath()
        assert loaded.getJobs().get(1).configuration == pipelineJob.getJobs().get(1).configuration
        assert loaded.getJobs().get(1).environment == pipelineJob.getJobs().get(1).environment

        assert loaded.getJobs().get(2).id == "wc-2_split_1"
        assert loaded.getJobs().get(2).workingDirectory == dir.getAbsolutePath()
        assert loaded.getJobs().get(2).configuration == pipelineJob.getJobs().get(2).configuration
        assert loaded.getJobs().get(2).environment == pipelineJob.getJobs().get(2).environment

        assert loaded.getJobs().get(3).id == "wc-2_split_2"
        assert loaded.getJobs().get(3).workingDirectory == dir.getAbsolutePath()
        assert loaded.getJobs().get(3).configuration == pipelineJob.getJobs().get(3).configuration
        assert loaded.getJobs().get(3).environment == pipelineJob.getJobs().get(3).environment

    }
    @Test
    public void testIteratoringJobs() throws Exception {
        def pp = new DefaultPipelineService(context, idservice)
        def store = new FileJobStore(dir)
        for(int i =0; i< 100; i++){
            PipelineJob pipelineJob = pp.create("split-wc", [:], dir)
            store.save(pipelineJob)
        }
        int c = 0;
        for (PipelineJob job : store.list(false)) {
            c++;
            store.archive(job);
        }
        assert c == 100;
        c = 0;
        for (PipelineJob job : store.list(true)) {
            c++;
            store.delete(job);
        }
        assert c == 100;

        c = 0;
        for (PipelineJob job : store.list(false)) {
            c++;
        }
        assert c == 0;
        c = 0;
        for (PipelineJob job : store.list(true)) {
            c++;
        }
        assert c == 0;
    }
}
