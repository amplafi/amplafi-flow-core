/**
 * Copyright 2006-2008 by Amplafi. All rights reserved. Confidential.
 */
package org.amplafi.flow.impl;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowLifecycleState;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowState;

/**
 * @author patmoore
 */
public interface FlowStateImplementor extends FlowState {
    <T> void setProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition, T value);

    <T> T getProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition);

    String getRawProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition);

    Long getRawLong(FlowActivity flowActivity, String key);
    @Deprecated
    String getRawProperty(String key);

    String getRawProperty(String namespace, String key);

    /**
     *
     * @param key
     * @param value
     * @return true if the value has changed.
     */
    @Deprecated
    boolean setRawProperty(String key, String value);

    /**
     * @param flowActivityImplementor
     * @param propertyDefinition
     */
    void initializeFlowProperty(FlowActivityImplementor flowActivityImplementor, FlowPropertyDefinition propertyDefinition);

    void initializeFlowProperties(FlowActivityImplementor flowActivity, Iterable<FlowPropertyDefinition> flowPropertyDefinitions);

    /**
     * get FlowActivity by position. It is preferred to use {@link #getActivity(String)}
     *
     * @param <T>
     * @param activityIndex
     * @return flowActivity
     */
    <T extends FlowActivity> T getActivity(int activityIndex);


    void setFlowLifecycleState(FlowLifecycleState flowLifecycleState);

    /**
     * Called when this flowstate no longer represents the current flow. Assume
     * that this FlowState may be G.C.'ed
     */
    void clearCache();

    void setCached(String namespace, String key, Object value);
    void setCached(FlowPropertyDefinition propertyDefinition, FlowActivity flowActivity, Object value);

    <T> T getCached(String namespace, String key);

    /**
     * @param flowManagement The flowManagement to set.
     */
    void setFlowManagement(FlowManagement flowManagement);


    /**
     * @param propertyDefinition
     * @param flowActivity
     * @return
     */
    <T> T getCached(FlowPropertyDefinition propertyDefinition, FlowActivity flowActivity);
}
