package jip.tools;

import jip.plugin.ExtensionPoint;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Basic tool interface that describes something executable
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@ExtensionPoint
public interface Tool {
    /**
     * Return the name of the tool
     *
     * @return name the name of the tool
     */
    String getName();

    /**
     * The tool version
     *
     * @return version the tools version
     */
    String getVersion();

    /**
     * Get short description of the tool
     *
     * @return description a short description of the tool or empty string
     */
    String getDescription();

    /**
     * Run the tool
     *
     * @param workingDir working directory
     * @param cfg run configuration
     * @throws Exception in case of an exception
     */
    void run(File workingDir, Map cfg) throws Exception;

    /**
     * Get map of tool parameter
     *
     * @return parameter the tool parameter
     */
    Map<String, Parameter> getParameter();

    /**
     * Get default input parameter
     *
     * @return input the default input parameter
     */
    String getDefaultInput();

    /**
     * Get default output parameter
     *
     * @return output the default output parameter
     */
    String getDefaultOutput();

    /**
     * Get installer that this tool needs
     *
     * @return installer the installer that this tool needs
     */
    List<String> getInstaller();

    /**
     * Get the tools default execute environment
     *
     * @return the tools default execute environment
     */
    ExecuteEnvironment getExecuteEnvironment();

}
