package jip.commands;

import com.google.inject.Inject;
import jip.CLIHelper;
import jip.jobs.*;
import jip.plugin.Extension;
import jip.utils.Resources;
import jip.utils.SimpleTablePrinter;
import jip.utils.Time;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

/**
 * List show and manage jobs
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Extension
public class JobsCommand implements JipCommand{
    /**
     * The logger
     */
    private static final Logger log = LoggerFactory.getLogger(JobsCommand.class);

    /**
     * The run service
     */
    private RunService runService;

    /**
     * The job store
     */
    private JobStore jobStore;

    @Inject
    public JobsCommand(RunService runService, JobStore jobStore) {
        this.runService = runService;
        this.jobStore = jobStore;
    }

    @Override
    public String getCommandName() {
        return "jobs";
    }

    @Override
    public String getShortDescription() {
        return "Manage jobs";
    }

    @Override
    public String getLongDescription() {
        return Resources.text("/help/commands/jobs.txt");
    }

    @Override
    public void run(String[] args, Namespace parsed) {
        if(parsed.getBoolean("check-jobs")){
            log.info("Checking job status");
            runService.checkJobs();
            return;
        }


        List<Object> jobIdList = parsed.getList("job");
        List<Object> deleteList = parsed.getList("delete");
        List<Object> cancelList = parsed.getList("cancel");


        if(jobIdList != null && jobIdList.size() > 0
           || deleteList != null && deleteList.size() > 0
           || cancelList != null && cancelList.size() > 0){

            if(jobIdList != null && jobIdList.size() > 0){
                showJobDetails(CLIHelper.parseRange(jobIdList));
            }

            if(deleteList != null && deleteList.size() > 0){
                deleteJobs(CLIHelper.parseRange(deleteList));
            }
            if(cancelList != null && cancelList.size() > 0){
                cancelJobs(CLIHelper.parseRange(cancelList));
            }
        }else{
            // list jobs
            Iterable<PipelineJob> jobList = jobStore.list(parsed.getBoolean("list-archived"));
            SimpleTablePrinter jobTable = new SimpleTablePrinter(Arrays.asList(
                    "ID",
                    "Name",
                    "State",
                    "Progress",
                    "Time",
                    "Last Message"));
            for (PipelineJob pipelineJob : jobList) {
                Map<JobState, Integer> counts = getJobStateMap(pipelineJob);
                JobState state = getState(counts);
                jobTable.addRow(
                        pipelineJob.getId(),
                        pipelineJob.getName(),
                        state,
                        counts.get(JobState.Done)+"/"+pipelineJob.getJobs().size(),
                        getPipelineJobTime(pipelineJob, state),
                        getLastMessage(pipelineJob)
                );
            }
            System.out.println(jobTable.toString());
        }
    }

    private String getLastMessage(PipelineJob pipelineJob) {
        List<Message> messages = new ArrayList<Message>();
        for (Job job : pipelineJob.getJobs()) {
            messages.addAll(job.getMessages());
        }
        Collections.sort(messages);
        if(messages.size() == 0) return "";
        return messages.get(messages.size()-1).getMessage();
    }

    private String getPipelineJobTime(PipelineJob pipelineJob, JobState state) {
        long start = Long.MAX_VALUE;
        long end = System.currentTimeMillis()/1000;
        boolean useStart = state.isDoneState() || state == JobState.Running;
        for (Job job : pipelineJob.getJobs()) {
            JobStats jobStats = job.getJobStats();
            if(jobStats.getStartDate() != null){
                start = Math.min(start, (useStart ? jobStats.getStartDate().getTime() : jobStats.getCreateDate().getTime())/1000);
            }else{
                start = jobStats.getCreateDate().getTime()/1000;
            }
            if(jobStats.getEndDate() != null){
                end = jobStats.getEndDate().getTime()/1000;
            }
        }
        return new Time(end-start).toString();
    }

    private void showJobDetails(List<Long> jobIdList) {
        // jobs jobs
        SimpleTablePrinter table = new SimpleTablePrinter(Arrays.asList(
                "Pipeline",
                "ID",
                "Remote ID",
                "State",
                "Time",
                "Max-Time",
                "Progress",
                "Message",
                "State Reason"

        ));

        for (Long jobId : jobIdList){
            addPipelineJobToTable(jobId + "", table);
        }
        System.out.println(table);
    }

    private void deleteJobs(List<Long> ids) {
        for (Long id : ids) {
            PipelineJob job = jobStore.get(id + "");
            if(job != null){
                Map<JobState, Integer> counts = getJobStateMap(job);
                JobState state = getState(counts);
                if(!state.isDoneState()){
                    // cancel first
                    runService.cancel(job);
                }
                jobStore.delete(job);
            }
        }
    }

    private void cancelJobs(List<Long> ids) {
        for (Long id : ids) {
            PipelineJob job = jobStore.get(id + "");
            if(job != null){
                Map<JobState, Integer> counts = getJobStateMap(job);
                JobState state = getState(counts);
                if(!state.isDoneState()){
                    runService.cancel(job);
                }
            }
        }
    }

    private void addPipelineJobToTable(String pipelineJobId, SimpleTablePrinter table) {
        PipelineJob pipelineJob = jobStore.get(pipelineJobId);
        for (Job job : pipelineJob.getJobs()) {
            JobStats jobStats = job.getJobStats();
            String time = "";
            long endTime = System.currentTimeMillis();
            long startTime;
            if(jobStats.getEndDate() != null){
                endTime = jobStats.getEndDate().getTime() / 1000;
            }
            if(jobStats.getStartDate() != null){
                startTime = jobStats.getStartDate().getTime() / 1000;
            }else{
                startTime = jobStats.getCreateDate().getTime() / 1000;
            }

            time = new Time(endTime-startTime).toString();
            table.addRow(
                    job.getPipelineId(),
                    job.getId(),
                    job.getRemoteId(),
                    job.getState(),
                    time,
                    new Time(job.getExecuteEnvironment().getMaxTime()).toString(),
                    job.getProgress() > 0 ? job.getProgress() : "",
                    job.getMessages().size() > 0 ? job.getMessages().get(job.getMessages().size()-1).getMessage(): "",
                    job.getStateReason()
            );
        }
    }

    private JobState getState(Map<JobState, Integer> counts) {
        if(counts.get(JobState.Failed) > 0) return JobState.Failed;
        if(counts.get(JobState.Canceled) > 0) return JobState.Canceled;
        if(counts.get(JobState.Hold) > 0) return JobState.Hold;
        if(counts.get(JobState.Running) > 0) return JobState.Running;
        if(counts.get(JobState.Submitted) > 0) return JobState.Submitted;
        if(counts.get(JobState.Queued) > 0) return JobState.Queued;
        return JobState.Done;
    }

    private Map<JobState, Integer> getJobStateMap(PipelineJob pipelineJob) {
        Map<JobState, Integer> counts = new HashMap<JobState, Integer>();
        for (JobState jobState : JobState.values()) {
            counts.put(jobState, 0);
        }
        for (Job job : pipelineJob.getJobs()) {
            Integer count = counts.get(job.getState());
            counts.put(job.getState(), count+1);
        }
        return counts;
    }

    @Override
    public void populateParser(Subparser parser) {
        parser.addArgument("-j", "--job").dest("job").nargs("*").type(String.class).help("List of job ids");
        parser.addArgument("-d", "--delete").dest("delete").nargs("*").type(String.class).help("Delete jobs");
        parser.addArgument("-c", "--cancel").dest("cancel").nargs("*").type(String.class).help("Cancel Jobs");
        parser.addArgument("--list-archived").dest("list-archived").action(storeTrue()).setDefault(false).help("List archived jobs");
        parser.addArgument("--check").dest("check-jobs").action(storeTrue()).setDefault(false).help("Check remote jobs and perform cleanup");
    }
}
