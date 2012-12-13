package jip.dsl

import jip.tools.DefaultInstaller
import jip.utils.ExecuteDelegate

/**
 * Basic installer delegate for dsl
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class InstallerDelegate {
    private DefaultInstaller installer

    InstallerDelegate(DefaultInstaller installer) {
        this.installer = installer
    }

    def propertyMissing(String name) {
        if (name == "exec"){
            return installer.getClosure()
        }
        return installer."${name}"
    }

    def propertyMissing(String name, def arg) {
        if (name == "exec"){
            exec(arg)
        }else if (name == "check"){
            check(arg)
        }else{
            installer."${name}" = arg
        }
    }

    void description(String description){
        installer.setDescription(description)
    }

    void exec(String exec){
        Closure c = { cfg->
            bash(exec, [args:[cfg], cwd:cfg])
        }
        installer.setClosure(c)

    }

    void exec(Closure exec){
        installer.setClosure(exec)
    }

    void check(String check){
        Closure c = { dir->
            bash(check, [args:[dir], cwd: dir])
        }
        installer.setCheck(c)
    }

    void check(Closure check){
        installer.setCheck(check)

    }

}
