package jip

import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class CLIHelperTest {


    @Test
    public void testRangeParsing() throws Exception {
        assert [10,11,12,1] == CLIHelper.parseRange(Arrays.asList("10-12", "1"))

    }
}
