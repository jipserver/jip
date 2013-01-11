package jip.jobs;

/**
 * Available job states
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public enum JobState {
    /**
     * Job submitted to the JIP queue
     */
    Submitted(false),

    /**
     * Job in cluster queue and waiting for execution
     */
    Queued(false),

    /**
     * Job in cluster queue and waiting for execution
     */
    Running(false),

    /**
     * Job finished successfully
     */
    Done(true),

    /**
     * Job finished not in fail state
     */
    Failed(true),

    /**
     * Job was canceled
     */
    Canceled(false),

    /**
     * Job on hold
     */
    Hold(false);
    /**
     * State is a finished state
     */
    private boolean doneState;

    private JobState(boolean doneState) {
        this.doneState = doneState;
    }

    public boolean isDoneState(){
        return doneState;
    }
}
