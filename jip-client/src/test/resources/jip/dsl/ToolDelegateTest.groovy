package jip.dsl

import jip.tools.DefaultTool
import org.junit.Before
import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ToolDelegateTest {

    ToolDelegate toolDelegate

    @Test
    void testSettingEnvironment() {
        def c = {
            env(time: 1, memory:100, threads:2)
        }
        c.delegate = toolDelegate
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()

        assert toolDelegate.tool.executeEnvironment.maxTime == 60
        assert toolDelegate.tool.executeEnvironment.maxMemory == 100
        assert toolDelegate.tool.executeEnvironment.threads == 2
    }

    @Test
    void testSettingEnvironmentTimeAsString() {
        def c = {
            env(time: "01:00:00", memory:100, threads:2)
        }
        c.delegate = toolDelegate
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()

        assert toolDelegate.tool.executeEnvironment.maxTime == 3600
        assert toolDelegate.tool.executeEnvironment.maxMemory == 100
        assert toolDelegate.tool.executeEnvironment.threads == 2
    }

    @Test
    void testSettingEnvironmentClosure() {
        def c = {
            env{
                time 1
                memory 100
                threads 2
            }
        }
        c.delegate = toolDelegate
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()

        assert toolDelegate.tool.executeEnvironment.maxTime == 60
        assert toolDelegate.tool.executeEnvironment.maxMemory == 100
        assert toolDelegate.tool.executeEnvironment.threads == 2
    }


    @Before
    public void setUp() throws Exception {
        this.toolDelegate = new ToolDelegate(new DefaultTool("testtool"))
    }
}
