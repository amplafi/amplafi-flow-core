/*
 * Created on May 31, 2007
 * Copyright 2006 by Patrick Moore
 */
package org.amplafi.flow.launcher;

import java.util.Map;

import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;



/**
 * This FlowLauncher continues a previously created FlowState.
 * @author Patrick Moore
 */
public class ContinueFlowLauncher extends BaseFlowLauncher implements ListableFlowLauncher {

    private static final long serialVersionUID = -1221490458104629351L;
    private String lookupKey;
    private String flowTypeName;

    public ContinueFlowLauncher() {

    }
    public ContinueFlowLauncher(String lookupKey, FlowManagement flowManagement) {
        super(flowManagement, null);
        this.lookupKey = lookupKey;
    }
    public ContinueFlowLauncher(FlowState flowState, FlowManagement flowManagement) {
        super(flowManagement, null);
        this.lookupKey = flowState.getLookupKey();
        this.flowTypeName = flowState.getFlowTypeName();
    }

    public FlowState getFlowState() {
        return getFlowManagement().getFlowState(lookupKey);
    }

    /**
     * May return no FlowState if the FlowState represented by
     * the lookupKey was already completed.
     * @see org.amplafi.flow.launcher.FlowLauncher#call()
     * @return the flow that was continued
     */
    public FlowState call() {
        return getFlowManagement().continueFlowState(lookupKey, true, this.getValuesMap());
    }
    public String getFlowLabel() {
        if ( getFlowState() == null ) {
            return "";
        } else {
            return getFlowState().getActiveFlowLabel();
        }
    }

    public void setFlowLabel(String label) {
        if ( getFlowState() != null ) {
            getFlowState().setActiveFlowLabel(label);
        }
    }

    public Object getKeyExpression() {
        return this.lookupKey;
    }

    public boolean hasKey(Object key) {
        return this.lookupKey.equals(key);
    }

    @Override
    public String getFlowTypeName() {
        return this.flowTypeName;
    }

    @Override
    public Map<String, String> getInitialFlowState() {
        return getFlowState().getFlowValuesMap();
    }
}
