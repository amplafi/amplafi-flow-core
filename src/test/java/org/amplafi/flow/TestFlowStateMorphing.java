/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

import java.util.HashMap;

import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class TestFlowStateMorphing {

    private static final String FIRST_FLOW = "FirstFlow";

    private static final String MORPHED_FLOW = "MorphedFlow";

    private static final String FS_MORPH_FLOW = "fsMorphFlow";

    /**
     * In this case flowstate morphing is happening becasue the value dynamic FPD is favorable for morphing
     */
    @Test
    public void testPositiveStateFlowMorphingPositive() {
        FlowActivityImpl fa1 = createFA("FA-1");
        FlowPropertyDefinition morphFlowFPD = new FlowPropertyDefinition(FS_MORPH_FLOW, Boolean.class).initAutoCreate();
        fa1.addPropertyDefinition(morphFlowFPD);

        FlowActivityImpl fa2 = createFA("FA-2");
        FlowActivityImpl fa3 = createFA("FA-3");
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();

        flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(FIRST_FLOW, fa1,fa2,fa3));

        FlowActivityImpl fa4 = createFA("FA-4");
        FlowActivityImpl fa5 = createFA("FA-5");
        flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(MORPHED_FLOW, fa2,fa4,fa5));
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();

        FlowState flowState = flowManagement.startFlowState(FIRST_FLOW, true, null, false);
        assertEquals(flowState.getFlow().getFlowTypeName(), FIRST_FLOW);
        assertEquals(flowState.getCurrentActivityByName(), "FA-1");
        flowState.next();
        assertEquals(flowState.getCurrentActivityByName(), "FA-2");

        flowState.morphFlow(MORPHED_FLOW, null);
        assertEquals(flowState.getFlow().getFlowTypeName(), MORPHED_FLOW);
        assertEquals(flowState.getCurrentActivityByName(), "FA-2");
        flowState.next();
        assertEquals(flowState.getCurrentActivityByName(), "FA-4");

        flowState.morphFlow(FIRST_FLOW, null);
        assertEquals(flowState.getCurrentActivityByName(), "FA-3");
    }

    /**
     * In this case there is no morphing happening
     */
    @Test
    public void testNegativeStateFlowMorphing() {
        FlowActivityImpl fa1 = createFA("FA-1");
        FlowPropertyDefinition morphFlowFPD = new FlowPropertyDefinition(FS_MORPH_FLOW, Boolean.class).initAutoCreate();
        fa1.addPropertyDefinition(morphFlowFPD);

        FlowActivityImpl fa2 = createFA("FA-2");
        FlowActivityImpl fa3 = createFA("FA-3");
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(FIRST_FLOW, fa1,fa2,fa3));
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();

        FlowState flowState = flowManagement.startFlowState(FIRST_FLOW, true, null, false);
        assertEquals(flowState.getFlow().getFlowTypeName(), FIRST_FLOW);
        assertEquals(flowState.getCurrentActivity().getActivityName(), "FA-1");
        flowState.next();
        assertEquals(flowState.getCurrentActivity().getActivityName(), "FA-2");
        flowState.setProperty(fa1,morphFlowFPD,false);
        flowState.next();
        assertEquals(flowState.getCurrentActivity().getActivityName(), "FA-3");
        flowState.finishFlow();
        assertNull(flowManagement.getCurrentFlowState(), "there shouldn't be a flow running");
    }

    /**
     * Test no common FAs
     */
    @Test
    public void testNoCommonFAs() {
        FlowActivityImpl fa1 = createFA("FA-1");
        FlowPropertyDefinition morphFlowFPD = new FlowPropertyDefinition(FS_MORPH_FLOW, Boolean.class).initAutoCreate();
        fa1.addPropertyDefinition(morphFlowFPD);

        FlowActivityImpl fa2 = createFA("FA-2");
        FlowActivityImpl fa3 = createFA("FA-3");

        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(FIRST_FLOW, fa1,fa2,fa3));

        FlowActivityImpl fa4 = createFA("FA-4");
        FlowActivityImpl fa5 = createFA("FA-5");
        flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(MORPHED_FLOW, fa4, fa5));

        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowState flowState = flowManagement.startFlowState(FIRST_FLOW, true, null, false);
        assertEquals(flowState.getFlow().getFlowTypeName(), FIRST_FLOW);
        assertEquals(flowState.getCurrentActivity().getActivityName(), "FA-1");
        flowState.next();
        assertEquals(flowState.getCurrentActivity().getActivityName(), "FA-2");

        flowState.morphFlow(MORPHED_FLOW, new HashMap<String,String>());
        assertEquals(flowState.getFlow().getFlowTypeName(), MORPHED_FLOW);
        assertEquals(flowState.getCurrentActivity().getActivityName(), "FA-4");
    }

    /**
     * Test no FAs not in order
     */
    @Test(expectedExceptions = IllegalStateException.class)
    public void testFAsNotInOrder() {
        FlowActivityImpl fa1 = createFA("FA-1");
        FlowPropertyDefinition morphFlowFPD = new FlowPropertyDefinition(FS_MORPH_FLOW, Boolean.class).initAutoCreate();
        fa1.addPropertyDefinition(morphFlowFPD);

        FlowActivityImpl fa2 = createFA("FA-2");
        FlowActivityImpl fa3 = createFA("FA-3");

        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(FIRST_FLOW, fa1,fa2,fa3));

        flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(MORPHED_FLOW, fa3, fa2, fa1));

        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();

        FlowState flowState = flowManagement.startFlowState(FIRST_FLOW, true, null, false);
        assertEquals(flowState.getFlow().getFlowTypeName(), FIRST_FLOW);
        assertEquals(flowState.getCurrentActivity().getActivityName(), "FA-1");
        flowState.next();
        assertEquals(flowState.getCurrentActivity().getActivityName(), "FA-2");
        flowState.morphFlow(MORPHED_FLOW, new HashMap<String,String>());
        fail();
    }

    private FlowActivityImpl createFA(String name) {
        FlowActivityImpl activity = new FlowActivityImpl();
        activity.setActivityName(name);
        return activity;
    }
}