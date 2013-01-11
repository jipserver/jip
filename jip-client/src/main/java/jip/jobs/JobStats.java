package jip.jobs;

import java.util.Date;

/**
 *
 * Pipeline and jobs stats
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface JobStats {
    /**
     * Get the create date
     *
     * @return createDate the create date
     */
    Date getCreateDate();
    /**
     * Get the start date
     *
     * @return startDate the start date
     */
    Date getStartDate();

    /**
     * Get the end date
     *
     * @return endDate the end date
     */
    Date getEndDate();

    void setCreateDate(Date createDate);

    void setStartDate(Date startDate);

    void setEndDate(Date endDate);
}
