/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

import java.util.ArrayList;
import java.util.List;

import org.amplafi.flow.flowproperty.FlowPropertyDefinition;


/**
 * A FlowActivity that can be used to clear other flowStates other than the current FlowState
 * which this {@link FlowActivity} is in.
 *
 */
public class ClearFlowsFlowActivity extends FlowActivityImpl {

    /**
     *
     */
    private static final String FLOWS = "flows";
    @Override
    protected void addStandardFlowPropertyDefinitions() {
        super.addStandardFlowPropertyDefinitions();
        addPropertyDefinitions(new FlowPropertyDefinition(FLOWS, List.class));
    }

    @Override
    public void initializeFlow() {
        super.initializeFlow();
        if ( isPropertyNotSet(FLOWS)) {
            // we don't want to try to clear ourselves.
            ArrayList<String> l = new ArrayList<String>();
            for(FlowState fs: this.getFlowManagement().getFlowStates()) {
                if ( getFlowState() != fs) {
                    l.add(fs.getLookupKey());
                }
            }
            initPropertyIfNull(FLOWS, l);
        }
    }
    @Override
    public FlowState finishFlow(FlowState currentNextFlowState) {
        FlowState nextFlowState = super.finishFlow(currentNextFlowState);
        List<String> flowStateLookupKeys = getProperty(FLOWS);
        String currentLookupKey = this.getFlowState().getLookupKey();
        for (String lookupKey: flowStateLookupKeys) {
            if ( lookupKey.equals(currentLookupKey) ||
                    nextFlowState != null && lookupKey.equals(nextFlowState.getLookupKey()) ) {
                continue;
            } else {
                getFlowManagement().dropFlowStateByLookupKey(lookupKey);
            }
        }
        return nextFlowState;
    }
}
