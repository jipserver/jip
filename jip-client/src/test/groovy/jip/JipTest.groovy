package jip

import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class JipTest {


    @Test
    public void testConfigLoading() throws Exception {
        ConfigSlurper s = new ConfigSlurper()

        ConfigObject o = s.parse("""
            HOME = "/users/testuser/"
            test{
                a = 1
                b = "\${HOME}/test"
            }
        """)
        assert o.test != null
        assert o.test.a == 1
        assert o.test.b == "/users/testuser//test"
        s.setBinding(o)
        ConfigObject second = s.parse('''
            second{
                a = test.a
            }
        ''')
        assert second.second.a == o.test.a
    }
}
