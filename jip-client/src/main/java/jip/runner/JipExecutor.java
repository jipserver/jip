package jip.runner;

import groovy.lang.Closure;

import java.io.File;
import java.util.Map;

/**
 * Execute script with various interpreters and tools
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface JipExecutor {

    /**
     * Run a simple bash command/script
     */
    int bash(String script);
    /**
     * Run a simple bash command/script
     */
    int bash(String script, Closure closure);

    /**
     * Run a simple bash command/script
     */
    int python(String script);

    /**
     * Run a simple bash command/script
     */
    int python(String script, Closure closure);

    /**
     * Run a simple bash command/script
     */
    int pip(String pipPackage);

    /**
     * Run a simple bash command/script
     */
    int run(BasicScriptRunner.Interpreter interpreter, String script, Closure definition);
}
