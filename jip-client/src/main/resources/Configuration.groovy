
/**
 * Default jip configuration
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */

jobs{
    idservice{
        service = "jip.jobs.FileIdService"
        /**
         * Storage location of the id file
         */
        file = "${jip.userdir}/ids"
    }
    storage{
        store = "jip.jobs.FileJobStore"
        directory = "${jip.userdir}/jobs"
    }


}