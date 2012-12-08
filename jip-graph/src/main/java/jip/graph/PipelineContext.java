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
 * A pipeline context can be filled with objects and properties of the objects can be queried.
 * You can query the map for context entries. An entry can be either static, which means its
 * value is valid and can be used. If the entry is dynamic, the value can still be resolved,
 * but its contents validity depends on its source. For example, if the source is an execution
 * and the value is the executions output, it is only valid after the execution run successfully.
 * <p>
 * By default, all entries are dynamic, but you can mark certain name spaces as static
 * </p>
 * <p/>
 * <p/>
 * <p>
 * You can use {@literal ${name.property} } keys to get sub properties fom the context.
 * </p>
 */
public class PipelineContext {
    transient static final Pattern SPLITTER = Pattern.compile("\\$\\{([A-Za-z0-9\\._]+)\\}|([^\\{\\}\\$]+)", Pattern.DOTALL);
    /**
     * The data container
     */
    private Map<String, Entry> data;

    /**
     * The data source
     */
    private Object source;
    /**
     * Parent context
     */
    private PipelineContext parent;

    /**
     * Create a new context
     */
    PipelineContext(Object source) {
        super();
        this.source = source;
        data = new HashMap<String, Entry>();
    }

    public boolean addEntry(String key, Object value) {
        boolean dynamic = true;
        if (value instanceof Number) dynamic = false;
        if (value instanceof Map) dynamic = false;
        if (value instanceof PipelineContext) dynamic = false;
        if (value instanceof FileParameter) dynamic = false;
        if (value instanceof String) dynamic = ((String) value).contains("${");
        return addEntry(key, value, source, dynamic);
    }

    public boolean addEntry(String key, Object value, Object source, boolean dynamic) {
//        if (value == null) return false;
        if (value instanceof PipelineContext) {
            ((PipelineContext) value).parent = this;
        }
        data.put(key, new Entry(dynamic, source, value, this));
        return true;
    }

    public PipelineContext createChild(String key, Object source) {
        PipelineContext child = new PipelineContext(source);
        child.parent = this;
        addEntry(key, child, source, false);
        return child;
    }

    public Entry getEntry(String key) {
        return get(key);
    }

    public Object getValue(String key) {
        Entry entry = getEntry(key);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    protected Entry get(String key) {
        // simple case
        // -key is contained directly
        Entry superValue = data.get(key);
        if (superValue != null) {
            return superValue;
        }
        //check if there is something to resolve
        // and catch illegal keys
        if (!key.contains("${")) return null;
        if (key.equals("${}")) return null; // illegal key
        if (key.equals("$")) return null; // illegal key

        // otherwise remove any trailing $ ${ and ending }
        String name = (String) key;
        if (name.startsWith("$")) name = name.substring(1);
        if (name.startsWith("{")) name = name.substring(1);
        if (name.endsWith("}")) name = name.substring(0, name.length() - 1);

        String[] elements = name.split("\\.");
        Entry entry = null;
        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];
            if (i == 0) {
                // first level comes from the map
                if (element.equals(name)) {
                    // no split happened. just check for a direct value
                    entry = data.get(element);
                } else {
                    entry = get(element);
                    if (entry != null && entry.context == this && entry.value.equals(key)) {
                        // recursive call that will end in a stackoverflow ?
                        entry = null;
                    }
                }
                // if first level fails in this context
                // check the parent
                if (entry == null && parent != null) {
                    return parent.get(key);
                }
            } else {
                // check the value for all sub levels
                Object entryValue = entry.getValue();
                entry = getValue(element, entryValue);
                if (entry == null && entryValue != null && (entryValue instanceof String) && !((String)entryValue).contains("${")) {
                    // quick hack to allow searching for file parameters
                    entry = getValue(element, new FileParameter((String) entryValue));
                }
            }
            // stop if we find a null value
            if (entry == null) break;
        }
        return entry;

    }

    /**
     * Get the value for the specified name by splitting the name. This searches
     * the given object for the property and returns the property value
     *
     * @param name   the name
     * @param object the source value
     * @return value the value or null
     */
    private Entry getValue(String name, Object object) {
        // check the value for all sub levels
        if (object instanceof PipelineContext) {
            return ((PipelineContext) object).getEntry(name);
        } else if (object instanceof Map) {
            Object value = ((Map) object).get(name);
            if (value == null) return null;
            return new Entry(true, source, value, this);
        } else {
            // todo add list support
            // check arbitrary object for property
            Entry result = getPropertyValue(name, object);
            if (result == null) {
                // no getter found, we have to check the fields
                result = getFieldValue(name, object, object.getClass());
            }
            return result;
        }
    }

    /**
     * Serch for the bean property with the given name. This only finds properties with
     * getters
     *
     * @param name   the name
     * @param object the object
     * @return value the value or null
     */
    private Entry getPropertyValue(String name, Object object) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass(), Object.class);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                if (propertyDescriptor.getName().equals(name)) {
                    try {
                        Object resolved = propertyDescriptor.getReadMethod().invoke(object, (Object[]) null);
                        Entry entry = new Entry(true, object, resolved, this);
                        entry.sourceProperty = name;
                        return entry;
                    } catch (Exception e) {
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
    private Entry getFieldValue(String name, Object object, Class cls) {
        try {
            Field field = cls.getDeclaredField(name);
            if (field != null) {
                boolean acc = field.isAccessible();
                field.setAccessible(true);
                Object result = field.get(object);
                field.setAccessible(acc);
                Entry entry = new Entry(true, object, result, this);
                entry.sourceProperty = name;
                return entry;
            }
        } catch (Exception e) {
        }
        // check superclass
        if (cls.getSuperclass() != null) {
            return getFieldValue(name, object, cls.getSuperclass());
        }
        return null;
    }

    /**
     * Get the keys registered in this context
     *
     * @return keys the keys
     */
    public Set<String> keySet() {
        return data.keySet();
    }

    public List<Entry> allEntries() {
        ArrayList<Entry> all = new ArrayList<Entry>();
        for (Map.Entry<String, Entry> entry : data.entrySet()) {
            all.add(entry.getValue());
            if (entry.getValue().getValue() instanceof PipelineContext) {
                all.addAll(((PipelineContext) entry.getValue().getValue()).allEntries());
            }
        }
        return all;
    }

    public static String resolve(String value, PipelineContext context) {
        if (value != null) {
            // check if we have to resolve the value
            String stringValue = value;
            stringValue = stringValue.trim();
            if (stringValue.contains("${")) {
                // multiple variable in term, we translate to string
                Matcher matcher = SPLITTER.matcher(stringValue);
                StringBuilder b = new StringBuilder();

                while (matcher.find()) {
                    String k = matcher.group();
                    if (k.startsWith("${")) {
                        Entry entry = context.getEntry(k);
                        if (entry != null) {
                            b.append(entry.toString());
                        } else {
                            b.append(k);
                        }
                    } else {
                        b.append(k);
                    }
                }
                return b.toString();
            }
        }
        return value;
    }


    /**
     * The context entries to resolve static/dynamic content
     */
    public static class Entry {
        boolean dynamic;
        Object source;
        PipelineContext context;
        private Object value;
        String sourceProperty;


        /**
         * Create a new entry
         *
         * @param dynamic is dynamic
         * @param source  the source
         * @param value   the value
         */
        Entry(boolean dynamic, Object source, Object value, PipelineContext context) {
//            if (value == null) throw new NullPointerException();
            this.dynamic = dynamic;
            this.value = value;
            this.source = source;
            this.context = context;
        }

        /**
         * returns true if this is a dynamic entry
         *
         * @return dynamic true if dynamic
         */
        public boolean isDynamic() {
            return dynamic;
        }

        /**
         * The resolved value
         *
         * @return value the resolved value
         */
        public Object getValue() {
            if (value != null) {
                // check if we have to resolve the value
                if (value instanceof String) {
                    String stringValue = (String) value;
                    stringValue = stringValue.trim();

                    if (stringValue.contains("${")) {
                        if (stringValue.indexOf("${") == stringValue.lastIndexOf("${") && stringValue.endsWith("}")) {
                            // single occurrence in the term
                            Entry entry = context.getEntry(stringValue);
                            if (entry != null) {
                                // update the source and make this
                                // dynamic if the target is dynamic as well


                                // catch the case where another file name is linked
                                // and the resovled value consists of another, unresolved valued.
                                // i.e ${x.y.input.name} -> ${input.name}. This
                                // becomes problematic when ${input.name} is actaully part of this
                                // context
                                Object entryvalue = entry.getValue();
                                if(entryvalue instanceof String){
                                    String es = (String) entryvalue;
                                    if (es.indexOf("${") == es.lastIndexOf("${") && es.endsWith("}")) {
                                        return value;
                                    }
                                }

                                this.dynamic = entry.isDynamic();
                                this.source = entry.getSource();
                                return entryvalue;
                            }
                        } else {
                            // multiple variable in term, we translate to string
                            Matcher matcher = SPLITTER.matcher(stringValue);
                            StringBuilder b = new StringBuilder();
                            boolean containsDynamic = false;
                            Object source = null;
                            int sources = 0;

                            while (matcher.find()) {
                                String k = matcher.group();
                                if (k.startsWith("${")) {
                                    Entry entry = context.getEntry(k);
                                    //
                                    if (entry != null) {
                                        containsDynamic |= entry.isDynamic();
                                        if (source == null && entry.isDynamic()) {
                                            source = entry.getSource();
                                            sources = 1;
                                        } else {
                                            if (source != entry.getSource() && entry.isDynamic()) {
                                                sources++;
                                            }
                                        }
                                        b.append(entry.toString());
                                    } else {
                                        // args .. null value!
                                        //throw new RuntimeException("Unable to resolve value for property "+k +" from term "+ stringValue);
                                        // set term as value ?!
                                        b = new StringBuilder();
                                        b.append(stringValue);
                                        containsDynamic = false;
                                        break;
                                    }

                                } else {
                                    b.append(k);
                                }
                            }

                            if (sources > 1 && b.toString().contains("${")) {
                                throw new RuntimeException("Translated string source " + stringValue + " has more than one dynamic source ! This is currently not supported !");

                            }
                            this.source = source == null ? this.source : source;
                            this.dynamic = containsDynamic | this.dynamic;
                            return b.toString();
                        }
                    }
                }
            }
            return value;
        }

        /**
         * Get the source of this value
         *
         * @return the source
         */

        public Object getSource() {
            return source;
        }

        @Override
        public String toString() {
            Object vv = getValue();
            return vv == null ? (value == null ? "null" : value.toString()) : vv.toString();
        }

        /**
         * Set the raw value
         *
         * @param value the raw value
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * Returns the raw, unresolved value
         *
         * @return raw value
         */
        public Object getRaw() {
            return value;
        }

        public boolean applyValue(Object value){
            if(sourceProperty == null) return false;
            if(!setPropertyValue(sourceProperty, source, value)){
                return setFieldValue(sourceProperty, source, source.getClass(), value );
            }
            return true;
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


    }
}
