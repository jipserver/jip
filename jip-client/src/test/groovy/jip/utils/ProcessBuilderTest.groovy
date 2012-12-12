package jip.utils

import org.junit.Test

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

/**
 * Test the process builder
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ProcessBuilderTest {

    @Test
    public void testSimpleLSRun() throws Exception {
        assert new jip.utils.ProcessBuilder("ls -la").run() == 0
    }
    @Test
    public void testRuntime() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        def pb = new jip.utils.ProcessBuilder("ls -la")
        assert pb.out(output)
                .run() == 0
        assertTrue(pb.getRuntime() > 0);
    }

    @Test
    public void testRunningPython() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assert new jip.utils.ProcessBuilder("print 'Hello'").interpreter("python")
                .out(output)
                .run() == 0
        assertEquals("Hello", output.toString().trim());
    }

    @Test
    public void testRunningPerl() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assert new jip.utils.ProcessBuilder("print 'Hello'").interpreter("perl")
                .out(output)
                .run() == 0
        assertEquals("Hello", output.toString());
    }

    @Test
    public void testRunningJar() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String jar = new File(getClass().getResource("/Runme.jar").getFile()).getAbsolutePath();
        assert new jip.utils.ProcessBuilder(new File(jar))
                .interpreter("java")
                .out(output)
                .interpreterArguments("-Xmx128m", "-jar")
                .out(output)
                .run() == 0
        assertEquals("Hello", output.toString().trim());
    }

    @Test
    public void testOverwriteEnvironment() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assert new jip.utils.ProcessBuilder('echo $PATH')
                .out(output)
                .environment([PATH:"/bin"]).run() == 0

        assert output.toString().trim() == "/bin"
    }

    @Test
    public void testExtendEnvironmentWithClosure() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assert new jip.utils.ProcessBuilder('echo $PATH')
                .environment([PATH:"/bin"])
                .out(output)
                .environment{
                    PATH="$PATH:/sbin"
                }
                .run() == 0
        assert output.toString().trim() == "/bin:/sbin"
    }
}
