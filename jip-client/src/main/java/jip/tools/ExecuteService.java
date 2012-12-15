package jip.tools;

import java.io.File;
import java.util.Map;

/**
 *
 * Execute tools within the jip context
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface ExecuteService {

    public void run(String tool, Map<String, Object> configuration, File workingDir);
}
