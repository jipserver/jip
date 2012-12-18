
/**
 * Default jip configuration
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */

jip{
    /**
     * The directory that contains the job
     * log files
     */
    logging{
        logfile = "${jip.userdir}/logs/jip.log"
        pattern = "[%-5p] [%t] [%d{dd MMM yyyy HH:mm:ss,SSS}] [%c{2}] : %m%n"
        log4j {
            rootLogger="info"
            logger {
                org.reflections = "fatal"
            }
        }
    }
}

jobs{
    // the id service provides
    // new job ids
    idservice{
        /**
         * The ID service that should be used
         */
        service = "jip.jobs.FileIdService"
        /**
         * Storage location of the id file
         */
        file = "${jip.userdir}/ids"
    }
    // configure the job storage environment
    storage{
        /**
         * The job store implementation
         * that should be used to persist
         * jobs
         */
        store = "jip.jobs.FileJobStore"
        /**
         * FileJobStore specific storage location for
         * job files
         */
        directory = "${jip.userdir}/jobs"
    }

    /**
     *
     */
}