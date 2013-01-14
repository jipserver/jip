package jip.utils;

import com.google.common.base.Joiner;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import jip.Jip;
import jip.jobs.Job;
import jip.jobs.PipelineJob;
import jip.tools.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
     * The logger
     */
    private static Logger log = LoggerFactory.getLogger(Templates.class);

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
        Template template = createTemplate(script);
        if(tool != null){
            for (String p : tool.getParameter().keySet()) {
                if (!cfg.containsKey(p)) {
                    Object defaultValue = tool.getParameter().get(p).getDefaultValue();
                    cfg.put(p, defaultValue != null ? defaultValue : "");
                }
            }
        }
        return fillTemplate(cfg, template);


    }

    private static Template createTemplate(String script) throws ClassNotFoundException, IOException {
        GStringTemplateEngine templateEngine = new GStringTemplateEngine();
        return templateEngine.createTemplate(script);
    }

    private static String fillTemplate(Map cfg, Template template) throws IOException {
        // make sure to have lists with space join toStrings()
        for (Object key : cfg.keySet()) {
            Object value = cfg.get(key);
            if(value instanceof List){
                cfg.put(key, new SpaceJoinToStringList((List)value));
            }
        }

        StringWriter writer = new StringWriter();
        template.make(new DelegateMap(cfg)).writeTo(writer);
        log.info("Template written:\n{}", writer);
        return writer.toString();
    }

    /**
     * Convert a script line into a jip job script
     *
     * @param script the script
     * @return jobScript the job script
     */
    public static String toJobScript(Job job, String script) {
        try {
            Template template = createTemplate(Resources.text("/jobs/script.template.txt"));
            Map cfg = new HashMap();
            File jipHome = Jip.getInstance().getJipHome(false);
            String path = jipHome + "/bin/jip";
            if(jipHome.getName().equals(".")){
                path = "jip";
            }
            cfg.put("jip", path);
            cfg.put("script", script);
            if(job != null){
                cfg.put("pipelineId", job.getPipelineId());
                cfg.put("jobId", job.getId());
            }
            return Templates.fillTemplate(cfg, template);
        } catch (Exception e) {
            log.error("Error while creating script template : {}", e.getMessage());
            throw new RuntimeException(e);
        }

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
