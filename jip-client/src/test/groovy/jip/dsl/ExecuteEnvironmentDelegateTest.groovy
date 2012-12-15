package jip.dsl

import jip.tools.DefaultExecuteEnvironment
import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ExecuteEnvironmentDelegateTest {

    @Test
    void testTimeLongProperty() {
        def ed = new ExecuteEnvironmentDelegate(new DefaultExecuteEnvironment())
        def c = {
            time = 1
        }
        c.delegate = ed
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()
        assert ed.env.maxTime == 60

    }

    @Test
    void testTimeLongCall() {
        def ed = new ExecuteEnvironmentDelegate(new DefaultExecuteEnvironment())
        def c = {
            time 1
        }
        c.delegate = ed
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()
        assert ed.env.maxTime == 60

    }


    @Test
    void testTimeStringProperty() {
        def ed = new ExecuteEnvironmentDelegate(new DefaultExecuteEnvironment())
        def c = {
            time "00:01:00"
        }
        c.delegate = ed
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()
        assert ed.env.maxTime == 60

    }

    @Test
    void testTimeStringCall() {
        def ed = new ExecuteEnvironmentDelegate(new DefaultExecuteEnvironment())
        def c = {
            time "00:01:00"
        }
        c.delegate = ed
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()
        assert ed.env.maxTime == 60

    }
    @Test
    void testMemoryProperty() {
        def ed = new ExecuteEnvironmentDelegate(new DefaultExecuteEnvironment())
        def c = {
            memory = 10
        }
        c.delegate = ed
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()
        assert ed.env.maxMemory == 10

    }

    @Test
    void testMemoryCall() {
        def ed = new ExecuteEnvironmentDelegate(new DefaultExecuteEnvironment())
        def c = {
            memory 10
        }
        c.delegate = ed
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()
        assert ed.env.maxMemory == 10

    }
    @Test
    void testThreadsProperty() {
        def ed = new ExecuteEnvironmentDelegate(new DefaultExecuteEnvironment())
        def c = {
            threads = 10
        }
        c.delegate = ed
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()
        assert ed.env.threads == 10

    }

    @Test
    void testThreadsCall() {
        def ed = new ExecuteEnvironmentDelegate(new DefaultExecuteEnvironment())
        def c = {
            threads 10
        }
        c.delegate = ed
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c()
        assert ed.env.threads == 10

    }
}
