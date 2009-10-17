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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.amplafi.flow.impl.FlowStateImpl;
import org.amplafi.flow.impl.FlowImpl;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowStateImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.amplafi.flow.flowproperty.PropertyScope.*;

/**
 * Tests around flows that don't require db.
 *
 * @author Patrick Moore
 */
public class TestFlows {
    private static final String SET_BY_MAP = "set-by-map";
    private static final String INITIAL_VALUE = "initial-property";
    private static final String PROPERTY1 = "property1";
    private static final String PROPERTY2 = "property2";
    private static final String FLOW_TYPE = "ftype1";
    private static final boolean TEST_ENABLE = true;

    /**
     * Test simple flow definitions and instances.
     *
     */
    @Test(enabled=TEST_ENABLE)
    public void testFlowDefinition() {
        FlowImpl flow = new FlowImpl();
        FlowActivity[] fas = new FlowActivity[3];
        for(int i = 0; i < fas.length; i++) {
            FlowActivityImpl fa = new FlowActivityImpl();
            fas[i] = fa;
            flow.addActivity(fa);
        }
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
        for(int i=0; i < fas.length; i++) {
            // make sure order is preserved.
            assertSame(flow.getActivity(i), fas[i]);
            // check that in definition all was as defined.
            assertFalse(fas[i].isActivatable());
            assertFalse(fas[i].isFinishingActivity());
        }

        Flow instance = flow.createInstance();
        List<FlowActivityImplementor> ifas = instance.getActivities();

        // make sure they are different and definition has not changed.
        for(int i=0; i < fas.length; i++) {
            // make sure order is preserved.
            assertNotSame(ifas.get(i), fas[i]);
            // check that in definition all was as defined.
            assertFalse(fas[i].isActivatable());
            assertFalse(fas[i].isFinishingActivity());
        }

        // check that first /last step of instance are set up correctly.
        assertTrue(ifas.get(0).isActivatable());
        assertFalse(ifas.get(0).isFinishingActivity());
        assertFalse(ifas.get(1).isActivatable());
        assertFalse(ifas.get(1).isFinishingActivity());
        assertFalse(ifas.get(2).isActivatable());
        assertFalse(ifas.get(2).isFinishingActivity());
    }

    /**
     * check to make sure properties specific to a FlowActivity are checked
     * and returned first.
     *
     */
    @Test
    public void testPropertyPriority() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        {
            FlowActivityImpl flowActivity0 = new FlowActivityImpl();
            flowActivity0.setActivityName("fs0");
            flowActivity0.addPropertyDefinitions(new FlowPropertyDefinitionImpl("key").initPropertyScope(activityLocal));
            FlowActivityImpl flowActivity1 = new FlowActivityImpl();
            flowActivity1.setActivityName("fs1");
            FlowImpl flow = new FlowImpl(FLOW_TYPE, flowActivity0, flowActivity1);
            flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
            flowTestingUtils.getFlowDefinitionsManager().addDefinition(FLOW_TYPE, flow);
        }
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(
            "key", "fs",
            FlowUtils.INSTANCE.toKey("fs0","key"), "fs0");
        FlowStateImplementor fs = flowManagement.startFlowState(FLOW_TYPE, true, initialFlowState, null);

        FlowActivityImplementor activity0 = fs.getActivity(0);
        assertEquals(activity0.getProperty("key"), "fs0", "flowState="+fs);
        FlowActivityImplementor activity1 = fs.getActivity(1);
        assertEquals(activity1.getProperty("key"), "fs");

        activity0.setProperty("key", "new-fs0");
        activity1.setProperty("key", "new-fs");

        assertEquals(activity0.getProperty("key"), "new-fs0", "flowState="+fs);
        assertEquals(activity1.getProperty("key"), "new-fs", "flowState="+fs);
    }

    /**
     * Test for hasVisibleNext and hasVisiblePrevious of FlowState.
     */
    @Test(enabled=TEST_ENABLE)
    public void testVisiblePreviousNext() {
        FlowImpl flow = new FlowImpl(FLOW_TYPE);
        flow.addActivity(new FlowActivityImpl());
        flow.addActivity(new FlowActivityImpl());
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
        flowTestingUtils.getFlowDefinitionsManager().addDefinition(FLOW_TYPE, flow);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();

        FlowState fs = new FlowStateImpl(FLOW_TYPE, flowManagement);
        fs.begin();

        assertTrue(fs.hasNext());
        assertTrue(fs.hasVisibleNext());
        assertFalse(fs.hasPrevious());
        assertFalse(fs.hasVisiblePrevious());

        fs.selectActivity(1, true);

        assertFalse(fs.hasNext());
        assertFalse(fs.hasVisibleNext());
        assertTrue(fs.hasPrevious());
        assertTrue(fs.hasVisiblePrevious());
    }

    /**
     * Test for hasVisibleNext and hasVisiblePrevious of FlowState when there are invisible FlowActivities.
     *
     * Also test when invisible is turned on/off during the flow.
     */
    @Test(enabled=TEST_ENABLE)
    public void testVisiblePreviousNextWithHidden() {
        Flow flow = new FlowImpl(FLOW_TYPE);
        FlowActivityImpl fa1 = new FlowActivityImpl();
        FlowActivityImpl fa2 = new FlowActivityImpl();
        FlowActivityImpl fa3 = new FlowActivityImpl();
        fa1.setInvisible(true);
        fa2.setInvisible(true);
        flow.addActivity(fa1);
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowTranslatorResolver().resolve(fa1);
        flow.addActivity(fa3);
        flowTestingUtils.getFlowTranslatorResolver().resolve(fa3);
        flow.addActivity(fa2);
        flowTestingUtils.getFlowTranslatorResolver().resolve(fa1);
        flowTestingUtils.getFlowDefinitionsManager().addDefinition(FLOW_TYPE, flow);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();

        FlowState fs = new FlowStateImpl(FLOW_TYPE, flowManagement);
        fs.begin();

        assertEquals(fs.getCurrentActivityIndex(), 1);
        assertTrue(fs.hasNext());
        assertFalse(fs.hasVisibleNext());
        assertTrue(fs.hasPrevious());
        assertFalse(fs.hasVisiblePrevious());
    }

    @Test(enabled=TEST_ENABLE)
    public void testInitialValuesOnFlow() {
        Flow flow = new FlowImpl(FLOW_TYPE);
        FlowPropertyDefinitionImpl globalDef = new FlowPropertyDefinitionImpl(PROPERTY1);
        globalDef.setInitial(INITIAL_VALUE);
        flow.addPropertyDefinitions(globalDef);
        FlowPropertyDefinitionImpl globalDef1 = new FlowPropertyDefinitionImpl(PROPERTY2);
        globalDef1.setInitial(INITIAL_VALUE);
        flow.addPropertyDefinitions(globalDef1);
        // activity #0
        FlowActivityImpl activity = new FlowActivityImpl();
        flow.addActivity(activity);
        // activity #1
        activity = new FlowActivityImpl();
        flow.addActivity(activity);
        // activity #2
        activity = new FlowActivityImpl();
        FlowPropertyDefinitionImpl localDef1 = new FlowPropertyDefinitionImpl(PROPERTY1);
        activity.addPropertyDefinitions(localDef1);
        flow.addActivity(activity);
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);

        flowTestingUtils.getFlowDefinitionsManager().addDefinition(FLOW_TYPE, flow);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        Map<String, String> initialFlowState = new HashMap<String, String>();
        initialFlowState.put(PROPERTY2, SET_BY_MAP);
        String returnToFlowLookupKey = null;
        FlowStateImplementor flowState = flowManagement.startFlowState(FLOW_TYPE, true, initialFlowState, returnToFlowLookupKey);
        assertEquals(flowState.getActivity(0).getProperty(PROPERTY1), INITIAL_VALUE, "flowState="+flowState);
        flowState.clearCache();
        assertEquals(flowState.getActivity(1).getProperty(PROPERTY2), SET_BY_MAP, "flowState="+flowState);
        flowState.clearCache();
        assertEquals(flowState.getActivity(2).getProperty(PROPERTY1), INITIAL_VALUE, "flowState="+flowState);
        flowState.clearCache();
    }

    @Test(enabled=TEST_ENABLE)
    public void testConversion() {
        String returnToFlowLookupKey = null;
        Map<String, String> initialFlowState = new HashMap<String, String>();
        Flow flow = new FlowImpl(FLOW_TYPE);
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Long.class);
        flow.addPropertyDefinitions(definition);
        FlowActivityImpl fa1 = new FlowActivityImpl();
        flow.addActivity(fa1);
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowTranslatorResolver().resolve(fa1);
        flowTestingUtils.getFlowDefinitionsManager().addDefinition(FLOW_TYPE, flow);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowState flowState = flowManagement.startFlowState(FLOW_TYPE, true, initialFlowState, returnToFlowLookupKey);
        flowState.setProperty("fee", true);
        Flow inst = flowState.getFlow();
        FlowPropertyDefinition flowPropertyDefinition = inst.getPropertyDefinition("fee");
        assertTrue(flowPropertyDefinition.getDataClass() == Boolean.class || flowPropertyDefinition.getDataClass() == boolean.class);

        // make sure that we can still see the original property definitions.
        flowPropertyDefinition = inst.getPropertyDefinition("foo");
        assertTrue(flowPropertyDefinition.getDataClass() == Long.class);
    }

    @Test(enabled=TEST_ENABLE)
    public void testEnumHandling() {
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState("foo", SampleEnum.EXTERNAL);
        Flow flow = new FlowImpl(FLOW_TYPE);
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", SampleEnum.class);
        flow.addPropertyDefinitions(definition);
        FlowActivityImpl fa1 = new FlowActivityImpl();
        definition = new FlowPropertyDefinitionImpl("fa1fp", SampleEnum.class).initInitial(SampleEnum.EMAIL.name());
        fa1.addPropertyDefinitions(definition);
        flow.addActivity(fa1);
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
        flowTestingUtils.getFlowDefinitionsManager().addDefinition(FLOW_TYPE, flow);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        String returnToFlowLookupKey = null;
        FlowState flowState = flowManagement.startFlowState(FLOW_TYPE, true, initialFlowState, returnToFlowLookupKey);
        SampleEnum type =flowState.getCurrentActivity().getProperty("foo");
        assertEquals(type, SampleEnum.EXTERNAL, "(looking for property 'foo') FlowState="+flowState);
        type =flowState.getProperty("fa1fp", SampleEnum.class);
        assertEquals(type, SampleEnum.EMAIL);
    }

    private static enum SampleEnum {
        EXTERNAL, EMAIL

    }
}
