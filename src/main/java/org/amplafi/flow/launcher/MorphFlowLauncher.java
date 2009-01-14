package org.amplafi.flow.launcher;

import java.util.Map;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;
import org.apache.commons.lang.ObjectUtils;


/**
 * This FlowLauncher allows the flowstate to change the flow it is running.
 */

public class MorphFlowLauncher extends BaseFlowLauncher implements ListableFlowLauncher {

    private String flowTypeName;

    private String flowLabel;

    private String lookupKey;

    private Object keyExpression;

    public MorphFlowLauncher(String flowTypeName, String lookupKey, FlowManagement flowManagement) {
        this(flowTypeName, lookupKey, null, flowManagement);
    }

    public MorphFlowLauncher(String flowTypeName, String lookupKey,
            Map<String, String> initialFlowState, FlowManagement flowManagement) {
        super(flowManagement, initialFlowState);
        this.flowTypeName = flowTypeName;
        this.lookupKey = lookupKey;
    }

    @Override
    public FlowState call() {
        FlowState currentFlowState = getFlowState();
        currentFlowState.morphFlow(flowTypeName, getValuesMap());
        return currentFlowState;
    }

    /**
     * @return the flow lookupKey that will be morphed.
     */
    public FlowState getFlowState() {
        return getFlowManagement().getFlowState(lookupKey);
    }


    @Override
    public String getFlowLabel() {
        if ( flowLabel == null ) {
            Flow flow = getFlowManagement().getFlowDefinition(flowTypeName);
            flowLabel = flow.getLinkTitle();
        }
        return flowLabel;
    }

    @Override
    public String getFlowTypeName() {
        return flowTypeName;
    }

    public void setKeyExpression(Object keyExpression) {
        this.keyExpression = keyExpression;
    }

    public Object getKeyExpression() {
        return keyExpression;
    }

    public boolean hasKey(Object key) {
        return ObjectUtils.equals(this.keyExpression, key);
    }
}
