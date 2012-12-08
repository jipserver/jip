package jip.tools

import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultToolServiceTest {


    @Test
    public void testLoadingDefaultTools() throws Exception {
        def service = new DefaultToolService(null)
        assert service.getTools().size() == 1
        assert service.getTool("bash") != null
    }
}
