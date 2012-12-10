package jip.dsl

import com.google.common.io.Files
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test execute utils
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ExecuteUtilsTest {

    File tmpDir
    ExecuteUtils utils

    @Before
    public void setUp() throws Exception {
        tmpDir = Files.createTempDir()
        utils = new ExecuteUtils(tmpDir, null)
    }

    @After
    public void tearDown() throws Exception {
        "rm -Rf ${tmpDir.absolutePath}".execute().waitFor()
    }

    @Test
    public void testSimpleBashRun() throws Exception {
        assert !new File(tmpDir, "testfile").exists()
        def c = {
            bash("touch testfile")
        }
        c.delegate = utils
        c()
        assert new File(tmpDir, "testfile").exists()
    }

    @Test
    public void testPythonRun() throws Exception {
        assert !new File(tmpDir, "testfile").exists()
        def c = {
            python("""
import os
def touch(fname, times=None):
    fhandle = file(fname, 'a')
    try:
        os.utime(fname, times)
    finally:
        fhandle.close()

if __name__ == "__main__":
    touch("testfile")
""")
        }
        c.delegate = utils
        c()
        assert new File(tmpDir, "testfile").exists()
    }

}
