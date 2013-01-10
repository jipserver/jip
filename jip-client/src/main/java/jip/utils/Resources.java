package jip.utils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Helper class to load resources from the classpath
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class Resources {

    /**
     * Load text from UTF-8 encoded file in classpath
     * @param resource
     * @return
     */
    public static String text(String resource){
        try {
            URL url = Resources.class.getResource(resource);
            if(url == null){
                throw new RuntimeException("Resources " + resource + " not found!");
            }
            return com.google.common.io.Resources.toString(url, Charset.forName("UTF8"));
        } catch (IOException e) {
            throw new RuntimeException("Error while loading resource " + resource + " : " + e.getMessage(), e);
        }
    }
}
