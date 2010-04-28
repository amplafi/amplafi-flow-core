/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package org.amplafi.flow.impl;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowStateLifecycle;
import org.amplafi.flow.FlowStateListener;
import org.amplafi.flow.FlowStepDirection;
import org.apache.commons.logging.Log;

/**
 * For debugging
 * @author patmoore
 *
 */
public class ChangeDebuggerFlowStateListener implements FlowStateListener {

    private Log log;

    /**
     * @see org.amplafi.flow.FlowStateListener#activityChange(org.amplafi.flow.impl.FlowStateImplementor, org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowStepDirection, org.amplafi.flow.FlowActivityPhase)
     */
    @Override
    public void activityChange(FlowStateImplementor flowState, FlowActivity flowActivity, FlowStepDirection flowStepDirection,
        FlowActivityPhase flowActivityPhase) {
        getLog().debug(flowState.getFlowPropertyProviderFullName()+" "+flowStepDirection+" phase="+flowActivityPhase+" to "+flowActivity);
    }

    /**
     * @see org.amplafi.flow.FlowStateListener#lifecycleChange(org.amplafi.flow.impl.FlowStateImplementor, org.amplafi.flow.FlowStateLifecycle)
     */
    @Override
    public void lifecycleChange(FlowStateImplementor flowState, FlowStateLifecycle previousFlowStateLifecycle) {
        getLog().debug(flowState.getFlowPropertyProviderFullName()+" "+flowState.getFlowStateLifecycle());
    }

    /**
     * @param log the log to set
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @return the log
     */
    public Log getLog() {
        return log;
    }

}
