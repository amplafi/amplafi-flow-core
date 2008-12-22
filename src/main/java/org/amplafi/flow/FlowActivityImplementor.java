/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

import java.util.Map;

/**
 * @author patmoore
 *
 */
public interface FlowActivityImplementor extends FlowActivity {
    /**
     * Only called if value != oldValue.
     *
     * @param flowActivityName
     * @param key
     * @param value
     * @param oldValue
     * @return what the value should be. Usually just return the value
     *         parameter.
     */
    public String propertyChange(String flowActivityName, String key, String value,
        String oldValue);

    /**
     * @return
     */
    public FlowActivityImplementor createInstance();

    /**
     * @param nextFlow
     * @return
     */
    public String resolve(String nextFlow);

    /**
     *
     */
    public void processDefinitions();
    /**
     * If the property has no value stored in the flowState's keyvalueMap then
     * put the supplied value in it.
     *
     * @param key
     * @param value
     * @see #isPropertyNotSet(String)
     */
    public void initPropertyIfNull(String key, Object value) ;

    public void initPropertyIfBlank(String key, Object value);

    public void setPropertyDefinitions(Map<String, FlowPropertyDefinition> properties);

    public void addPropertyDefinition(FlowPropertyDefinition definition);

    public void addPropertyDefinitions(FlowPropertyDefinition... definitions);

    public void addPropertyDefinitions(Iterable<FlowPropertyDefinition> definitions);


    public String getRawProperty(String key);
    /**
     * set a value with key specified in either the flowActivity specific values
     * (if such a flowactivity specific value is set already) or the global flow
     * values otherwise.
     *
     * @param key
     * @param value
     * @return {@link FlowState#setRawProperty(FlowActivity, String, String)}
     */
    public boolean setRawProperty(String key, String value);
}
