package jip.graph;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class FileParameterTest{

    @Test
    public void testGetName() throws Exception {
        assertEquals("bla", new FileParameter("bla.blu").getName());
        assertEquals("bla", new FileParameter("bla.").getName());
        assertEquals("bla", new FileParameter("bla").getName());
        assertEquals("bla", new FileParameter("/tmp/bla").getName());
        assertEquals("bla", new FileParameter("/tmp/bla.blub").getName());
    }

    @Test
    public void testGetParent() throws Exception {
        assertEquals("", new FileParameter("bla.blu").getParent());
        assertEquals("", new FileParameter("bla.").getParent());
        assertEquals("", new FileParameter("bla").getParent());
        assertEquals("/tmp/", new FileParameter("/tmp/bla").getParent());
        assertEquals("/tmp/", new FileParameter("/tmp/bla.blub").getParent());
        assertEquals("abc/tmp/", new FileParameter("abc/tmp/bla.blub").getParent());

    }

    @Test
    public void testGetExtension() throws Exception {
        assertEquals("blu", new FileParameter("bal.blu").getExtension());
        assertEquals("", new FileParameter("bal.").getExtension());
        assertEquals("", new FileParameter("bal").getExtension());
        assertEquals("", new FileParameter("/tmp/bal").getExtension());
        assertEquals("blub", new FileParameter("/tmp/bal.blub").getExtension());
        assertEquals("blub", new FileParameter("abc/tmp/bal.blub").getExtension());

    }
}
