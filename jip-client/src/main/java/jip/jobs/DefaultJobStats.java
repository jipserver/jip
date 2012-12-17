package jip.jobs;

import java.util.Date;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultJobStats implements JobStats {
    private Date createDate;
    private Date startDate;
    private Date endDate;

    public DefaultJobStats() {
        this.createDate = new Date();
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
