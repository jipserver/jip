package jip.jobs;

import java.util.Date;

/**
 * Jip Job message that can be send by a running job.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface Message extends Comparable<Message>{

    /**
     * Return the type of this message
     *
     * @return type the message type
     */
    public MessageType getType();

    /**
     * Get the message
     *
     * @return message the message
     */
    public String getMessage();

    /**
     * Create date of this message
     *
     * @return date the create date of this message
     */
    public Date getCreateDate();
}
