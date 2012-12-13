package jip.utils;

import com.google.common.base.Joiner;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import jip.tools.Tool;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // make sure to have lists with space join toStrings()
        for (Object key : cfg.keySet()) {
            Object value = cfg.get(key);
            if(value instanceof List){
                cfg.put(key, new SpaceJoinToStringList((List)value));
            }
        }

        StringWriter writer = new StringWriter();
        template.make(new DelegateMap(cfg)).writeTo(writer);
        return writer.toString();
    }

    /**
     * Helper class that creates a space joined string representation of lists
     *
     * @param <E> the value type
     */
    private static class SpaceJoinToStringList<E> extends ArrayList<E>{

        public SpaceJoinToStringList(List<E> value) {
            super(value);
        }

        @Override
        public String toString() {
            return Joiner.on(" ").join(this);
        }
    }

    /**
     * Resolve unknown entries to variable representation
     */
    public static class DelegateMap extends HashMap{

        public DelegateMap(Map cfg) {
            super(cfg);
        }

        @Override
        public Object get(Object o) {
            Object value = super.get(o);
            if(value == null){
                value = "${"+o.toString()+"}";
            }
            return value;
        }
    }

}
