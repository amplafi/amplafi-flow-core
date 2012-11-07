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

package org.amplafi.flow;

import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowStateImplementor;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.amplafi.flow.FlowStateLifecycle.*;
import static org.testng.Assert.*;

/**
 * Test {@link FlowStateLifecycle}.
 * @author andyhot
 */
public class TestFlowStateLifecycle {

    @Test(dataProvider="notAllowed", expectedExceptions={IllegalStateException.class})
    public void testNotAllowed(FlowStateLifecycle previous, FlowStateLifecycle next) {
        STATE_CHECKER.checkAllowed(previous, next);
    }

    @DataProvider(name="notAllowed")
    protected Object[][] getNotAllowed() {
        return new Object[][] {
            {canceled, started},
            {successful, started},
            {failed, started},
            {started, created},
        };
    }

    /**
     * Make sure that {@link FlowStateListener} are getting notified.
     */
    @Test
    public void testFlowLifecycleListener() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String flowTypeName = flowTestingUtils.addFlowDefinition(new FlowActivityImpl().initInvisible(false));
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowStateLifecycleListenerImpl flowLifecycleStateListener = new FlowStateLifecycleListenerImpl();
        flowManagement.addFlowStateListener(flowLifecycleStateListener);
        FlowState flowState = flowManagement.startFlowState(flowTypeName, true, null);
        assertEquals(flowLifecycleStateListener.last, starting);
        assertEquals(flowLifecycleStateListener.current, started);
        flowState.finishFlow();
        assertEquals(flowLifecycleStateListener.current, successful);
    }
    static class FlowStateLifecycleListenerImpl implements FlowStateListener {
        FlowStateLifecycle last;
        FlowStateLifecycle current;
        @Override
        public void lifecycleChange(FlowStateImplementor flowState, FlowStateLifecycle previousFlowLifecycleState) {
            assertNotSame(flowState.getFlowStateLifecycle(), previousFlowLifecycleState);
            assertSame(current, previousFlowLifecycleState);
            last = previousFlowLifecycleState;
            current = flowState.getFlowStateLifecycle();
        }
        /**
         * @see org.amplafi.flow.FlowStateListener#activityChange(org.amplafi.flow.impl.FlowStateImplementor, org.amplafi.flow.FlowActivity, FlowStepDirection, FlowActivityPhase)
         */
        @Override
        public void activityChange(FlowStateImplementor flowState, FlowActivity flowActivity, FlowStepDirection flowStepDirection, FlowActivityPhase flowActivityPhase) {

        }
    }
}
