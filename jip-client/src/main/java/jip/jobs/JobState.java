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
    Submitted,

    /**
     * Job in cluster queue and waiting for execution
     */
    Queued,

    /**
     * Job finished successfully
     */
    Done,

    /**
     * Job finished not in fail state
     */
    Failed,

    /**
     * Job was canceled
     */
    Canceled,

    /**
     * Job on hold
     */
    Hold,
}
