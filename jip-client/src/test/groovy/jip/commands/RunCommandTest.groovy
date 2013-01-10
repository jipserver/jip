package jip.commands

import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class RunCommandTest {


    @Test
    public void testLoadingDescription() throws Exception {
        assert new RunCommand(null, null).getLongDescription() != null
        assert new RunCommand(null, null).getShortDescription() != null
    }
}
