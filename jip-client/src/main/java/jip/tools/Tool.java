package jip.tools;

import jip.plugin.ExtensionPoint;

import java.util.List;
import java.util.Map;

/**
 * Basic tool interface
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
     * Get short description of the tool
     *
     * @return description a short description of the tool or empty string
     */
    String getDescription();

    /**
     * Run the tool
     *
     * @param cfg run configuration
     * @throws Exception in case of an exception
     */
    void run(Map cfg) throws Exception;

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



}