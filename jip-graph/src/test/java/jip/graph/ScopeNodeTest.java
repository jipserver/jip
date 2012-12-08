package jip.graph;

import jip.graph.ScopeNode;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class ScopeNodeTest {
    @Test
    public void testSimpleScopeNoResolve() throws Exception {
        ScopeNode scope = new ScopeNode("MyScope");
        scope.setValue("MyValue");
        assertEquals("MyValue", scope.get("${MyScope}"));
        assertEquals("lulu MyValue_lala", scope.get("lulu ${MyScope}_lala"));
    }

    @Test
    public void testSimpleScopefileProperties() throws Exception {
        ScopeNode scope = new ScopeNode("file");
        scope.setValue("/my/directory/structure.txt");
        assertEquals("/my/directory/structure.txt", scope.get("${file}"));
        assertEquals("structure", scope.get("${file.name}"));
        assertEquals("/my/directory/", scope.get("${file.parent}"));
        assertEquals("txt", scope.get("${file.extension}"));
    }
    @Test
    public void testSimpleScopefilePropertiesInList() throws Exception {
        ScopeNode scope = new ScopeNode("file");
        scope.setValue(Arrays.asList("/my/directory/structure.txt", "/my/directory/structure2.txt"));

        assertEquals(Arrays.asList("/my/directory/structure.txt", "/my/directory/structure2.txt"), scope.get("${file}"));
        assertEquals(Arrays.asList("structure", "structure2"), scope.get("${file.name}"));
        assertEquals(Arrays.asList("/my/directory/", "/my/directory/"), scope.get("${file.parent}"));
        assertEquals(Arrays.asList("txt", "txt"), scope.get("${file.extension}"));
    }

    @Test
    public void testThatNameAndExtensionAreResolvedProperly() throws Exception {
        // test fot JIP-99
        ScopeNode scope = new ScopeNode("file");
        scope.setValue("/my/directory/structure.txt.a.b");
        assertEquals("/my/directory/structure.txt.a.b", scope.get("${file}"));
        assertEquals("structure", scope.get("${file.name}"));
        assertEquals("/my/directory/", scope.get("${file.parent}"));
        assertEquals("txt.a.b", scope.get("${file.extension}"));

    }

    @Test
    public void testSimpelList() throws Exception {
        ScopeNode scope = new ScopeNode("MyScope");
        scope.setValue(Arrays.asList("A", "B"));
        assertEquals(Arrays.asList("A", "B"), scope.get("${MyScope}"));
    }
    @Test
    public void testListExpansion() throws Exception {
        ScopeNode scope = new ScopeNode("MyScope");

        ScopeNode a = scope.createChild("a");
        ScopeNode b = scope.createChild("b");
        ScopeNode d = scope.createChild("d");
        a.setValue("mya");
        b.setValue(Arrays.asList("${MyScope.a}", "${MyScope.a}_2"));
        d.setValue("prefix_${MyScope.b}");
        assertEquals(Arrays.asList("mya", "mya_2"), scope.get("${MyScope.b}"));
        assertEquals(Arrays.asList("prefix_mya", "prefix_mya_2"), scope.get("${MyScope.d}"));
    }
    @Test
    public void testListExpansionNumbers() throws Exception {
        ScopeNode scope = new ScopeNode("MyScope");

        ScopeNode a = scope.createChild("a");
        ScopeNode b = scope.createChild("b");
        a.setValue(1);
        b.setValue(Arrays.asList("${MyScope.a}", "${MyScope.a}_2"));
        assertEquals(Arrays.asList(1, "1_2"), scope.get("${MyScope.b}"));
    }

    @Test
    public void testSelfReverence() throws Exception {
        ScopeNode scope = new ScopeNode("MyScope");
        scope.setValue("${MyScope}");
        try{
            scope.get("${MyScope}");
            fail();
        }catch(Exception e){
            assertEquals("Variable MyScope has a reference to itself", e.getMessage());
        }

    }

    @Test
    public void testSimplePath() throws Exception {
        ScopeNode scope = new ScopeNode("Root");
        ScopeNode child = scope.createChild("child");
        child.setValue("MyValue");
        assertEquals("MyValue", scope.get("${Root.child}"));
        try{
            scope.get("${MyScope}");
            fail();
        }catch(Exception e){
            assertEquals("Variable MyScope not found", e.getMessage());
        }

    }
    @Test
    public void testReference() throws Exception {
        ScopeNode scope = new ScopeNode("Root");
        ScopeNode child = scope.createChild("child1");
        child.setValue("MyValue1");
        ScopeNode child2 = scope.createChild("child2");
        child2.setValue("${Root.child1}");

        assertEquals("MyValue1", scope.get("${Root.child2}"));
        assertEquals("MyValue1", child2.get("${child2}"));
    }

    @Test
    public void testScopeWalkingReference() throws Exception {
        ScopeNode scope = new ScopeNode("Root");
        ScopeNode c1 = scope.createChild("c1");
        ScopeNode c2 = c1.createChild("c2");
        ScopeNode c3 = c2.createChild("c3");
        c1.createValue("c11", "Child1-v1");
        c1.createValue("c12", "Child1-v2");
        c2.createValue("c21", "Child2-v1");
        c2.createValue("c22", "Child2-v2");
        c3.createValue("c31", "Child3-v1");
        c3.createValue("c32", "Child3-v2");

        assertEquals("Child3-v1", c3.get("${c31}"));
        assertEquals("Child2-v1", c3.get("${c2.c21}"));
        assertEquals("Child2-v1", c3.get("${c1.c2.c21}"));
    }


    @Test
    public void testSetValues() throws Exception {
        ScopeNode scope = new ScopeNode("Root");
        ScopeNode c1 = scope.createChild("c1");
        ScopeNode c2 = c1.createChild("c2");
        ScopeNode c3 = c2.createChild("c3");
        c1.createValue("c11", "Child1-v1");
        c1.createValue("c12", "Child1-v2");
        c2.createValue("c21", "Child2-v1");
        c2.createValue("c22", "Child2-v2");
        c3.createValue("c31", "Child3-v1");
        c3.createValue("c32", "Child3-v2");

        assertEquals("Child3-v1", c3.get("${c31}"));
        assertEquals("Child2-v1", c3.get("${c2.c21}"));
        assertEquals("Child2-v1", c3.get("${c1.c2.c21}"));
        scope.set("${c1.c2.c21}", "newvalue");
        assertEquals("newvalue", c3.get("${c1.c2.c21}"));
        c3.set("${c2.c21}", "newvalue2");
        assertEquals("newvalue2", c3.get("${c2.c21}"));
        assertEquals("newvalue2", c3.get("${c1.c2.c21}"));
    }

    @Test
    public void testSetValuesForSource() throws Exception {
        ScopeNode scope = new ScopeNode("Root");
        ScopeNode c1 = scope.createChild("c1");
        ScopeNode c2 = c1.createChild("c2");
        ScopeNode c3 = c2.createChild("c3");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("A", "Child3-v2");
        Dummy dummy = new Dummy("D");
        c1.createValue("c11", "Child1-v1");
        c1.createValue("c12", "Child1-v2");
        c2.createValue("c21", "Child2-v1");
        c2.createValue("c22", "Child2-v2");
        c3.createValue ("c31", "Child3-v1", dummy, "name");
        c3.createValueFromSource("name", dummy);
        c3.createValue("c32", "Child3-v2", map, "A");

        assertEquals("Child3-v1", c3.get("${c31}"));
        assertEquals("Child2-v1", c3.get("${c2.c21}"));
        assertEquals("Child2-v1", c3.get("${c1.c2.c21}"));
        scope.set("${c1.c2.c21}", "newvalue");
        assertEquals("newvalue", c3.get("${c1.c2.c21}"));
        c3.set("${c2.c21}", "newvalue2");
        assertEquals("newvalue2", c3.get("${c2.c21}"));
        assertEquals("newvalue2", c3.get("${c1.c2.c21}"));

        scope.set("${c1.c2.c3.c32}", "B");
        scope.set("${c1.c2.c3.c31}", "E");
        assertEquals("B", map.get("A"));
        assertEquals("E", dummy.name);
        scope.set("${c1.c2.c3.name}", "lala");
        assertEquals("lala", dummy.name);

    }

    class Dummy{
        private String name;

        public Dummy(String d) {
            this.name = d;
        }
    }




}
