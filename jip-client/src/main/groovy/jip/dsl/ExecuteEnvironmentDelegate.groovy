package jip.dsl

import jip.tools.DefaultExecuteEnvironment
import jip.utils.Time

/**
 * Delegates to a DefaultExecuteEnvironment
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ExecuteEnvironmentDelegate {
    private DefaultExecuteEnvironment env

    ExecuteEnvironmentDelegate(DefaultExecuteEnvironment env) {
        this.env = env
    }

    def propertyMissing(String name) {
        switch (name){
            case "time": return env.maxTime
            case "memory": return env.maxMemory
            case "threads": return env.threads
        }
        throw new NoSuchFieldException(name)
    }

    def propertyMissing(String name, def arg) {
        if (name == "time"){
            time(arg)
            return
        }else if (name == "memory"){
            memory(arg)
            return
        }else if (name == "threads"){
            threads(arg)
            return
        }
        throw new NoSuchFieldException(name)
    }

    void time(Number time){
        env.setMaxTime(new Time("00:"+time+":00").time)
    }

    void time(String time){
        env.setMaxTime(new Time(time).time)
    }

    void memory(long memory){
        env.setMaxMemory(memory)
    }

    void threads(int threads){
        env.setThreads(threads)
    }
}
