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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class ValueNode {
    private static final Logger log = LoggerFactory.getLogger(ValueNode.class);
    private ScopeNode parent;
    private Object value;
    private Object source;
    private Object sourceProperty;
    boolean resolved= false;
    public boolean autoApply;

    ValueNode(ScopeNode parent, Object value, Object source, Object sourceProperty) {
        if(parent == null) throw new NullPointerException("Value nodes must have a parent scope");
        //if(value ==null) throw new NullPointerException("NULL value is not permitted in value nodes");
        this.parent = parent;
        this.value = value;
        this.source = source;
        this.sourceProperty = sourceProperty;

    }

    Object getValue(List<ValueNode> path, String variableName) {
        if(value == null) return null;
        if(path == null) path = new ArrayList<ValueNode>();
        if(log.isDebugEnabled()){
            log.debug("Resolving value node " + variableName + " with value "+ value + " for path " + path);
        }
        if(!resolved){
            value = resolve(value, path, variableName);
            resolved = true;
        }else{
            path.add(this);
        }


        // parent name
        if(value != null && !variableName.endsWith(parent.name)){
            // not the end of the chain, check fo file properties
            int last = variableName.lastIndexOf(parent.name) + parent.name.length()+1;
            String unresolved = variableName.substring(last);
            if(unresolved.contains("\\.")){
                throw new RuntimeException("Unable to resolve " + variableName );
            }
            if(value instanceof String){
                return resovleFile(value.toString(), unresolved);
            }else if(value instanceof Collection){
                return resovleFile(((Collection)value), unresolved);
            }else{
                throw new RuntimeException("Unable to resolve file parameter "+unresolved + " from " + value);
            }
        }
        return value;

    }



    private Object resolve(Object v, List<ValueNode> path, String variableName){
        if(path != null && path.contains(this) && variableName.endsWith(parent.name)){
            throw new IllegalArgumentException("Variable "+variableName+" has a reference to itself");
        }
        if (path != null) {
            path.add(this);
        }
        if(v instanceof String){
            if(((String)v).contains("${")){
                //try toString()  resolve in parent context
                return parent.get((String) v, path);
            }else{
                applyValue();
                return v;
            }
        }else if (v instanceof List){
            // resolve the values
            ArrayList copy = new ArrayList();
            List source = (List) v;
            for (Object o : source) {
                Object resolved = resolve(o, new ArrayList<ValueNode>(), variableName);
                if(resolved instanceof Collection){
                    for (Object vv : ((Collection) resolved)) {
                        copy.add(vv);
                    }
                }else{
                    copy.add(resolved);
                }
            }
            applyValue();
            return copy;
        }
        return v;
    }

    void setValue(Object value) {
        this.value = value;
        this.resolved = false;
        applyValue();

    }

    private void applyValue() {
        if(autoApply && source != null && sourceProperty != null){
            // apply the value
            try{
                if(source instanceof Map){
                    ((Map)source).put(sourceProperty, value);
                }else{
                    if(!setPropertyValue(sourceProperty.toString(),source, value )){
                        setFieldValue(sourceProperty.toString(), source, source.getClass(), value);
                    }
                }
            }catch (Exception e){
                throw new RuntimeException("Unable to apply value to " +sourceProperty + " @ " + source + " : " + e.getMessage());
            }
        }
    }

    private boolean setFieldValue(String name, Object object, Class cls, Object value) {
        try {
            Field field = cls.getDeclaredField(name);
            if (field != null) {
                boolean acc = field.isAccessible();
                field.setAccessible(true);
                field.set(object, value);
                field.setAccessible(acc);
                return true;
            }
        } catch (Exception e) {
        }
        // check superclass
        if (cls.getSuperclass() != null) {
            return setFieldValue(name, object, cls.getSuperclass(), value);
        }
        return false;
    }
    private boolean setPropertyValue(String name, Object object, Object value) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass(), Object.class);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                if (propertyDescriptor.getName().equals(name)) {
                    try {
                        propertyDescriptor.getWriteMethod().invoke(object, new Object[]{value});
                        return true;
                    } catch (Exception e) {
                    }
                }
            }
        } catch (IntrospectionException e) {
        }
        return false;
    }



    private List resovleFile(Collection value, String unresolved) {
        ArrayList copy = new ArrayList();
        for (Object o : value) {
            if(o instanceof String){
                copy.add(resovleFile(o.toString(), unresolved));
            }else if(o instanceof Collection){
                copy.add(resovleFile(((Collection) o), unresolved));
            }else{
                throw new RuntimeException("Unable to resolve file parameter "+unresolved + " from " + value);
            }
        }
        return copy;
    }

    private String resovleFile(String value, String unresolved) {
        if(unresolved.equals("name")) return getName(value);
        if(unresolved.equals("parent"))return getParent(value);
        if(unresolved.equals("extension")) return getExtension(value);
        throw new RuntimeException("Unable to resolve file parameter "+unresolved + " from " + value);
    }

    private String getName(String file){
        int startindex = file.lastIndexOf("/");
        if(startindex < 0) startindex = 0;
        else{
            startindex+=1;
        }
        int endIndex = file.indexOf(".", startindex);
        if(endIndex < 0){
            endIndex = file.length();
        }
        return file.substring(startindex, endIndex);
    }

    private String getParent(String file){
        int i = file.lastIndexOf("/");
        if(i>=0){
            return file.substring(0, i+1);
        }
        return "";
    }

    private String getExtension(String file){
//        int i =file.lastIndexOf(".");
        // fix JIP-99 and make sure full extension is returned
        int i =file.indexOf(".");
        if(i >= 0 && i < file.length()-1){
            return file.substring(i+1);
        }
        return "";
    }


    @Override
    public String toString() {
        return "ValueNode{" +
                "value=" + value +
                ", resolved=" + resolved +
                '}';
    }
}
