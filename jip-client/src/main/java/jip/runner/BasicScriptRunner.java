package jip.runner;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Execute jip jobs
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class BasicScriptRunner {
    /**
     * Possibel interpreters
     */
    public static enum Interpreter{
        /**
         * Bash interpreter
         */
        bash,
        /**
         * Python interpreter
         */
        python,
        /**
         * Perl interpreter
         */
        perl,
        /**
         * Execute java jar
         */
        jar,
    }

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(BasicScriptRunner.class);
    /**
     * The name of this runner
     */
    private String name;
    /**
     * The interpreter used to run the script
     */
    private final Interpreter interpreter;
    /**
     * The script to be executed
     */
    private final String script;
    /**
     * The arguments passed to the script
     */
    private final String[] arguments;
    /**
     * The script environment
     */
    private final Map<String, String> environment;
    /**
     * The scrip runs standard out
     */
    private final OutputStream stdout;
    /**
     * The scrip runs standard error
     */
    private final OutputStream stderr;

    /**
     * Job Runtime in milliseconds
     */
    private long runTime = -1;
    /**
     * Additional interpreter options
     */
    private String[] interpreterOptions;

    /**
     * the working directory
     */
    private String workingDir;



    /**
     * Create a new runner that will execute the given script using the interpreter
     * in the specified environment. If arguments are specified, the are passt on to the
     * script.
     *
     * @param name name of this run
     * @param interpreter interpreter the interpreter to run the script
     * @param script the script
     * @param arguments the arguments passed to the script
     * @param environment the script environment
     * @param stdout the runs standard output
     * @param stderr the runs standard error
     */
    public BasicScriptRunner(String name,
                             Interpreter interpreter,
                             String[] interpreterOptions,
                             String script,
                             String[] arguments,
                             Map<String, String> environment,
                             OutputStream stdout,
                             OutputStream stderr) {
        this(name, interpreter, interpreterOptions, script, arguments, environment, stdout, stderr, null);
    }

    /**
     * Create a new runner that will execute the given script using the interpreter
     * in the specified environment. If arguments are specified, the are passt on to the
     * script.
     *
     * @param name name of this run
     * @param interpreter interpreter the interpreter to run the script
     * @param script the script
     * @param arguments the arguments passed to the script
     * @param environment the script environment
     * @param stdout the runs standard output
     * @param stderr the runs standard error
     */
    public BasicScriptRunner(String name,
                             Interpreter interpreter,
                             String[] interpreterOptions,
                             String script,
                             String[] arguments,
                             Map<String, String> environment,
                             OutputStream stdout,
                             OutputStream stderr,
                             String workingDir) {

        if(name == null) name = "Script";
        if(interpreter == null) throw new NullPointerException("NULL interpreter not permitted");
        if(script == null) throw new NullPointerException("NULL script not permitted");
        this.name = name;
        this.interpreter = interpreter;
        this.interpreterOptions = interpreterOptions;
        this.script = script;
        this.arguments = arguments;
        this.environment = environment;
        this.stdout = stdout;
        this.stderr = stderr;
        this.workingDir = workingDir;
    }

    /**
     * Run this job
     *
     * @throws Exception in case an error occurs during the rob run
     * @return exitValue the scripts exit code
     */
    public int run() throws Exception{
        File scriptFile = null;
        try{
            List<String> parameter = new ArrayList<String>();
            // add the interpreter
            switch (interpreter){
                case jar: parameter.add("java"); break;
                default: parameter.add(interpreter.toString());
            }
            // add options
            if(interpreterOptions != null){
                parameter.addAll(Arrays.asList(interpreterOptions));
            }

            if(interpreter != Interpreter.jar){
                // script file and append it
                scriptFile = writeScript();
                parameter.add(scriptFile.getAbsolutePath());
                logger.debug("[{}] : Written script to {}", scriptFile.getAbsolutePath(), name);
            }else{
                // add script as name of the jar
                parameter.add("-jar");
                parameter.add(script);
            }


            if(arguments != null){
                parameter.addAll(Arrays.asList(arguments));
            }

            logger.debug("[{}] : Starting run -> {}", name, Joiner.on(" ").join(parameter));
            ProcessBuilder pb = new ProcessBuilder(parameter);
            if(environment != null){
                Map<String, String> pbe = pb.environment();
                if(environment.containsKey("CWD")) {
                    File cwd = new File(environment.get("CWD"));
                    pb.directory(cwd);
                    logger.debug("[{}] : Set working directory {}", name, cwd.getAbsolutePath());
                }
                for (Map.Entry<String, String> ee : environment.entrySet()) {
                    if(ee.getValue() != null){
                        pbe.put(ee.getKey(), ee.getValue().toString());
                    }
                    logger.debug("[{}] : Environment {}->{}", new String[]{name, ee.getKey(), ee.getValue()});
                }
            }

            if(workingDir != null){
                pb.directory(new File(workingDir));
            }
            long startTime = System.currentTimeMillis();
            Process process = pb.start();
            // read from process stdin and stdout
            ProcessStreamReader inReader = new ProcessStreamReader(process.getInputStream(), stdout, false);
            inReader.start();
            ProcessStreamReader errReader = new ProcessStreamReader(process.getErrorStream(), stderr, true);
            errReader.start();
            int exitValue = process.waitFor();
            inReader.join();
            errReader.join();
            runTime = System.currentTimeMillis()-startTime;
            logger.info("[{}] : Script finished with {}", name, exitValue);
            return exitValue;
        }catch (Exception error){
            logger.error("[{}] : Error running script : {}", name, error.getMessage());

            throw error;
        }finally {
            if(scriptFile != null) {
                scriptFile.delete();
            }
        }
    }

    /**
     * Get the run time in milli seconds
     * or return -1
     *
     * @return time runtime in milli seconds or -1
     */
    public long getRunTime() {
        return runTime;
    }

    /**
     * Write the script to a temporary file that
     * is deleted with VM termination
     */
    private File writeScript() throws IOException {
        File scriptFile = File.createTempFile("jip", ".job");
        scriptFile.deleteOnExit();
        Files.write(this.script.getBytes(), scriptFile);
        return scriptFile;
    }

    class ProcessStreamReader extends Thread{
        private final OutputStream originalOutput;
        private final BufferedReader stream;
        private final BufferedOutputStream output;
        private final boolean errorStream;
        private final boolean isSystemOut;

        public ProcessStreamReader(InputStream stream, OutputStream output, boolean isErrorStream) {
            this.stream = new BufferedReader(new InputStreamReader(stream));
            this.output = output == null ? null : new BufferedOutputStream(output);
            errorStream = isErrorStream;
            this.originalOutput = output;
            this.isSystemOut = originalOutput == System.err || originalOutput == System.out;
        }

        @Override
        public void run() {
            String line = null;
            try {
                boolean first = true;
                while ((line = stream.readLine()) != null){
                    if(!isSystemOut){
                        if(errorStream){
                            logger.error("[{}] : {}", name, line);
                        }else{
                            logger.info("[{}] : {}", name, line);
                        }
                    }
                    if(output != null){
                        if(first){
                            first = false;
                        }else{
                            output.write('\n');
                            output.flush();
                        }
                        output.write(line.getBytes());
                    }
                }
            } catch (Exception e) {
                logger.error("[{}] : Error while reading from stream {}", name, e.getMessage());
            }finally {
                if(output != null){try {
                    if(originalOutput == System.out || originalOutput == System.err){
                        output.write('\n');
                    }
                    output.flush();
                } catch (IOException ignore) {}}
                try {stream.close();} catch (IOException ignore) {}
                if(output != null && originalOutput != System.out && originalOutput != System.err) try {output.close();} catch (IOException e) {}
            }

        }
    }
}
