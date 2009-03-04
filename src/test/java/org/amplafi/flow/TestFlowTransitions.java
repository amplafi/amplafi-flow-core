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

import static org.amplafi.flow.FlowConstants.*;
import static org.testng.Assert.*;

import java.util.Map;

import org.amplafi.flow.flowproperty.AddToMapFlowPropertyValueProvider;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.translator.BaseFlowTranslatorResolver;
import org.amplafi.flow.translator.ShortFlowTranslator;
import org.amplafi.flow.impl.FlowImpl;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.BaseFlowManagement;
import org.amplafi.flow.impl.FlowDefinitionsManagerImpl;
import org.testng.annotations.Test;

/**
 * @author patmoore
 *
 */
public class TestFlowTransitions {

    private static final String FLOW_TYPE_1 = "ftype1";
    private static final String FLOW_TYPE_2 = "ftype2";
    @Test
    public void testSimpleFlowTransitionMapChecking() {
        FlowImpl flow = new FlowImpl(FLOW_TYPE_1);
        FlowActivityImpl fa1 = new FlowActivityImpl();
        FlowPropertyDefinition definition = fa1.getPropertyDefinition(FSFLOW_TRANSITIONS);
        assertNull(definition);
        flow.addActivity(fa1);
        definition = fa1.getPropertyDefinition(FSFLOW_TRANSITIONS);
        assertNotNull(definition);
        String returnToFlowLookupKey = null;
        definition.setFlowPropertyValueProvider(new AddToMapFlowPropertyValueProvider<String,FlowTransition>(new FlowTransition("foo", FLOW_TYPE_2, "foo", TransitionType.alternate, null)));
        BaseFlowManagement baseFlowManagement = getFlowManagement(flow);
        FlowState flowState = baseFlowManagement.startFlowState(FLOW_TYPE_1, false, null, returnToFlowLookupKey);

        Map<String, FlowTransition> propValue = flowState.getCurrentActivity().getProperty(FSFLOW_TRANSITIONS);
        assertTrue( propValue.keySet().contains("foo"));
    }

    @Test
    public void testReturnToFlow() {
        FlowImpl flow1 = new FlowImpl(FLOW_TYPE_1);
        String defaultAfterPage1 = "end-of-"+FLOW_TYPE_1;
        String defaultPage1 = "page-of-"+FLOW_TYPE_1;
        flow1.setPageName(defaultPage1);
        flow1.setDefaultAfterPage(defaultAfterPage1);
        FlowActivityImpl fa1 = new FlowActivityImpl();
        flow1.addActivity(fa1);

        FlowImpl flow2 = new FlowImpl(FLOW_TYPE_2);
        String defaultAfterPage2 = "end-of-"+FLOW_TYPE_2;
        String defaultPage2 = "page-of-"+FLOW_TYPE_2;
        flow2.setPageName(defaultPage2);
        flow2.setDefaultAfterPage(defaultAfterPage2);
        FlowActivityImpl fa2_1 = new FlowActivityImpl();
        flow2.addActivity(fa2_1);
        Object returnToFlowLookupKey = true;
        BaseFlowManagement baseFlowManagement = getFlowManagement(flow1, flow2);
        FlowState flowState1 = baseFlowManagement.startFlowState(FLOW_TYPE_1, true, null, returnToFlowLookupKey);
        assertEquals(flowState1.getCurrentPage(), defaultPage1);
        FlowState flowState2 = baseFlowManagement.startFlowState(FLOW_TYPE_2, true, null, true);
        String lookupKey1 = flowState2.getPropertyAsObject(FSRETURN_TO_FLOW);
        assertEquals(flowState2.getCurrentPage(), defaultPage2);
        assertEquals(flowState1.getLookupKey(), lookupKey1);
        String pageName = flowState2.finishFlow();
        assertEquals(pageName, defaultPage1);
        FlowState flowState1_again = baseFlowManagement.getCurrentFlowState();
        assertEquals(flowState1_again.getLookupKey(), flowState1.getLookupKey());
        flowState1_again.finishFlow();
        FlowState nothing = baseFlowManagement.getCurrentFlowState();
        assertNull(nothing);
    }
    /**
     * @param flow
     * @return
     */
    private BaseFlowManagement getFlowManagement(Flow... flow) {
        BaseFlowManagement baseFlowManagement = new BaseFlowManagement();
        baseFlowManagement.setFlowTranslatorResolver(getFlowTranslatorResolver());
        FlowDefinitionsManagerImpl flowDefinitionsManager = new FlowDefinitionsManagerImpl();
        flowDefinitionsManager.initializeService();
        baseFlowManagement.setFlowDefinitionsManager(flowDefinitionsManager);
        flowDefinitionsManager.setFlowTranslatorResolver(getFlowTranslatorResolver());
        flowDefinitionsManager.addDefinitions(flow);
        return baseFlowManagement;
    }
    private BaseFlowTranslatorResolver getFlowTranslatorResolver() {
        BaseFlowTranslatorResolver flowTranslatorResolver = new BaseFlowTranslatorResolver();
        flowTranslatorResolver.addStandardFlowTranslators();
        flowTranslatorResolver.initializeService();
        flowTranslatorResolver.addFlowTranslator(new ShortFlowTranslator());
        return flowTranslatorResolver;
    }
}
