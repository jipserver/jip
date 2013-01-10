package jip.cluster

import jip.jobs.Job
import jip.plugin.Extension
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Extension
class SlurmCluster implements Cluster{
    /**
     * Slurm grid type
     */
    public static final String TYPE = "slurm"

    /**
     * All possible slurm states and the mapping to the job state
     POSSIBLE SLURM STATES
     CA  CANCELLED       Job was explicitly cancelled by the user or system administrator.  The job may or may not have been initiated.
     CD  COMPLETED       Job has terminated all processes on all nodes.
     CF  CONFIGURING     Job has been allocated resources, but are waiting for them to become ready for use (e.g. booting).
     CG  COMPLETING      Job is in the process of completing. Some processes on some nodes may still be active.
     F   FAILED          Job terminated with non-zero exit code or other failure condition.
     NF  NODE_FAIL       Job terminated due to failure of one or more allocated nodes.
     PD  PENDING         Job is awaiting resource allocation.
     R   RUNNING         Job currently has an allocation.
     S   SUSPENDED       Job has an allocation, but execution has been suspended.
     TO  TIMEOUT         Job terminated upon reaching its time limit.
     */
    public static Map<String, ClusterJobState> STATE_MAP = new HashMap<String, ClusterJobState>(){{
        put("CANCELLED", ClusterJobState.Canceled);
        put("CA", ClusterJobState.Canceled);
        put("COMPLETED", ClusterJobState.Done);
        put("CD", ClusterJobState.Done);
        put("CONFIGURING", ClusterJobState.Queued);
        put("CF", ClusterJobState.Queued);
        put("COMPLETING", ClusterJobState.Queued);
        put("CG", ClusterJobState.Queued);
        put("PENDING", ClusterJobState.Queued);
        put("PD", ClusterJobState.Queued);
        put("FAILED", ClusterJobState.Error);
        put("F", ClusterJobState.Error);
        put("NODE_FAIL", ClusterJobState.Error);
        put("NF", ClusterJobState.Error);
        put("TIMEOUT", ClusterJobState.Error);
        put("TO", ClusterJobState.Error);
        put("RUNNING", ClusterJobState.Running);
        put("R", ClusterJobState.Running);
        put("SUSPENDED", ClusterJobState.Running); // todo : what are the transisitons for suspended ? do we need a special state ?
        put("S", ClusterJobState.Running);
    }};


    /**
     * General slurmd log pattern. If it matches, you will get three groups.
     * <pre>
     *     1. the node
     *     2. the job id
     *     3. the timestamp
     *     4. the message (optional, might be empty)
     * </pre>
     */
    public static final Pattern SLURM_LOG_PATTERN = Pattern.compile(".*slurmd\\[(.*)\\]: \\*\\*\\* JOB (\\d+) CANCELLED AT ([0-9\\-:T]+) (.*)\\*\\*\\*\$");
    /**
     * Date format for the slurm timestamp
     */
    public static final DateFormat LOG_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    /**
     * Job submit pattern
     */
    static Pattern SUBMIT_PATTERN = Pattern.compile(".*Submitted batch job (\\d+).*", Pattern.MULTILINE | Pattern.DOTALL);


    /**
     * The logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass())
    /**
     * Path to the sbatch command
     */
    String sbatch
    /**
     * Path to the scancel command
     */
    String scancel
    /**
     * Path to the squeue command
     */
    String squeue

    /**
     * The run directory
     */
    String directory;
    /**
     * The configuration
     */
    Map configuration

    SlurmCluster() {
        this([:])
    }
    SlurmCluster(Map attr) {
        this.sbatch = attr?.sbatch ? attr.sbatch : "sbatch"
        this.scancel = attr?.scancel ? attr.scancel : "scancel"
        this.squeue = attr?.squeue ? attr.squeue : "squeue"
        this.directory = attr?.directory ? attr.directory : "/tmp"
        this.configuration = attr;
    }

    @Override
    Map<String, ClusterJobState> list() throws Exception{
        def output = """${squeue} -h -o "%i %T %N %l %S""".execute()
        def result = parseSqueueOutput(output.inputStream)
        if (output.waitFor() != 0) {
            throw new RuntimeException("Slurm polling failed! Error Message: ${output.errorStream.text}");
        }
        return result;
    }

    @Override
    String getType() {
        return TYPE;
    }

    Map<String, ClusterJobState> parseSqueueOutput(InputStream output) throws IOException {
        log.debug("Parsing squeue output");
        final Map<String, ClusterJobState> states = new HashMap<String, ClusterJobState>();
        BufferedReader commandOutput = new BufferedReader(new InputStreamReader(output));
        String l = null;
        String[] split = null;
        while ((l = commandOutput.readLine()) != null) {
            if(l.startsWith("\"")) l = l.substring(1);
            if(l.endsWith("\""))l = l.substring(0, l.length()-1);
            split = l.split(" ");
            if (split.length != 5){
                log.warn("squeue output does not contain 5 fields: " + l);
                continue;
            }
            log.debug("squeue add grid job " + split[0]);
            states.put(split[0], STATE_MAP.get(split[1]));
        }
        commandOutput.close();
        return states;
    }


    @Override
    void cancel(List<Job> jobs) {
        def jobids = jobs.find{it.remoteId != null}.collect {it.remoteId}
        log.debug("Slurm cancel jobs ${jobids}")
        """${scancel} ${jobids.join(' ')}""".execute().waitFor()
    }

    @Override
    void submit(Job job) {
        def params = [[sbatch]]
        if(job.executeEnvironment){
            def environment = job.executeEnvironment
            if(environment.threads > 0) params<<['-c', "${environment.threads}"]
//                if(environment.nodes > 0) params<<["-N", "${environment.nodes}"]
//                if(environment.qos) params<<["--qos=${environment.qos}"]
//                if(environment.partition) params<<["-p", "${environment.partition}"]
            if(environment.maxMemory > 0) params<<["--mem-per-cpu=${environment.maxMemory}"]
//                if(environment.freeTempSpace > 0) params<<["--tmp=${environment.freeTempSpace}"]
            if(environment.maxTime && environment.maxTime > 0) params<<["-t", "${environment.maxTime}"]
//                if(environment.additionalProperties){
//                    params << environment.additionalProperties
//                }
        }

        // set log files

        if(!job.log){
            job.log = "${job.workingDirectory}/jip-${job.id}.out"
        }
        if(!job.errorLog){
            job.errorLog = "${job.workingDirectory}/jip-${job.id}.err"
        }
        params << ['-o', job.log, '-e', job.errorLog]

        if (job.dependenciesBefore && job.dependenciesBefore.size() > 0){
            params << ['-d', "afterok:${job.dependenciesBefore.collect {it.remoteId}.join(':')}"]
        }
        if (job.workingDirectory){
            params << ['-D', job.workingDirectory]
        }


        params << "jip execute ${job.id}"

        def process = params.flatten().join(" ").execute()
        def res = process.inputStream.text
        Matcher m = SUBMIT_PATTERN.matcher(res);
        if(process.waitFor() != 0 || !m.matches()){
            throw new RuntimeException("Unable to submit job : ${process.errorStream.text}")
        }
        def jobId = m.group(1)
        job.remoteId = jobId
    }

    @Override
    void hold(List<Job> jobs) throws Exception {
        throw new UnsupportedOperationException()
    }
}
