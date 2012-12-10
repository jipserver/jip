package jip.utils

import org.junit.Test

/**
 * Test environment delegate
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class EnvironmentDelegateTest {

    @Test
    public void testNullInitiaization() throws Exception {
        assert new EnvironmentDelegate(null).data != null
        assert new EnvironmentDelegate(null).data.size() == 0
    }

    @Test
    public void testNonExistingVariable() throws Exception {
        def delegate = new EnvironmentDelegate(null)
        def c = {
            PATH="${PATH}:/bin"
        }
        c.delegate = delegate
        c()
        assert delegate.data.size() == 1
        assert delegate.data["PATH"] == ":/bin"
    }

    @Test
    public void testExtendingVariable() throws Exception {
        def delegate = new EnvironmentDelegate([PATH:"/sbin"])
        def c = {
            PATH="${PATH}:/bin"
        }
        c.delegate = delegate
        c()
        assert delegate.data.size() == 1
        assert delegate.data["PATH"] == "/sbin:/bin"
    }
}
