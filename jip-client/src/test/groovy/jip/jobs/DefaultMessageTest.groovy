package jip.jobs

import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class DefaultMessageTest {

    @Test
    public void testConvertToMap() throws Exception {
        def date = new Date()
        def m = [
                type: "Info",
                createDate: date.time,
                message: "The message"
        ]

        def message = new DefaultMessage(m)
        assert message.getCreateDate() == date
        assert message.getMessage() == "The message"
        assert message.getType() == MessageType.Info
    }
}
