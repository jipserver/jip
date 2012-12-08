/*
 * Copyright (C) 2012 Thasso Griebel
 *
 * This file is part of JIP.
 *
 * JIP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JIP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package jip.graph;

import java.util.*;

public class PipelineJob {
    /**
     * The execution ID, must be unique within the pipeline
     */
    private String id;
    /**
     * Configuration for this execution. This extends the
     * default configuration of an Executable
     */
    private Map<String, Object> configuration;

    /**
     * Manual parent dependencies using execution IDs
     */
    private List<String> after;

    /**
     * Parameters of this execution
     */
    private List<Parameter> parameters;

    /**
     * If this execution is a pipeline of its own, this is set
     */
    private Pipeline pipeline;

    /**
     * the tool id
     */
    private String toolId;

    /**
     * Default constructor
     */
    public PipelineJob(String id) {
        this(id, null);
    }

    /**
     * Default constructor
     */
    public PipelineJob(String id, String toolId) {
        this.setId(id);
        this.toolId = toolId;
    }


    public PipelineJob(PipelineJob source) {
        id = source.getId();
        this.toolId = source.toolId;
        configuration = new HashMap<String, Object>();
        if(source.getConfiguration() != null){
            for (Map.Entry<String, Object> e : source.getConfiguration().entrySet()) {
                Object value = e.getValue();
                if(value instanceof List){
                    value = new ArrayList((Collection) value);
                }
                configuration.put(e.getKey(), value);
            }
        }

        after = new ArrayList<String>();
        if(source.getAfter() != null){
            after.addAll(source.getAfter());
        }
        parameters = new ArrayList<Parameter>(source.getParameters().size());
        for (Parameter parameter : source.getParameters()) {
            this.parameters.add(new Parameter(parameter));
        }
        this.pipeline = source.getPipeline();
    }


    /**
     * Get the execution ID. This has to be unique within a pipeline
     *
     * @return id the execution id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the execution id
     *
     * @param id the execution id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the execution configuration
     *
     * @return cfg the execution configuration
     */
    public Map<String,Object> getConfiguration() {
        if(configuration == null) configuration = new HashMap<String, Object>();
        return configuration;
    }

    /**
     * Set the execution configuration
     *
     * @param configuration the execution configuration
     */
    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    /**
     * Get the list of manual dependencies to executions that must
     * be executed and complete before this execution can be started
     *
     * @return dependencies list of dependencies
     */
    public List<String> getAfter() {
        if(after == null) after = new ArrayList<String>();
        return after;
    }

    /**
     * Set the list of dependencies
     *
     * @param after the list of dependencies
     */
    public void setAfter(List<String> after) {
        this.after = after;
    }

    /**
     * Get the list of available parameters
     *
     * @return parameters the list of available parameters
     */
    public List<Parameter> getParameters() {
        if(parameters == null){
            this.parameters = new ArrayList<Parameter>();
        }
        return parameters;
    }

    /**
     * Set the list of available parameters
     *
     * @param parameters parameters the list of available parameters
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Get the pipeline that is represented by this execution or null if
     * this execution does not represent a pipeline
     *
     * @return pipeline the pipeline represented by this execution or null
     */
    public Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Set the pipeline represented by this execution
     *
     * @param pipeline the pipeline represented by this execution
     */
    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Get the id of the tool that created this job
     * @return tool the tool id
     */
    public String getToolId() {
        return toolId;
    }

    @Override
    public String toString() {
        return id;
    }
}
