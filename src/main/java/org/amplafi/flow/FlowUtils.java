package org.amplafi.flow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.json.JSONStringer;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;



/**
 *
 */
// TODO need to figure out a way to get rid of this as a utility class -- fold it into something else.
public class FlowUtils {

    public static final FlowUtils INSTANCE = new FlowUtils();
    /**
     * Copy state from old FlowState to the new one. Only copy supplied keys.
     * @param old
     * @param keys if not supplied then the entire flowstate is copied.
     * @return the new state
     */
    public Map<String, String> copyState(FlowState old, String...keys) {
        Map<String, String> ret;
        if (keys==null || keys.length == 0) {
            ret = new LinkedHashMap<String, String>(old.getFlowValuesMap().getAsStringMap(true, true));
        } else {
            ret = new LinkedHashMap<String, String>();
            for (String key: keys) {
                ret.put(key, old.getRawProperty(key));
            }
        }
        return ret;
    }
    public void copyMapToFlowState(FlowState flowState, Map<String, String> overrideValues, String...keys) {
        if ( MapUtils.isNotEmpty(overrideValues)) {
            Map<String, String> ret;
            if (keys==null || keys.length == 0) {
                ret = overrideValues;
            } else {
                ret = new LinkedHashMap<String, String>();
                for (String key: keys) {
                    ret.put(key, overrideValues.get(key));
                }
            }
            for(Map.Entry<String, String> entry: ret.entrySet()) {
                flowState.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }
    /**
     * Convert a simple class name to a default property name.
     * For example, com.amplafi.core.BroadcastMessage has a default property name of
     * "broadcastMessage".
     * @param dataClass
     * @return the default property name.
     */
    public String toPropertyName(Class<?> dataClass) {
        return FlowPropertyDefinition.toPropertyName(dataClass);
    }
    /**
     * Converts a {@link Map} into a {@link List} of <em>key='value'</em> strings.
     *
     * @param map The map to convert
     * @return list of strings in <em>key='value'</em> form
     */
    public List<String> createInitialValues(Map<String, String> map) {
        List<String> result = new ArrayList<String>();
        for(Map.Entry<String, String> entry: map.entrySet()) {
            addInitialValues(result, entry.getKey(), entry.getValue());
        }
        return result;
    }
    /**
     * @param values
     * @param keyValue
     */
    public void addInitialValues(List<String> values, Object keyValue) {
        addInitialValues(values, keyValue, keyValue);
    }

    /**
     * Converts each passed key value pair to the form: <em>key='value'</em>
     * and adds the result into the given list.
     *
     * @param values the list in which to include the key value pairs
     * @param keyValues the key value pairs
     */
    public static void addInitialValues(List<String> values, Object... keyValues) {
        for(int i = 0; i < keyValues.length; i+=2) {
            if ( keyValues[i] != null) {
                values.add(keyValues[i].toString()+"='"+ObjectUtils.toString(keyValues[i+1])+"'");
            }
        }
    }
    public String toFlowString(Object value) {
        String strV = null;
        if ( value instanceof String ) {
            strV = (String) value;
        } else if ( value != null ) {
            JSONStringer stringer = new JSONStringer();
            stringer.value(value);
            strV = stringer.toString();
            if (strV.startsWith("\"") && strV.endsWith("\"")) {
                // trim off unneeded " that appear when handling simple objects.
                strV = strV.substring(1, strV.length()-1);
            }
        }
        return strV;
    }
    public String toLowerCase(String flowName) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < flowName.length(); i++) {
            if ( Character.isUpperCase(flowName.charAt(i)) ) {
                if ( i > 0 ) {
                    sb.append('-');
                }
                sb.append(Character.toLowerCase(flowName.charAt(i)));
            } else {
                sb.append(flowName.charAt(i));
            }
        }
        return sb.toString();
    }
    public List<String> createInitialValues(Object... keyValues) {
        Map<String, String> map = createState(keyValues);
        return this.createInitialValues(map);
    }
    /**
     * Utility to create the initial state to be used when starting flows.
     * Users should supply a list of key-value object pairs.<p/>
     * If a value is null, nothing is added to the map.<p/>
     *
     * Before being added to the map, toString() is called on all objects.
     * {@link java.util.List Lists} are a special case - toString() is called
     * on all elements and the results are joined together in a newline
     * delimited string.
     *
     *
     * @param objects the key-value pairs
     * @return the map with the state info, or an empty map if no arguments passed
     */
    public Map<String, String> createState(Object...objects) {
        Map<String, String> ret = new LinkedHashMap<String, String>();
        if (objects==null) {
            return ret;
        }
        int evenSize = (objects.length>>1)<<1;
        for (int i=0; i<evenSize; i+=2) {
            String key;
            if ( objects[i] instanceof Class) {
                key = toPropertyName((Class<?>) objects[i]);
            } else {
                key = objects[i].toString();
            }
            Object value = objects[i+1];
            if ( value != null ) {
                String strV = toFlowString(value);
                ret.put(key, strV);
            }
        }
        return ret;
    }
}
