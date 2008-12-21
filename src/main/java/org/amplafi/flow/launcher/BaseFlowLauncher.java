/**
 * Copyright 2006-8 by Amplafi, Inc.
 */
package org.amplafi.flow.launcher;

import org.amplafi.flow.FlowManagement;

/**
 *
 *
 */
public abstract class BaseFlowLauncher implements FlowLauncher {
    private transient FlowManagement flowManagement;
    private String returnToFlowLookupKey;
    protected BaseFlowLauncher() {

    }
    /**
     * @param flowManagement
     */
    public BaseFlowLauncher(FlowManagement flowManagement) {
        this.flowManagement = flowManagement;
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
}
