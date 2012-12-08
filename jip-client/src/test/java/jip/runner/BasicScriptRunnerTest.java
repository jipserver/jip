package jip.runner;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class BasicScriptRunnerTest {
    @Test
    public void testSimpleLSRun() throws Exception {
        BasicScriptRunner runner = new BasicScriptRunner("TEST", BasicScriptRunner.Interpreter.bash, null, "ls -la", null, null, null, null);
        assertEquals(0, runner.run());
    }
    @Test
    public void testWritingOutput() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BasicScriptRunner runner = new BasicScriptRunner("TEST", BasicScriptRunner.Interpreter.bash, null, "echo Hello", null, null, output, null);
        assertEquals(0, runner.run());
        assertEquals("Hello", output.toString());
        assertTrue(runner.getRunTime() > 0);
    }

    @Test
    public void testRunningPython() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BasicScriptRunner runner = new BasicScriptRunner("TEST", BasicScriptRunner.Interpreter.python, null, "print \"Hello\"", null, null, output, null);
        assertEquals(0, runner.run());
        assertEquals("Hello", output.toString());
    }

    @Test
    public void testRunningPerl() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BasicScriptRunner runner = new BasicScriptRunner("TEST", BasicScriptRunner.Interpreter.perl, null, "print \"Hello\"", null, null, output, null);
        assertEquals(0, runner.run());
        assertEquals("Hello", output.toString());
    }

    @Test
    public void testRunningJar() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String jar = new File(getClass().getResource("/Runme.jar").getFile()).getAbsolutePath();
        BasicScriptRunner runner = new BasicScriptRunner("TEST", BasicScriptRunner.Interpreter.jar, new String[]{"-Xmx128m"}, jar, new String[]{"Hello"}, null, output, null);
        assertEquals(0, runner.run());
        assertEquals("Hello", output.toString());
    }

}
