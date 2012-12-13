package jip.utils;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import jip.tools.Tool;

import java.io.StringWriter;
import java.util.Map;

/**
 * Utility methods for templates
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class Templates {

    /**
     * Fill the given script template based
     *
     * @param tool the tool
     * @param script the script
     * @param cfg the configuration
     * @return filled the filled template
     * @throws Exception in case of an error
     */
    public static String fillTemplate(Tool tool, String script, Map cfg) throws Exception {
        GStringTemplateEngine templateEngine = new GStringTemplateEngine();
        Template template = templateEngine.createTemplate(script);
        if(tool != null){
            for (String p : tool.getParameter().keySet()) {
                if (!cfg.containsKey(p)) {
                    Object defaultValue = tool.getParameter().get(p).getDefaultValue();
                    cfg.put(p, defaultValue != null ? defaultValue : "");
                }
            }
        }
        StringWriter writer = new StringWriter();
        template.make(cfg).writeTo(writer);
        return writer.toString();
    }

}
