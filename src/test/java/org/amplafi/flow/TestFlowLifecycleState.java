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
import static org.amplafi.flow.FlowLifecycleState.*;
import static org.testng.Assert.*;

/**
 * Test {@link FlowLifecycleState}.
 * @author andyhot
 */
public class TestFlowLifecycleState {

    @Test(dataProvider="notAllowed", expectedExceptions={IllegalStateException.class})
    public void testNotAllowed(FlowLifecycleState previous, FlowLifecycleState next) {
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
     * Make sure that {@link FlowLifecycleStateListener} are getting notified.
     */
    @Test
    public void testFlowLifecycleListener() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String flowTypeName = flowTestingUtils.addFlowDefinition(new FlowActivityImpl());
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowLifecycleStateListenerImpl flowLifecycleStateListener = new FlowLifecycleStateListenerImpl();
        flowManagement.addFlowLifecycleListener(flowLifecycleStateListener);
        FlowState flowState = flowManagement.startFlowState(flowTypeName, true, null, null);
        assertEquals(flowLifecycleStateListener.last, starting);
        assertEquals(flowLifecycleStateListener.current, started);
        flowState.finishFlow();
        assertEquals(flowLifecycleStateListener.current, successful);
    }
    class FlowLifecycleStateListenerImpl implements FlowLifecycleStateListener {
        FlowLifecycleState last;
        FlowLifecycleState current;
        @Override
        public void lifecycleChange(FlowStateImplementor flowState, FlowLifecycleState previousFlowLifecycleState) {
            assertNotSame(flowState.getFlowLifecycleState(), previousFlowLifecycleState);
            assertSame(current, previousFlowLifecycleState);
            last = previousFlowLifecycleState;
            current = flowState.getFlowLifecycleState();
        }
    };
}
