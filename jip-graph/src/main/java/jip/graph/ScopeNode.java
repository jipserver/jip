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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Scope entry for a variable tree. A scope node can either have scope node children or it can have a single
 * {@link ValueNode} as a child. Values can be resolved using {@link #get(String)}, where the string can be a variable
 * reference like {@literal "${myscope.myvariable}"} or a string that contains variables.
 */
public class ScopeNode {
    /**
     * the Splitter
     */
    transient static final Pattern SPLITTER = Pattern.compile("\\$\\{([A-Za-z0-9\\._-]+)\\}|([^\\{\\}\\$]+)", Pattern.DOTALL);
    /**
     * The nodes name
     */
    String name;
    /**
     * the children
     */
    private List<ScopeNode> children = new ArrayList<ScopeNode>();
    /**
     * The parent node
     */
    ScopeNode parent;
    /**
     * Value node
     */
    private ValueNode valueNode;

    /**
     * Create a new scope node
     *
     * @param name the scope node
     */
    ScopeNode(String name) {
        if (name == null) throw new NullPointerException("NULL name not permitted for scope nodes");
        if (name.contains("\\.")) throw new IllegalArgumentException("'.' are reserved and can not be used in names !");
        this.name = name;
    }

    /**
     * INTERNAL
     * @param name the name
     * @param parent the parent
     */
    private ScopeNode(String name, ScopeNode parent) {
        this(name);
        this.parent = parent;
    }

    /**
     * Create a child scope
     *
     * @param name the name of the child scope
     * @return child the child scope
     */
    public ScopeNode createChild(String name) {
        if (valueNode != null)
            throw new IllegalArgumentException("The scope node has a value assigned and cannot have child nodes");
        for (ScopeNode child : children) {
            if (child.name.equals(name))
                throw new IllegalArgumentException("The scope has already a node with name " + name);
        }
        ScopeNode child = new ScopeNode(name, this);
        children.add(child);
        return child;
    }

    /**
     * Create a child and set its value
     *
     * @param name the name of th child
     * @param value the value
     */
    public void createValue(String name, Object value) {
        ScopeNode child = createChild(name);
        child.setValue(value);
    }

    /**
     * Create a value node with a reference to its original source
     *
     * @param name the name
     * @param value the value
     * @param source the source object
     * @param sourceProperty the source property
     */
    public ScopeNode createValue(String name, Object value, Object source, Object sourceProperty) {
        ScopeNode child = createChild(name);
        child.setValue(value, source, sourceProperty);
        child.valueNode.autoApply=true;
        return child;
    }

    public void createValueFromSource(Object property, Object source){
        createValueFromSource(property, source, true);
    }
    public void createValueFromSource(Object property, Object source, boolean autoApply){
        Object value = null;
        if(source instanceof Map){
            value = ((Map) source).get(property);
            if(value == null){
                throw new RuntimeException("Unable to read value for " + property);
            }
        }else{
            // todo: compelte remvoe ? the bean lookup is really expensive !
            //value = getPropertyValue(property.toString(), source);
            if(value == null){
                // try field
                value = getFieldValue(property.toString(), source, source.getClass());
            }
        }
        ScopeNode child = createValue(property.toString(), value, source, property);
        child.valueNode.autoApply = autoApply;
    }

    /**
     * Set the value for this node. An exception is triggered if
     * this node already has scope children
     *
     * @param value the value
     */
    public void setValue(Object value) {
        setValue(value, null, null);
    }

    /**
     * Set the value of this node with reference to the original source object
     * and the source property
     *
     * @param value the value
     * @param source original source
     * @param sourceProperty the original source property
     */
    public void setValue(Object value, Object source, Object sourceProperty) {
        if (children.size() > 0)
            throw new IllegalArgumentException("The scope node has child scopes and can not have a value!");
        this.valueNode = new ValueNode(this, value, source, sourceProperty);
    }

    /**
     * INternal: resolve a value
     *
     * @param fullName the full variable name
     * @param index the current search index
     * @param path the path of nodes already searched
     * @return value the value or null
     */
    ValueNode resolve(String fullName, int index, List<ValueNode> path) {
        // split the full name
        String[] split = fullName.split("\\.");
        if (index >= split.length) return null;
        String current = split[index];
        String next = null;
        if(index+1 < split.length){
            next = split[index+1];
        }

        // strategies to resolve a value
        if (name.equals(current) && valueNode != null) {
            try {
                //return valueNode.getValue(path);// throws NPE when no value is set
                return valueNode;// throws NPE when no value is set
            } catch (IllegalArgumentException e) {
                if (e.getMessage().equals("circle")) {
                    throw new RuntimeException("Variable " + fullName + " has a reference to itself");
                }
            }
            return null;
        } else {
            if (valueNode != null) {
                // start a search from the root
                // if path is empty,so no jump to root happend before
                if (parent == null) throw new NullPointerException("Variable " + fullName + " not found");
                if (path != null && path.contains(parent))
                    return null; // parent is searching already and this is no hit
                // move up in scope
                if (path == null) {
                    path = new ArrayList<ValueNode>();
                }
                return moveUp(fullName, path);
            } else {
                // this is a scope node, if we are in the path already
                // stop searching
                if (path != null && path.contains(this))
                    throw new NullPointerException("Variable " + fullName + " not found");
                if (path == null) path = new ArrayList<ValueNode>();


                ArrayList<ValueNode> pathcopy = new ArrayList<ValueNode>(path);
                if(current.equals(name)){
                    for (ScopeNode child : children) {
                        if (!path.contains(child) && next != null && child.name.equals(next)) {
                            ValueNode resolved = child.resolve(fullName, index + 1, path);
                            if (resolved != null) return resolved;
                        }
                    }
                    throw new NullPointerException("Variable " + fullName + " not found");
                }else{
                    // check first level direct
                    for (ScopeNode child : children) {
                        if (child.name.equals(current)) {
                            ValueNode resolved = child.resolve(fullName, index, pathcopy);
                            if (resolved != null) return resolved;
                        }
                    }
                }

                if (parent == null) throw new NullPointerException("Variable " + fullName + " not found");
                // move up in scope
                return moveUp(fullName, path);

            }
        }
    }

    /**
     * Helper to find the first node specified in the full path
     * and use it to resolve the variable or return null.
     *
     * @param fullName the full path
     * @param path the path
     * @return value the value resolved by the full path or nulls
     */
    private ValueNode moveUp(String fullName, List<ValueNode> path) {
        if (parent == null || fullName == null) return null;
        String[] split = fullName.split("\\.");
        String current = split[0];

        // traverse upwards and find a matching node
        ScopeNode n = parent;
        ScopeNode last = this;
        while (n != null && !n.name.equals(current)){
            // check first level direct
            for (ScopeNode child : n.children) {
                if (child.name.equals(current)) {
                    ValueNode resolved = child.resolve(fullName, 0, path);
                    if (resolved != null) return resolved;
                }
            }

            last = n;
            n = n.parent;
        }

        if (n == null){
            // do a bfs search to find the node
            Stack<ScopeNode> stack =new Stack<ScopeNode>();
            stack.push(last);
            while(!stack.isEmpty()){
                ScopeNode pop = stack.pop();
                if(pop.name.equals(current)) return pop.resolve(fullName, 0,path);
                for (ScopeNode child : pop.children) {
                    stack.push(child);
                }
            }
            return null;
        }
        return n.resolve(fullName, 0, path);
    }

    /**
     * Get the value for the specified variable. If the string does not
     * contain a variable, its simply returned. Otherwise the value is resolved
     * and returned.
     *
     * @param string the search string
     * @return value the resolved value or null
     */
    public Object get(String string) {
        return get(string, null);
    }

    /**
     * Set the value for the specified variable
     *
     * @param string the variable name
     * @param value the value
     */
    public void set(String string, Object value) {
        if (string == null) return;
        if (!string.contains("${"))
            throw new IllegalArgumentException("No variable specified, the string has to be wrapped in ${}");
        Matcher matcher = SPLITTER.matcher(string);

        List<String> matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        if (matches.size() != 1) {
            throw new IllegalArgumentException("Unable to set variable " + string + " : variable not found");
        }
        if (matches.size() == 1) {
            String k = matches.get(0);
            if (k.startsWith("${") && k.endsWith("}")) {
                k = k.substring(2, k.length() - 1);
                ArrayList<ValueNode> path = new ArrayList<ValueNode>();
                ValueNode valueNode = resolve(k, 0, path);
                if (valueNode == null)
                    throw new IllegalArgumentException("Unable to set variable " + string + " : variable not found");
                valueNode.setValue(value);
            }
        }
    }

    /**
     * Internale: resolve the value
     *
     * @param string the variable path
     * @param path the search path
     * @return value the resolved value or null
     */
    Object get(String string, List<ValueNode> path) {

        if (string == null) return null;
        if (!string.contains("${")) return string;
        Matcher matcher = SPLITTER.matcher(string);

        List<String> matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        if (matches.size() == 0) {
            throw new IllegalArgumentException("No matches found");
        }
        if (matches.size() == 1) {
            String k = matches.get(0);
            if (k.startsWith("${") && k.endsWith("}")) {
                k = k.substring(2, k.length() - 1);
                ValueNode valueNode = resolve(k, 0, path);
                return valueNode != null ? valueNode.getValue(path, k) : null;
            } else {
                return k;
            }
        } else {
            StringBuilder b = new StringBuilder();
            List<StringBuilder> collection = null;
            for (String match : matches) {
                String k = match;
                if (k.startsWith("${") && k.endsWith("}")) {
                    k = k.substring(2, k.length() - 1);
                    ValueNode value = resolve(k, 0, path);
                    if(value == null){
                        continue;
                    }
                    Object result = value.getValue(path, k);

                    if (result != null) {
                        if (result instanceof Collection) {
                            // expand to list
                            Collection c = (Collection) result;
                            if (collection == null) {
                                collection = new ArrayList<StringBuilder>();
                                // add clones of current buffer
                                for (int i = 0; i < c.size(); i++) {
                                    collection.add(new StringBuilder(b));
                                }
                            }

                            // check the size
                            if (c.size() != collection.size())
                                throw new IllegalArgumentException("List expansion for lists with unequal size is not supported!");
                            int i = 0;
                            for (Object o : c) {
                                collection.get(i).append(o);
                                i++;
                            }
                        } else {
                            if (collection == null) {
                                b.append(result);
                            } else {
                                for (StringBuilder stringBuilder : collection) {
                                    stringBuilder.append(result);
                                }
                            }
                        }
                    }
                } else {
                    if (collection == null) {
                        b.append(k);
                    } else {
                        for (StringBuilder stringBuilder : collection) {
                            stringBuilder.append(k);
                        }
                    }
                }
            }
            if(collection != null){
                ArrayList<String> strings = new ArrayList<String>();
                for (StringBuilder stringBuilder : collection) {
                    strings.add(stringBuilder.toString());
                }
                return strings;
            }


            return b.toString();
        }
    }


    /**
     * Search for the bean property with the given name. This only finds properties with
     * getters
     *
     * @param name   the name
     * @param object the object
     * @return value the value or null
     */
    private Object getPropertyValue(String name, Object object) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass(), Object.class);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                if (propertyDescriptor.getName().equals(name)) {
                    try {
                        return propertyDescriptor.getReadMethod().invoke(object, (Object[]) null);
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to read value for " + name);
                    }
                }
            }
        } catch (IntrospectionException e) {
        }
        return null;

    }

    /**
     * Does a field search for the name and searched the full hierarchy
     *
     * @param name   the field name
     * @param object the object
     * @param cls    the class
     * @return value the value or null
     */
    private Object getFieldValue(String name, Object object, Class cls) {
        try {
            Field field = cls.getDeclaredField(name);
            if (field != null) {
                boolean acc = field.isAccessible();
                field.setAccessible(true);
                Object result = field.get(object);
                field.setAccessible(acc);
                return result;
            }
        }catch (Exception e) {
            throw new RuntimeException("Unable to read value for " + name);
        }
        // check superclass
        if (cls.getSuperclass() != null) {
            return getFieldValue(name, object, cls.getSuperclass());
        }
        return null;
    }


    @Override
    public String toString() {
        return "ScopeNode{" + "name='" + name + '\'' +", valueNode=" + valueNode +'}';
    }
}
