package jip.utils

import org.junit.Test

/**
 * Test execute Delegate executions
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ExecuteDelegateTest {

    @Test
    public void testBashScript() throws Exception {
        def d = new ExecuteDelegate()
        def c = {
            bash("ls -la")
        }
        c.delegate = d
        assert c() == 0
    }

    @Test
    public void testFailOnError() throws Exception {
        def d = new ExecuteDelegate()
        def c = {
            bash("false")
        }
        c.delegate = d
        try {
            c()
        } catch (Exception e) {

        }
    }

    @Test
    public void testPerlScriptWithOutput() throws Exception {
        def d = new ExecuteDelegate()
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        def c = {
            perl("print('Hello')", out:out)
        }
        c.delegate = d
        assert c() == 0
        assert out.toString() == "Hello"

    }
}
