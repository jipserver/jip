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
 * Represents an edge in the pipeline graph and carries information about the
 * connection type. It can either be a manual "after" dependencies, in which case it
 * simply ensures the job dependencies. It can also link output of one execution
 * with the input of another, in which case it stores the input and output values.
 */
public class JobEdge {
    static enum Type{
        /**
         * LInks list of output to list of input
         */
        Many2Many,
        /**
         * Links single value output to list of input
         */
        One2Many,
        /**
         * LInks single value to single value
         */
        One2One,
        /**
         * Links a list of values to a single value
         */
        Many2One,
        /**
         * Manual dependency that does not link anything
         */
        After
    }

    /**
     * The edge type
     */
    private Type type;
    /**
     * The target property
     */
    private String targetProperty;

    /**
     * The source property
     */
    private String sourceProperty;

    /**
     * The source node id
     */
    private String sourceNode;

    /**
     * The target property
     */
    private Object value;

    /**
     * Index to be used when this is a splitted many2one edge
     */
    private int index = -1;


    /**
     * Create a manual after edge
     *
     * @param sourceNode the source node
     */
    public JobEdge(String sourceNode) {
        this(Type.After, sourceNode, null, null, null);
    }

    /**
     * Create a new edge. The type must be defined and the source and target properties
     * are only allowed to be null if the type is 'After'.
     *
     * @param type the type
     * @param sourceNode the source node
     * @param targetProperty the target property
     * @param value the value
     * @param sourceProperty the source property
     */
    public JobEdge(Type type,String sourceNode,  String targetProperty, Object value, String sourceProperty) {
        if(type == null) throw new IllegalArgumentException("Null type not permitted");
        this.type = type;
        this.targetProperty = targetProperty;
        this.sourceNode = sourceNode;
        this.sourceProperty = sourceProperty;
        this.value = value;
        if(this.value instanceof Collection){
            // make sure its a flat list
            this.value = JobEdge.flatten((Collection) this.value);
        }
        if((this.targetProperty == null || this.value == null) && type != Type.After){
            throw new IllegalArgumentException("The edge does not represent a manual dependency, but target property is defined");
        }
        if((this.sourceProperty == null || this.value == null) && type != Type.After){
            throw new IllegalArgumentException("The edge does not represent a manual dependency, but no source property is defined");
        }
    }

    /**
     * The edge type
     * @return type the edge type
     */
    public Type getType() {
        return type;
    }

    /**
     * Set the type of this edge
     *
     * @param type the type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * The target property
     *
     * @return targetProperty the target property
     */
    public String getTargetProperty() {
        return targetProperty;
    }

    /**
     * set the target property
     * @param targetProperty the target property
     */
    public void setTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
    }

    /**
     * Get the many2one resolve index
     *
     * @return index the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the many2one resolve index
     * @param index the index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * The the source node id
     * @return id the source node id
     */
    public String getSourceNode() {
        return sourceNode;
    }

    /**
     * Set the source node
     * @param sourceNode the source node
     */
    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }


    /**
     * Get the source property specified for this edge
     *
     * @return sourceProperty the source property
     */
    public String getSourceProperty() {
        return sourceProperty;
    }

    /**
     * Get the raw value
     * @return value the raw value
     */
    public Object getValue() {
        return value;
    }


    /**
     * Update the given object and ensure that if its a string and
     * is specified without leading "/", that the directory prefix is added.
     * This recursively iterates collections
     *
     * @param sourceNode the source node
     * @param sourceValue the object
     */
    public Object addDirectoryToFiles(JobNode sourceNode, Object sourceValue){
        if(sourceValue == null) return null;

//        if(sourceValue instanceof String && !((String) sourceValue).startsWith("/")){
//            String fullDir = sourceNode.getDirectory() + "/" + sourceValue;
//            return fullDir;
//        }
        if(sourceValue instanceof Collection){
            List copy = new ArrayList();
            for (Object o : ((Collection) sourceValue)) {
                Object res = addDirectoryToFiles(sourceNode, o);
                if(res != null){
                    copy.add(res);
                }
            }
            return copy;
        }
        return sourceValue;
    }

    /**
     * If source or target properties are configured, apply the configuration and set the actual value
     *
     * @param graph the graph
     */
    public void applyConfiguration(PipelineGraph graph) {
        JobNode source = graph.graph.getEdgeSource(this);
        JobNode target = graph.graph.getEdgeTarget(this);

        if(source != null && target != null){
            Object sourceValue = value;
//            if(index >= 0 && sourceValue instanceof List){
//                sourceValue = ((List)value).get(index);
//                value = source;
//            }


            if(sourceValue != null){
                boolean isFile = false;
                for (Parameter parameter : target.getParameter()) {
                    if(parameter.getName().equals(targetProperty)){
                        isFile = parameter.isFile();
                        break;
                    }
                }
                if(isFile){
                    sourceValue = addDirectoryToFiles(source, sourceValue);
                }
            }

            switch (getType()){
                case One2One:
                    if(getIndex() < 0 || !(sourceValue instanceof List))
                        target.getConfiguration().put(targetProperty, sourceValue);
                    else{
                        target.getConfiguration().put(targetProperty, ((List)sourceValue).get(getIndex()));
                    }
                    break;
                case One2Many:
                    Object targetV = target.getParameter(targetProperty);
                    Collection targetCollection;
                    if(targetV == null || !( targetV instanceof Collection) )
                        targetCollection = new ArrayList();
                    else{
                        targetCollection = (Collection) targetV;
                    }

                    // remove unresolved values from the source thing
                    removeUnresolvedStrings(targetCollection);

                    if(sourceValue instanceof Collection){
                        for (Object o : (Collection) sourceValue) {
                            //if(!targetCollection.contains(o))
                            targetCollection.add(o);
                        }
                    }else{
                        //if(!targetCollection.contains(sourceValue))
                            targetCollection.add(sourceValue);
                    }

                    // flatten the list
                    targetCollection = new ArrayList(flatten(targetCollection));

                    target.getConfiguration().put(targetProperty, targetCollection);

                    // save in context ?
                    //entry = graph.getContext().getEntry("${" + target.getNodeId() + ".configuration." + targetProperty + "}");
                    //entry.setValue(targetCollection);

                    break;
                case Many2Many:
                    Object targetVV = target.getParameter(targetProperty);
                    Collection tc;
                    if(targetVV == null || !( targetVV instanceof Collection) )
                        tc = new ArrayList();
                    else{
                        tc = (Collection) targetVV;
                    }
                    Collection value = (Collection) sourceValue;                                        
                    if(value != null){
                        tc.addAll(value);                        
                    }
                    // fix JIP-195 and make sure we remove any references to the original sources
                    tc.remove("${"+getSourceNode() + "." + getSourceProperty() + "}");

                    target.getConfiguration().put(targetProperty, tc);
                    // save in context ?
                    //entry = graph.getContext().getEntry("${" + target.getNodeId() + ".configuration." + targetProperty + "}");
                    //entry.setValue(tc);

                    break;
                case Many2One: throw new RuntimeException("Unable to transfer configuration for many-to-one relations! Make sure you split the graph before applying the config!");
            }
        }
    }

    static Collection flatten(Collection targetCollection) {
        return flatten(targetCollection, null, null);
    }

    private static Collection flatten(Collection targetCollection, Map flattr, List target) {
        if(flattr == null)
            flattr = new HashMap();
        if(target == null)
            target = new ArrayList();

        for (Object o : targetCollection) {
            if(o instanceof Collection){
                Collection flat = flatten((Collection) o, flattr, target);
                for (Object o1 : flat) {
                    if(!flattr.containsKey(o1)){
                        flattr.put(o1, true);
                        target.add(o1);
                    }
                }
            }else{
                if(!flattr.containsKey(o)){
                    flattr.put(o, true);
                    target.add(o);
                }
            }
        }
        return target;
    }

    private void removeUnresolvedStrings(Collection list) {
        List<Object> toRemove = new ArrayList<Object>();
        for (Object o : list) {
            if(o instanceof String){
                if(((String) o).contains("${")){
                    toRemove.add(o);
                }
            }else if(o instanceof Collection){
                removeUnresolvedStrings((Collection) o);
            }
        }
        list.removeAll(toRemove);
    }


    @Override
    public String toString() {
        return targetProperty;
    }
}
