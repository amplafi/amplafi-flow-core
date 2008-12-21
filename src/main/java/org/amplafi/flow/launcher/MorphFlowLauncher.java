package org.amplafi.flow.launcher;

import java.util.Map;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;


/**
 * This FlowLauncher allows the flowstate to change the flow it is running.
 */

public class MorphFlowLauncher extends BaseFlowLauncher {

    private String flowTypeName;

    private Map<String, String> initialFlowState;

    private String flowLabel;

    public MorphFlowLauncher(String flowTypeName, FlowManagement flowManagement) {
        this(flowTypeName, null, flowManagement);
    }

    public MorphFlowLauncher(String flowTypeName, Map<String, String> initialFlowState,
            FlowManagement flowManagement) {
        super(flowManagement);
        this.flowTypeName = flowTypeName;
        this.initialFlowState = initialFlowState;
    }

    @Override
    public FlowState call() {
        FlowState currentFlowState = getFlowManagement().getCurrentFlowState();
        currentFlowState.morphFlow(flowTypeName, initialFlowState);
        return currentFlowState;
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

    @Override
    public Map<String, String> getInitialFlowState() {
        return initialFlowState;
    }

}
