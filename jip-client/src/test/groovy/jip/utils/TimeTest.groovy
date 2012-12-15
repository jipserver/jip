package jip.utils

import org.junit.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

import static junit.framework.Assert.*

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class TimeTest {

    @Test
    public void testMinutesPattern() throws Exception {
        Pattern minutes_seconds = Pattern.compile('^(\\d+):(\\d+)$');
        Matcher m = minutes_seconds.matcher("15:20");
        assertTrue(m.matches());
        assertEquals("15", m.group(1));
        assertEquals("20", m.group(2));
    }

    @Test
    public void testTimeConversions(){
        assertEquals("03:03:03", new Time("03:03:03").toString());
        assertEquals("1-03:03:03", new Time("1-03:03:03").toString());
        assertEquals("1-03:03:00", new Time("1-03:03").toString());
        assertEquals("00:00:00", new Time("0").toString());
    }

}

