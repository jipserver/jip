package jip.dsl

import jip.tools.DefaultExecuteEnvironment
import jip.utils.Time

import java.util.regex.Pattern

/**
 * Delegates to a DefaultExecuteEnvironment
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class ExecuteEnvironmentDelegate {
    /**
     * Parse memory strings
     */
    private static Pattern memoryPattern = Pattern.compile("(\\d+)([mMgG])?")
    /**
     * The environment to delegate to
     */
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
        }else if (name == "priority"){
            priority(arg)
            return
        }else if (name == "queue"){
            queue(arg)
            return
        }
        throw new NoSuchFieldException(name)
    }

    void time(Number time){
        env.setMaxTime(new Time("00:"+time+":00").time)
    }

    void time(String time){
        if (time != null){
            env.setMaxTime(new Time(time).time)
        }
    }

    void memory(long memory){
        env.setMaxMemory(memory)
    }

    void memory(String memory){
        if(memory == null){
            env.setMaxMemory(0)
            return;
        }
        def matcher = memoryPattern.matcher(memory)
        if (matcher.matches()){
            long mem = Long.parseLong(matcher.group(1))
            if (matcher.groupCount() == 2){
                if (matcher.group(2).toLowerCase() == "g"){
                    mem = mem * 1024
                }
            }
            env.setMaxMemory(mem)
        }else{
            throw new RuntimeException("Unable to parse memory expression " + memory)
        }
    }

    void threads(int threads){
        env.setThreads(threads)
    }

    void queue(String queue){
        env.setQueue(queue)
    }

    void priority(String priority){
        env.setPriority(priority)
    }
}
