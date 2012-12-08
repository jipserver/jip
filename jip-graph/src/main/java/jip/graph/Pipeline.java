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

/**
 * Represent a pipeline.
 */
public class Pipeline{
    /**
     * The executions
     */
    private List<PipelineJob> executions;

    /**
     * The global configuration for this pipeline
     */
    private Map<String, Object> configuration;

    /**
     * List of pipeline parameter
     */
    private List<Parameter> parameter;


    /**
     * Default
     */
    public Pipeline() {
    }

    /**
     *Copy constructor
     *
     * @param source source
     */
    public Pipeline(Pipeline source) {
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

        executions = new ArrayList<PipelineJob>();
        if(source.getExecutions() != null){
            for (PipelineJob execution : source.getExecutions()) {
                executions.add(new PipelineJob(execution));
            }
        }
        this.parameter = new ArrayList<Parameter>();
        for (Parameter p : source.getParameter()) {
            this.parameter.add(new Parameter(p));
        }
    }

    /**
     * Get the list of executions
     *
     * @return execution the list of executions
     */
    public List<PipelineJob> getExecutions() {
        if(executions == null) executions = new ArrayList<PipelineJob>();
        return executions;
    }

    /**
     * Set the list of executions
     *
     * @param executions the list of executions
     */
    public void setExecutions(List<PipelineJob> executions) {
        if(executions== null) executions = new ArrayList<PipelineJob>();
        this.executions = executions;
    }

    /**
     * Get the global configuration or null
     *
     * @return configuration the global configuration or null
     */
    public Map<String, Object> getConfiguration() {
        if(configuration == null) configuration = new HashMap<String, Object>();
        return configuration;
    }

    /**
     * Set the global configuration
     *
     * @param configuration the configuration
     */
    public void setConfiguration(Map<String, Object> configuration) {
        if(configuration == null) configuration = new HashMap<String, Object>();
        this.configuration = configuration;
    }

    /**
     * Get the list of pipeline parameter
     *
     * @return parameters list of pipeline parameter
     */
    public List<Parameter> getParameter() {
        if(parameter == null) parameter = new ArrayList<Parameter>();
        return parameter;
    }

    /**
     * Set the list of parameter
     *
     * @param parameter the parameter
     */
    public void setParameter(List<Parameter> parameter) {
        this.parameter = parameter;
    }

}
