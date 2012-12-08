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
 * along with JIP.  If not, see <http://www.gnu.org/licenses/>.
 */

package jip.graph;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Pipeline graph node that contains the configuration for a job
 */
public class JobNode {
    /**
     * The local node pipelineJob configuration, this takes precedence over
     * all other configurations
     */
    private Map<String, Object> configuration;
    /**
     * Node id to identify this node in the graph
     */
    private String nodeId;

    /**
     * True if this is a split node
     */
    private boolean splitNode;
    /**
     * The referenced pipelineJob
     */
    private PipelineJob pipelineJob;

    /**
     * Create a new node
     *
     * @param pipelineJob the pipelineJob
     */
    public JobNode(PipelineJob pipelineJob) {
        this.pipelineJob = pipelineJob;
        this.nodeId = pipelineJob.getId();
        // add all non paramter config values to configuration
        HashSet<String> params = new HashSet<String>();
        for (Parameter parameter : pipelineJob.getParameters()) {
            params.add(parameter.getName());
        }
        for (Map.Entry<String, Object> e : pipelineJob.getConfiguration().entrySet()) {
            if(!params.contains(e.getKey())){
                getConfiguration().put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Copy constructor. NOTE that this does NOT clone the  actual graph structure, just
     * a clone of the node !
     *
     * @param node the source
     */
    public JobNode(JobNode node) {
        this.pipelineJob = node.getPipelineJob();
        this.nodeId = node.getNodeId();

        if (node.getConfiguration() != null) {
            for (String o : node.getConfiguration().keySet()) {
                Object value = node.getConfiguration().get(o);
                getConfiguration().put(o, value);
            }
        }
    }

    /**
     * Get the pipelineJob
     *
     * @return pipelineJob the pipelineJob
     */
    public PipelineJob getPipelineJob() {
        return pipelineJob;
    }

    /**
     * Get optional customized node configuration. This is used to
     * store the configuration after a many-to-one split
     *
     * @return config the custom node config
     */
    public Map<String, Object> getConfiguration() {
        if (configuration == null) configuration = new HashMap<String, Object>();
        return configuration;
    }

    /**
     * Get the node id
     *
     * @return id node id
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * True if this is a split node
     *
     * @return split true if this is a node
     */
    public boolean isSplitNode() {
        return splitNode;
    }

    /**
     * Set split nod status
     *
     * @param splitNode split node
     */
    public void setSplitNode(boolean splitNode) {
        this.splitNode = splitNode;
    }

    /**
     * Set the node id
     *
     * @param nodeId th node id
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Parameter getParameterRaw(String name) {
        for (Parameter parameter : getParameter()) {
            if(parameter.getName().equals(name)) return parameter;
        }
        return null;
    }
    /**
     * Resolve parameter value in proper order, starting at the node, then checking the
     * pipelineJob context and then checking the executable for some default value
     *
     * @param name the parameter name
     * @return value the value or null
     */
    public Object getParameter(String name) {
        PipelineJob execution = getPipelineJob();
        Map<?, ?> executionConfig = execution.getConfiguration();
        Map<?, ?> nodeConfig = getConfiguration();
        // start at node level
        Object value = nodeConfig != null ? nodeConfig.get(name) : null;
        if (value == null) {
            value = executionConfig != null ? executionConfig.get(name) : null;
            if (value == null) {
                for (Parameter parameter : execution.getParameters()) {
                    if(parameter.getName().equals(name)) return  parameter.getDefaultValue();
                }
            }
        }
        return value;
    }
    /**
     * Get the parameter value while ignoring any defaults. This returns the raw config value
     *
     * @param name the parameter name
     * @return value the value or null
     */
    public Object getParameterUnresolved(String name) {
        PipelineJob execution = getPipelineJob();
        Map<?, ?> executionConfig = execution.getConfiguration();
        Map<?, ?> nodeConfig = getConfiguration();
        // start at node level
        Object value = nodeConfig != null ? nodeConfig.get(name) : null;
        if (value == null) {
            value = executionConfig != null ? executionConfig.get(name) : null;
        }
        return value;
    }

//    public void makeRelative(String root){
//        String cwd = getDirectory()+(getDirectory().endsWith("/") ? "":"/");
//        root = root.endsWith("/") ? root:root+"/";
//        for (Object key : getConfiguration().keySet()) {
//            Object sourceValue = getConfiguration().get(key);
//            getConfiguration().put(key, relative(sourceValue, cwd, root));
//        }
//        setDirectory(FileUtils.getRelativePath(cwd,root, "/"));
//    }

//    private Object relative(Object sourceValue, String cwd, String root){
//        if(sourceValue instanceof String && ((String) sourceValue).startsWith("/")){
//            try {
//                if(!sourceValue.toString().startsWith(root)) return sourceValue;
//                String relativePath = FileUtils.getRelativePath(sourceValue.toString(), cwd, "/");
//                return relativePath;
//            } catch (RuntimeException e) {}
//            return sourceValue;
//        }
//
//        if(sourceValue instanceof Collection){
//            List copy = new ArrayList();
//            for (Object o : ((Collection) sourceValue)) {
//                Object res = relative(o, cwd,root);
//                if(res != null){
//                    copy.add(res);
//                }
//            }
//            return copy;
//        }
//        return sourceValue;
//    }



    /**
     * Get parameter associated with this node
     *
     * @return parameter the list of parameters associated with this node
     */
    public List<Parameter> getParameter() {
        return new ArrayList<Parameter>(getPipelineJob().getParameters());
    }


    /**
     * Searches for one-to-one configuration relationships and returns a map
     * with parameter name (target property on this node) and a 2-element String[]
     * as value, where the first element is the source node id and the second element
     * is the source property (output property)
     *
     * @param graph the pipeline graph
     * @return params map : tragetProperty->[sourceNode, sourceProperty]
     */
    public List<JobEdge> one2one(PipelineGraph graph) {
        return findDependency(graph, false, false);
    }

    /**
     * Searches for many-to-one configuration relationships and returns a map
     * with parameter name (target property on this node) and a 2-element String[]
     * as value, where the first element is the source node id and the second element
     * is the source property (output property)
     *
     * @param graph the pipeline graph
     * @return params map : tragetProperty->[sourceNode, sourceProperty]
     */
    public List<JobEdge> many2one(PipelineGraph graph) {
        return findDependency(graph, true, false);
    }

    /**
     * Searches for one-to-many configuration relationships and returns a map
     * with parameter name (target property on this node) and a 2-element String[]
     * as value, where the first element is the source node id and the second element
     * is the source property (output property)
     *
     * @param graph the pipeline graph
     * @return params map : tragetProperty->[sourceNode, sourceProperty]
     */
    public List<JobEdge> one2many(PipelineGraph graph) {
        return findDependency(graph, false, true);
    }

    /**
     * Searches for one-to-many configuration relationships and returns a map
     * with parameter name (target property on this node) and a 2-element String[]
     * as value, where the first element is the source node id and the second element
     * is the source property (output property)
     *
     * @param graph the pipeline graph
     * @return params map : tragetProperty->[sourceNode, sourceProperty]
     */
    public List<JobEdge> many2many(PipelineGraph graph) {
        return findDependency(graph, true, true);
    }

    /**
     *  Get the list of pipelineJob ids that this node has to wait for
     * @return execIds dependency ids
     */
    public List<String> getSourceDependecies(PipelineGraph graph){
        ArrayList<String> ids = new ArrayList<String>();
        for (JobEdge jobEdge : graph.getGraph().incomingEdgesOf(this)) {
            JobNode source = graph.getGraph().getEdgeSource(jobEdge);
            ids.add(source.getPipelineJob().getId());
        }
        return ids;
    }

    /**
     * Compute the depth of the node
     *
     * @param graph the graph
     * @return depth the depth
     */
    public int getDepth(PipelineGraph graph){
        if(graph.getGraph().inDegreeOf(this) == 0) return 1;
        Set<JobEdge> jobEdges = graph.getGraph().incomingEdgesOf(this);
        int pd = Integer.MAX_VALUE;
        for (JobEdge jobEdge : jobEdges) {
            pd = Math.min(graph.getGraph().getEdgeSource(jobEdge).getDepth(graph), pd);
        }
        return pd+1;
    }

    @Override
    public String toString() {
        return getNodeId();
    }

    protected List<JobEdge> findDependency(PipelineGraph graph, boolean manyIn, boolean manyOut) {
        return null;
    }

    public List<JobEdge> findDependency(PipelineGraph graph) {
        List<JobEdge> edges = new ArrayList<JobEdge>();
        for (Parameter parameter : getParameter()) {
            boolean manyIn = parameter.isList();
            String sourceContextKey = "${" + getNodeId() + "." + parameter.getName() + "}";
            Object value = graph.getContext().get(sourceContextKey);
            if (value == null) {
                continue;
            }
            Object configValue = getParameter(parameter.getName());
            // fixed parameter
            List<ParamMapping> sourceNodes = isDynamic(configValue, graph);
            if (sourceNodes == null) {
                getConfiguration().put(parameter.getName(), value);
                continue;
            }

            for (ParamMapping mapping : sourceNodes) {

                JobEdge.Type type;
                // pick a type
                if (mapping.parameter.isList()) {
                    // it a collection
                    if (manyIn) {
                        type  = JobEdge.Type.Many2Many;
                    } else {
                        type = JobEdge.Type.Many2One;
                    }
                } else {
                    // its a singlevalue
                    if (manyIn) {
                        type = JobEdge.Type.One2Many;
                    } else {
                        type = JobEdge.Type.One2One;
                    }
                }
                // add the edge
//                System.out.println("!!"+value);
//                System.out.println("??"+mapping.node.getParameter(mapping.parameter.getName()));
                value = mapping.node.getParameter(mapping.parameter.getName());
                edges.add(new JobEdge(type, mapping.node.getNodeId(), parameter.getName(), value, mapping.parameter.getName()));
            }
        }
        return edges;
    }

    private List<ParamMapping> isDynamic(Object name, PipelineGraph graph) {
        ArrayList<ParamMapping> jobNodes = new ArrayList<ParamMapping>();
        if(name instanceof String){
            if(((String) name).contains("${")){
                Matcher matcher = ScopeNode.SPLITTER.matcher((CharSequence) name);
                List<String> matches = new ArrayList<String>();
                while (matcher.find()) {
                    matches.add(matcher.group());
                }
                if (matches.size() == 0) {
                    return null;
                }

                for (String k : matches) {
                    if (k.startsWith("${") && k.endsWith("}")) {
                        k = k.substring(2, k.length() - 1);

                        String[] split = k.split("\\.");
                        if(split.length > 1){
                            String nid = split[0];
                            String param = split[1];
                            JobNode node = graph.findNode(nid);
                            boolean contained = false;
                            for (ParamMapping jobNode : jobNodes) {
                                if(jobNode.node.equals(node)){
                                    contained = true;
                                    break;
                                }
                            }
                            if(node != null && node != this && !contained){
                                for (Parameter parameter : node.getParameter()) {
                                    if(parameter.getName().equals(param)){
                                        if(parameter.isOutput()){
                                            jobNodes.add(new ParamMapping(node, parameter));
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }if (name instanceof Collection){
            Collection c = (Collection) name;
            for (Object o : c) {
                List<ParamMapping> nodes = isDynamic(o, graph);
                if(nodes != null){
                    for (ParamMapping node : nodes) {
                        if(node.node != this && !jobNodes.contains(node)){
                            jobNodes.add(node);
                        }
                    }
                }
            }
        }
        return jobNodes.size() == 0 ? null : jobNodes;
    }

    /**
     * Create a single contexts that covers all variables/configuration and output for this node
     */

    public void createContext(ScopeNode ctx) {
        // add config
        for (Parameter parameter : getParameter()) {
            Object value = getParameter(parameter.getName());
            ctx.createValue(parameter.getName(), value);
        }
    }



    private class ParamMapping{
        JobNode node;
        Parameter parameter;

        public ParamMapping(JobNode node, Parameter parameter) {
            this.node = node;
            this.parameter = parameter;
        }
    }


}
