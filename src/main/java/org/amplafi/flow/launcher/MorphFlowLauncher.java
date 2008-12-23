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

    private String flowLabel;

    public MorphFlowLauncher(String flowTypeName, FlowManagement flowManagement) {
        this(flowTypeName, null, flowManagement);
    }

    public MorphFlowLauncher(String flowTypeName, Map<String, String> initialFlowState,
            FlowManagement flowManagement) {
        super(flowManagement, initialFlowState);
        this.flowTypeName = flowTypeName;
    }

    @Override
    public FlowState call() {
        FlowState currentFlowState = getFlowManagement().getCurrentFlowState();
        currentFlowState.morphFlow(flowTypeName, getValuesMap());
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

}
