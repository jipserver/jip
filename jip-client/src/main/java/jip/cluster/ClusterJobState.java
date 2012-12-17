package jip.cluster;

/**
 * Possible states of a grid job
 */
public enum ClusterJobState {
    /**
     * Initial state, job is submitted to JIP but not yet on the cluster
     */
    Submitted(false),
    /**
     * job is submitted to the cluster and waiting for execution
     */
    Queued(false),
    /**
     * Job is running
     */
    Running(false),
    /**
     * Job is completed successfully
     */
    Done(true),
    /**
     * Job failed
     */
    Error(true),
    /**
     * Job was canceled
     */
    Canceled(true),;

    /**
     * Indicates that this is a finished state
     */
    private boolean finishedState;

    ClusterJobState(boolean finishedState) {
        this.finishedState = finishedState;
    }

    /**
     * Jobs in this state are finished
     *
     * @return finished true if this state is a finished state
     */
    public boolean isFinishedState() {
        return finishedState;
    }

    /**
     * Opposite of finished state
     *
     * @return execution jobs in this state are still executing
     */
    public boolean isExecutionState(){
        return !isFinishedState();
    }

}
