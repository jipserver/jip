package jip.utils

import jip.runner.BasicScriptRunner
import org.junit.Test

/**
 * Test the process builder
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ProcessBuilderTest {

    @Test
    public void testSimpleBashScriptCreate() throws Exception {
        String path = System.getenv("PATH")
        println(path)
        BasicScriptRunner r = new ProcessBuilder("ls").get()
        assert r != null
        assert r.interpreter == BasicScriptRunner.Interpreter.bash
        assert r.script == "ls"
        assert r.workingDir == new File("").absolutePath
        assert r.stdout == System.out
        assert r.stderr == System.err

    }
    @Test
    public void testOverwriteEnvironment() throws Exception {
        BasicScriptRunner r = new ProcessBuilder("ls").environment([PATH:"/bin"]).get()
        assert r.environment == [PATH:  "/bin"]
    }

    @Test
    public void testExtendEnvironment() throws Exception {
        String path = System.getenv("PATH")
        BasicScriptRunner r = new ProcessBuilder("ls")
                .environment([PATH:"/bin"])
                .environment{
                   PATH="$PATH:/sbin"
                }
                .get()
        assert r.environment == [PATH:  "/bin:/sbin"]
    }
}
