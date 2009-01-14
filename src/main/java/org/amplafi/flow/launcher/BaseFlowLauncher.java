/**
 * Copyright 2006-8 by Amplafi, Inc.
 */
package org.amplafi.flow.launcher;

import java.util.HashMap;
import java.util.Map;

import org.amplafi.flow.FlowManagement;

/**
 *
 *
 */
public abstract class BaseFlowLauncher implements FlowLauncher {
    private transient FlowManagement flowManagement;
    private String returnToFlowLookupKey;
    private Map<String, String> valuesMap;
    protected BaseFlowLauncher() {

    }
    /**
     * @param flowManagement
     */
    public BaseFlowLauncher(FlowManagement flowManagement, Map<String, String> valuesMap) {
        this.flowManagement = flowManagement;
        this.valuesMap = new HashMap<String, String>();
        if ( valuesMap != null) {
            this.valuesMap.putAll(valuesMap);
        }
    }
    @Override
    public void setFlowManagement(FlowManagement sessionFlowManagement) {
        this.flowManagement = sessionFlowManagement;
    }
    public FlowManagement getFlowManagement() {
        if(flowManagement == null) {
            throw new IllegalStateException("no flowmanagement object supplied!");
        }
        return flowManagement;
    }
    /**
     * @param returnToFlowLookupKey the returnToFlowLookupKey to set
     */
    public void setReturnToFlowLookupKey(String returnToFlowLookupKey) {
        this.returnToFlowLookupKey = returnToFlowLookupKey;
    }
    /**
     * @return the returnToFlowLookupKey
     */
    public String getReturnToFlowLookupKey() {
        return returnToFlowLookupKey;
    }
    public Map<String, String> getValuesMap() {
        if ( this.valuesMap == null) {
            this.valuesMap = new HashMap<String, String>();
        }
        return this.valuesMap;
    }
    @Override
    public Map<String, String> getInitialFlowState() {
        return getValuesMap();
    }
    /**
     * @see org.amplafi.flow.launcher.FlowLauncher#put(java.lang.String, java.lang.String)
     */
    @Override
    public String put(String key, String value) {
        return this.getValuesMap().put(key, value);
    }
    /**
     * @see org.amplafi.flow.launcher.FlowLauncher#putIfAbsent(java.lang.String, java.lang.String)
     */
    @Override
    public String putIfAbsent(String key, String defaultValue) {
        if ( !this.getValuesMap().containsKey(key)) {
            return this.getValuesMap().put(key, defaultValue);
        } else {
            return null;
        }
    }
    public String getListDisplayValue() {
        return getFlowLabel();
    }

}
