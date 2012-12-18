package jip.jobs;

import jip.plugin.ExtensionPoint;

/**
 *
 * Provide ids. Note that implementations must be both
 * threads safe and process safe as there might be multiple
 * instances and multiple processes accessing the id storage at
 * the same time.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@ExtensionPoint
public interface IdService {
    /**
     * Provide the next available id
     *
     * @return id next available id
     */
    public String next();
}
