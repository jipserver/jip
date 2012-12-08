package jip.dsl

import com.google.common.io.Files

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ToolInstallerTest extends GroovyTestCase {

    public void testInstallingFromClosure(){
        File dir = Files.createTempDir()
        try{
            def dsl = new JipDSL()
            // test error on instantiation without exec
            dsl.evaluateToolDefinition("""
            installer "test-installer", {
                exec { dir ->
                    new File(dir, "testfile").write("Content")
                    return "hellp"
                }
                check { dir->
                    return new File(dir, "testfile").exists()
                }
            }
            """, [:])
            assert dsl.context.installer["test-installer"].isInstalled(dir) == false
            dsl.context.installer["test-installer"].install(dir)
            assert dsl.context.installer["test-installer"].isInstalled(dir) == true
        }finally {
            "rm -Rf ${dir.absolutePath}".execute().waitFor()
        }
    }

    public void testInstallingFromScript(){
        File dir = Files.createTempDir()
        try{
            def dsl = new JipDSL()
            // test error on instantiation without exec
            dsl.evaluateToolDefinition("""
            installer "test-installer", {
                exec "touch testfile"
                check "ls testfile"
            }
            """, [:])
            def installed = dsl.context.installer["test-installer"].isInstalled(dir)
            assert installed == false
            dsl.context.installer["test-installer"].install(dir)
            assert dsl.context.installer["test-installer"].isInstalled(dir) == true
        }finally {
            "rm -Rf ${dir.absolutePath}".execute().waitFor()
        }
    }
}
