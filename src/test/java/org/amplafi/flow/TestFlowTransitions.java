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
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.flowproperty.PropertyScope;
import org.amplafi.flow.flowproperty.PropertyUsage;
import org.amplafi.flow.impl.FlowImpl;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowStateImplementor;
import org.amplafi.flow.impl.TransitionFlowActivity;
import org.amplafi.flow.translator.BooleanFlowTranslator;
import org.easymock.EasyMock;
import org.testng.annotations.Test;
import static org.amplafi.flow.flowproperty.PropertyScope.*;
import static org.easymock.EasyMock.*;
import static com.sworddance.util.CUtilities.*;
/**
 * Tests to see that transitioning between flows ( other than morphing ) happens correctly.
 *
 * @author patmoore
 *
 */
public class TestFlowTransitions {

    /**
     *
     */
    private static final String A_VALUE_THAT_IS_COPIED_BACK = "a value that is copied back";

    private static final String FLOW_TYPE_1 = "ftype1";

    private static final String FLOW_TYPE_2 = "ftype2";

    private static final String FLOW_TYPE_3 = "ftype3";

    private static final boolean TEST_ENABLED = true;

    @Test(enabled = TEST_ENABLED)
    public void testSimpleFlowTransitionMapChecking() {
        {
            FlowImpl flow = new FlowImpl(FLOW_TYPE_1);
            FlowActivityImpl fa1 = new FlowActivityImpl().initInvisible(false);
            FlowPropertyDefinitionImplementor definition = fa1.getFlowPropertyDefinition(FSFLOW_TRANSITIONS);
            assertNull(definition);
            flow.addActivity(fa1);
            definition = fa1.getFlowPropertyDefinition(FSFLOW_TRANSITIONS);
            assertNotNull(definition);
        }
        {
            // also helps test overriding existing properties
            FlowImpl flow = new FlowImpl(FLOW_TYPE_1);
            FlowActivityImpl fa1 = new FlowActivityImpl().initInvisible(false);
            flow.addActivity(fa1);
            FlowPropertyDefinitionImplementor definition = fa1.getFlowPropertyDefinition(FSFLOW_TRANSITIONS);
            fa1.addPropertyDefinitions(new FlowPropertyDefinitionBuilder(definition).initFlowPropertyValueProvider(
                new AddToMapFlowPropertyValueProvider<FlowPropertyProvider, String, FlowTransition>(new FlowTransition("foo", FLOW_TYPE_2, "foo",
                    TransitionType.alternate, null))));
            definition = fa1.getFlowPropertyDefinition(FSFLOW_TRANSITIONS);
            assertNotNull(definition);
            FlowManagement baseFlowManagement = getFlowManagement(flow);
            FlowState flowState = baseFlowManagement.startFlowState(FLOW_TYPE_1, false, null);

            Map<String, FlowTransition> propValue = flowState.getCurrentActivity().getProperty(FSFLOW_TRANSITIONS);
            assertTrue(propValue.keySet().contains("foo"));

        }
    }

    /**
     * start one flow then a subflow,
     * set a value
     * finish the subflow
     * check to make sure the flow returns to the original flow
     * check to make sure the altered value is returned to
     */
    @Test(enabled=false) // 3 sept 2013 PAT: disabled because rules changed as to how undeclared properties can flow through flows. Need to re-enable
    public void testReturnToFlow() {
        FlowImpl mainFlow = new FlowImpl(FLOW_TYPE_1);
        String defaultAfterPage1 = "default-after-page-for-"+FLOW_TYPE_1;
        String defaultPage1 = "page-of-"+FLOW_TYPE_1;
        mainFlow.setPageName(defaultPage1);
        mainFlow.setDefaultAfterPage(defaultAfterPage1);
        FlowActivityImpl fa1 = new FlowActivityImpl().initInvisible(false);
        FlowPropertyDefinitionBuilder copiedBackProperty = new FlowPropertyDefinitionBuilder("copiedBackProperty").initAccess(PropertyScope.flowLocal,
            PropertyUsage.io);
        fa1.addPropertyDefinitions(copiedBackProperty);
        mainFlow.addActivity(fa1);

        FlowImpl subFlow = new FlowImpl(FLOW_TYPE_2);
        String defaultAfterPage2 = "default-after-page-for-"+FLOW_TYPE_2;
        String defaultPage2 = "page-of-"+FLOW_TYPE_2;
        subFlow.setPageName(defaultPage2);
        subFlow.setDefaultAfterPage(defaultAfterPage2);
        FlowActivityImpl fa2_1 = new FlowActivityImpl().initInvisible(false);
        fa2_1.addPropertyDefinitions(new FlowPropertyDefinitionBuilder(copiedBackProperty));
        subFlow.addActivity(fa2_1);
        subFlow.addActivity(new TransitionFlowActivity());

        FlowImpl continuedFlow = new FlowImpl(FLOW_TYPE_3);
        String defaultAfterPage3 = "default-after-page-for-"+FLOW_TYPE_3;
        String defaultPage3 = "page-of-"+FLOW_TYPE_3;
        continuedFlow.setPageName(defaultPage3);
        continuedFlow.setDefaultAfterPage(defaultAfterPage3);
        FlowActivityImpl fa3_1 = new FlowActivityImpl().initInvisible(false);
        continuedFlow.addActivity(fa3_1);

        Object returnToFlowLookupKey = true;
        FlowManagement baseFlowManagement = getFlowManagement(mainFlow, subFlow, continuedFlow);
        FlowState flowState1 = baseFlowManagement.startFlowState(FLOW_TYPE_1, true, null, returnToFlowLookupKey);
        flowState1.setProperty(copiedBackProperty.getName(), A_VALUE_THAT_IS_COPIED_BACK);
        assertEquals(flowState1.getProperty(copiedBackProperty.getName()), A_VALUE_THAT_IS_COPIED_BACK);
        assertEquals(flowState1.getCurrentPage(), defaultPage1);
        FlowState flowState2 = baseFlowManagement.startFlowState(FLOW_TYPE_2, true, null, true);
        assertEquals(flowState2.getCurrentPage(), defaultPage2);
        String lookupKey1 = flowState2.getProperty(FSRETURN_TO_FLOW);
        assertEquals(flowState2.getFlowTypeName(), FLOW_TYPE_2, flowState2.toString());
        assertEquals(flowState1.getLookupKey(), lookupKey1, "the child flow does not have the parent flow as the return-to-flow ");
        flowState2.setProperty(FSNEXT_FLOW, FLOW_TYPE_3);
        String pageName = flowState2.finishFlow();

        FlowState flowState3 = baseFlowManagement.getCurrentFlowState();
        assertEquals(flowState3.getFlowTypeName(), FLOW_TYPE_3, flowState3.toString());
        assertEquals(flowState3.getProperty(copiedBackProperty.getName()), A_VALUE_THAT_IS_COPIED_BACK);
        assertEquals(pageName, defaultPage3, "the child flow when it completed did not redirect to the parent flow's page. flowState2="+flowState2);
        lookupKey1 = flowState3.getProperty(FSRETURN_TO_FLOW);
        assertEquals(flowState1.getLookupKey(), lookupKey1, "the child flow does not have the parent flow as the return-to-flow ");
        pageName = flowState3.finishFlow();

        assertEquals(pageName, defaultPage1, "the child flow when it completed did not redirect to the parent flow's page. flowState2="+flowState2);
        FlowState flowState1_again = baseFlowManagement.getCurrentFlowState();
        assertEquals(flowState1_again.getLookupKey(), flowState1.getLookupKey());
        // TODO: 3 Oct 2010 (pat) revisit copy back from callee in the future. Right now, too much chance for mischief from callee. Caller should select only the values desired.
//        assertEquals(flowState1_again.getProperty(copiedBackProperty.getName()), A_VALUE_THAT_IS_COPIED_BACK);
        flowState1_again.finishFlow();
        FlowState nothing = baseFlowManagement.getCurrentFlowState();
        assertNull(nothing);
    }

    /**
     * Test to see how properties are cleared/copied during flow transitions.
     * <ul>
     * <li>make sure that {@link #flowLocal} is respected.</li>
     * <li>make sure that cache is cleared on flow completion.</li>
     * </ul>
     */
    @Test(enabled=false) // 3 sept 2013 PAT: disabled because rules changed as to how undeclared properties can flow through flows. Need to re-enable
    public void testAvoidConflictsOnFlowTransitions() {
        FlowActivityImpl flowActivity1 = new FlowActivityImpl().initInvisible(false);
        // initialized by "first" flow ignored by second flow.
        final String initializedByFirst = "initializedByFirst";
        flowActivity1.addPropertyDefinitions(new FlowPropertyDefinitionBuilder(initializedByFirst).initPropertyUsage(PropertyUsage.initialize));

        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.addFlowDefinition("first", flowActivity1, new TransitionFlowActivity(null, "second", TransitionType.normal));

        FlowActivityImpl flowActivity2 = new FlowActivityImpl().initInvisible(false);
        // this property name is unknown to "first" flow so "first" flow should not affect this property value at all.
        // for second flow, the property is flowLocal/ internalState so the setting should only affect the flowLocal copy.
        String privatePropertyForSecondFlow = "privateForSecond";
        String globalSettingForSecondFlowPrivateProperty = "global_for_privateForSecond";
        FlowPropertyDefinitionBuilder flowPropertyDefinition_secondflow_prop0 = new FlowPropertyDefinitionBuilder(privatePropertyForSecondFlow,
            Boolean.class).initAccess(flowLocal, PropertyUsage.internalState);
        // first flow doesn't understand this property but it sets it for the second flow to use.
        String opaqueSecondFlowProperty = "secondFlowProperty";
        flowActivity2.addPropertyDefinitions(flowPropertyDefinition_secondflow_prop0, new FlowPropertyDefinitionBuilder(opaqueSecondFlowProperty,
            String.class).initPropertyScope(flowLocal).initPropertyUsage(PropertyUsage.io));
        flowTestingUtils.addFlowDefinition("second", flowActivity2);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();

        FlowStateImplementor flowState = flowManagement.startFlowState("first", true, FlowUtils.INSTANCE.createState(privatePropertyForSecondFlow,
            globalSettingForSecondFlowPrivateProperty, initializedByFirst, "ShouldBeIgnored"), false);
        String opaqueValuePassedFromFirstToSecond = "opaque";
        Map<String, String> trustedValues = createMap(opaqueSecondFlowProperty, opaqueValuePassedFromFirstToSecond);
        flowState.copyTrustedValuesMapToFlowState(trustedValues);
        assertEquals(flowState.getProperty(initializedByFirst, String.class), null, "flowState=" + flowState);
        String propertyValueInitializedByFirst = "realvalue";
        flowState.setProperty(initializedByFirst, propertyValueInitializedByFirst);
        flowTestingUtils.advanceToEnd(flowState);
        FlowStateImplementor nextFlowState = flowManagement.getCurrentFlowState();
        assertNotNull(nextFlowState);
        // flowLocal namespace ignored the passed setting
        assertNull(nextFlowState.getProperty(privatePropertyForSecondFlow, Boolean.class), "nextFlowState=" + nextFlowState);
        String privatePropertyValueInSecondFlow = "true";
        nextFlowState.setProperty(privatePropertyForSecondFlow, privatePropertyValueInSecondFlow);
        assertEquals(nextFlowState.getFlowTypeName(), "second");
        // but it is still there for others.
        assertEquals(nextFlowState.getRawProperty((String) null, privatePropertyForSecondFlow), globalSettingForSecondFlowPrivateProperty,
            "nextFlowState=" + nextFlowState);
        assertEquals(nextFlowState.getProperty(opaqueSecondFlowProperty, String.class), opaqueValuePassedFromFirstToSecond, "looking at="
            + opaqueSecondFlowProperty + "  nextFlowState=" + nextFlowState);
        assertEquals(nextFlowState.getProperty(privatePropertyForSecondFlow), Boolean.parseBoolean(privatePropertyValueInSecondFlow), "looking at="
            + privatePropertyForSecondFlow + "  nextFlowState=" + nextFlowState);
        assertEquals(nextFlowState.getRawProperty((String) null, initializedByFirst), propertyValueInitializedByFirst, "nextFlowState="
            + nextFlowState);
        flowTestingUtils.advanceToEnd(flowState);

    }

    /**
     * Test to make sure that a {@link FlowActivityImpl} returns true (advance to next
     * {@link FlowActivity} ) in {@link FlowActivityImpl#activate(FlowStepDirection)} if there is no
     * page or component name.
     */
    @Test(enabled = TEST_ENABLED)
    public void testTransitionActivate() {
        FlowActivityImpl activity = new FlowActivityImpl();
        FlowImplementor flow = EasyMock.createMock(FlowImplementor.class);
        FlowStateImplementor flowState = EasyMock.createNiceMock(FlowStateImplementor.class);
        expect(flow.getFlowState()).andReturn(flowState).anyTimes();
        FlowPropertyDefinitionImplementor pageNameDefinition = new FlowPropertyDefinitionBuilder(FlowConstants.FSPAGE_NAME, String.class).toFlowPropertyDefinition();
        expect(flow.getFlowPropertyDefinition(FlowConstants.FSPAGE_NAME)).andReturn(pageNameDefinition).anyTimes();
        expect(flow.getFlowPropertyDefinition(FlowConstants.FAINVISIBLE)).andReturn(
            new FlowPropertyDefinitionBuilder(FlowConstants.FAINVISIBLE, boolean.class).initTranslator(new BooleanFlowTranslator())
                .toFlowPropertyDefinition()).anyTimes();
        expect(flow.getFlowPropertyDefinition(FlowConstants.FSAUTO_COMPLETE)).andReturn(
            new FlowPropertyDefinitionBuilder(FlowConstants.FSAUTO_COMPLETE, boolean.class).initTranslator(new BooleanFlowTranslator())
                .toFlowPropertyDefinition()).anyTimes();
        activity.setFlow(flow);
        expect(flowState.getPropertyWithDefinition(activity, pageNameDefinition)).andReturn(null);
        expect(flowState.getPropertyWithDefinition(activity, pageNameDefinition)).andReturn("foo");
        expect(flowState.getPropertyWithDefinition(activity, pageNameDefinition)).andReturn(null);
        expect(flowState.getPropertyWithDefinition(activity, pageNameDefinition)).andReturn("foo");
        EasyMock.replay(flow, flowState);
        assertTrue(activity.activate(FlowStepDirection.inPlace));
        assertFalse(activity.activate(FlowStepDirection.inPlace));
        assertTrue(activity.activate(FlowStepDirection.inPlace));
        assertFalse(activity.activate(FlowStepDirection.inPlace));
    }

    /**
     * @param flow
     * @return
     */
    private FlowManagement getFlowManagement(FlowImplementor... flow) {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowDefinitionsManager().addDefinitions(flow);
        return flowTestingUtils.getFlowManagement();
    }
}
