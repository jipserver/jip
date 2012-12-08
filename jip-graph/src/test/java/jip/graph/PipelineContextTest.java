package jip.graph;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import jip.graph.PipelineContext;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PipelineContextTest {

    @Test
    public void testGet() {
        PipelineContext ctx = new PipelineContext(null);

        A a = new A("Aname", "Adescription");
        B b = new B("Bname", "Bdescription", "better");
        Map<String, Object> mapData = new HashMap<String, Object>();
        mapData.put("some" ,"thing");
        mapData.put("anA" ,new A("a", "b"));
        mapData.put("anB" ,new B("c", "d", "e"));

        ctx.addEntry("a", a);
        ctx.addEntry("b", b);
        ctx.addEntry("map", mapData);


        assertEquals("Aname", ctx.get("${a.name}").toString());
        assertEquals("Adescription", ctx.get("${a.description}").toString());
        assertEquals("better", ctx.get("${b.betterName}").toString());
        assertEquals("Bname", ctx.get("${b.name}").toString());
        assertEquals("Bdescription", ctx.get("${b.description}").toString());
        assertEquals("thing", ctx.get("${map.some}").toString());
        assertEquals("a", ctx.get("${map.anA.name}").toString());
        assertEquals("b", ctx.get("${map.anA.description}").toString());
        assertEquals("c", ctx.get("${map.anB.name}").toString());
        assertEquals("d", ctx.get("${map.anB.description}").toString());
        assertEquals("e", ctx.get("${map.anB.betterName}").toString());

    }

    @Test
    public void testSelfResolve() {
        PipelineContext ctx = new PipelineContext(null);

        A a = new A("Aname", "Adescription");

        ctx.addEntry("a", a);
        ctx.addEntry("b", "${a.name}");


        assertEquals("Aname", ctx.get("${a.name}").toString());
        assertEquals("Adescription", ctx.get("${a.description}").toString());
        assertEquals("Aname", ctx.get("b").toString());
    }

    @Test
    public void testSplitter() {
        Matcher matcher = PipelineContext.SPLITTER.matcher("${a}${b}some${c}${a}");

        assertTrue(matcher.find());
        assertEquals("${a}", matcher.group());
        assertTrue(matcher.find());
        assertEquals("${b}", matcher.group());
        assertTrue(matcher.find());
        assertEquals("some", matcher.group());
        assertTrue(matcher.find());
        assertEquals("${c}", matcher.group());
        assertTrue(matcher.find());
        assertEquals("${a}", matcher.group());
        assertFalse(matcher.find());

        matcher = PipelineContext.SPLITTER.matcher("No variable in this term");

        assertTrue(matcher.find());
        assertEquals("No variable in this term", matcher.group());
        assertFalse(matcher.find());


    }

    static class A{
        private String name;
        private String description;
        public String bla;
        public static String blub;
        private static String blubPrivate;


        A(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    class B extends A{
        private String betterName;

        B(String name, String description, String betterName) {
            super(name, description);
            this.betterName = betterName;
        }

        public String getOther() {
            return "Other";
        }

        public String getBetterName() {
            return betterName;
        }
    }
}
