package org.amplafi.flow;

import java.util.Map;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowConstants;
import org.amplafi.flow.impl.FlowDefinitionsManagerImpl;
import org.amplafi.flow.impl.*;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.TransitionType;
import org.amplafi.flow.translator.BaseFlowTranslatorResolver;
import org.easymock.classextension.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;


import static org.easymock.classextension.EasyMock.*;
/**
 *
 */
public class TestTransitionFlowActivity extends Assert {
    @Test
    public void testDup() {
        TransitionFlowActivity obj = new TransitionFlowActivity();
        assertTrue(obj.getClass().isInstance(obj.dup()));
    }

    /**
     * Test to make sure that a {@link TransitionFlowActivity} returns true in {@link TransitionFlowActivity#activate()} if there
     * is no page or component name.
     * @throws Exception
     */
    @Test
    public void testTransitionActivate() throws Exception {
        TransitionFlowActivity obj = new TransitionFlowActivity();
        Flow flow = EasyMock.createMock(Flow.class);
        FlowState flowState = EasyMock.createNiceMock(FlowState.class);
        expect(flow.getFlowState()).andReturn(flowState).anyTimes();
        expect(flow.getPropertyDefinition(FlowConstants.FSAUTO_COMPLETE)).andReturn(null).anyTimes();
        obj.setFlow(flow);
        EasyMock.replay(flow, flowState);
        assertTrue(obj.activate());
        obj.setPageName("foo");
        assertFalse(obj.activate());
        obj.setPageName(null);
        assertTrue(obj.activate());
        obj.setComponentName("foo");
        assertFalse(obj.activate());
    }

    @Test
    public void testTransitionFinishFlow() throws Exception {
        String returnToFlowLookupKey = null;
        FlowDefinitionsManagerImpl flowDefinitionsManager = new FlowDefinitionsManagerImpl();
        flowDefinitionsManager.initializeService();
        BaseFlowTranslatorResolver flowTranslatorResolver = new BaseFlowTranslatorResolver();
        flowTranslatorResolver.addStandardFlowTranslators();
        flowTranslatorResolver.initializeService();
        flowDefinitionsManager.setFlowTranslatorResolver(flowTranslatorResolver);
        TransitionFlowActivity obj = new TransitionFlowActivity();
        String flowTypeName = "foo";
        flowDefinitionsManager.addDefinitions(new FlowImpl(flowTypeName, obj));
        BaseFlowManagement baseFlowManagement = new BaseFlowManagement();
        baseFlowManagement.setFlowDefinitionsManager(flowDefinitionsManager);
        baseFlowManagement.setFlowTranslatorResolver(flowTranslatorResolver);
        Map<String, String> initialFlowState = null;
        baseFlowManagement.startFlowState(flowTypeName, false, initialFlowState, returnToFlowLookupKey);
    }

    @Test
    public void testTransitionFlowActivityWithFlowTransitions() throws Exception {
        String returnToFlowLookupKey = null;
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String nextFlowType = flowTestingUtils.addDefinition(new FlowActivityImpl());
        TransitionFlowActivity transitionFlowActivity = new TransitionFlowActivity();
        transitionFlowActivity.setTransitionType(TransitionType.alternate);
        transitionFlowActivity.setNextFlowType(nextFlowType);
        String flowTypeName = flowTestingUtils.addDefinition(new FlowActivityImpl(), transitionFlowActivity);
        FlowManagement flowManagement = flowTestingUtils.getFlowDefinitionsManager().getSessionFlowManagement();
        FlowState flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
        flowTestingUtils.advanceToEnd(flowState);
        FlowState nextFlowState = flowManagement.getCurrentFlowState();
        // the alternate condition was not met.
        assertNull(nextFlowState);

        flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
        flowState.setFinishType(TransitionType.alternate.toString());
        flowTestingUtils.advanceToEnd(flowState);
        nextFlowState = flowManagement.getCurrentFlowState();
        assertNotNull(nextFlowState);
        assertEquals(nextFlowState.getFlowTypeName(), nextFlowType);

    }
    /**
     * test a {@link TransitionFlowActivity} that transitions on a normal finish
     * @throws Exception
     */
    @Test
    public void testTransitionFlowActivityWithNormalFinish() throws Exception {
        String returnToFlowLookupKey = null;
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String nextFlowType = flowTestingUtils.addDefinition(new FlowActivityImpl());
        TransitionFlowActivity transitionFlowActivity = new TransitionFlowActivity();
        transitionFlowActivity.setNextFlowType(nextFlowType);
        String flowTypeName = flowTestingUtils.addDefinition(new FlowActivityImpl(), transitionFlowActivity);
        FlowManagement flowManagement = flowTestingUtils.getFlowDefinitionsManager().getSessionFlowManagement();
        FlowState flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
        flowTestingUtils.advanceToEnd(flowState);
        FlowState nextFlowState = flowManagement.getCurrentFlowState();
        assertNotNull(nextFlowState);
        assertEquals(nextFlowState.getFlowTypeName(), nextFlowType);

    }
    /**
     * 2 alternate finishes + 1 normal finish
     * @throws Exception
     */
    @Test
    public void testTransitionFlowActivityWithMultipleFlowTransitions() throws Exception {
        String returnToFlowLookupKey = null;
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String nextFlowType0 = flowTestingUtils.addDefinition(new FlowActivityImpl());
        TransitionFlowActivity transitionFlowActivity0 = new TransitionFlowActivity();
        transitionFlowActivity0.setTransitionType(TransitionType.alternate);
        transitionFlowActivity0.setNextFlowType(nextFlowType0);

        String nextFlowType1 = flowTestingUtils.addDefinition(new FlowActivityImpl());
        TransitionFlowActivity transitionFlowActivity1 = new TransitionFlowActivity();
        transitionFlowActivity1.setTransitionType(TransitionType.alternate);
        transitionFlowActivity1.setFinishKey("foo1");
        transitionFlowActivity1.setNextFlowType(nextFlowType1);

        String nextFlowType2 = flowTestingUtils.addDefinition(new FlowActivityImpl());
        TransitionFlowActivity transitionFlowActivity2 = new TransitionFlowActivity();
        transitionFlowActivity2.setNextFlowType(nextFlowType2);

        String flowTypeName = flowTestingUtils.addDefinition(new FlowActivityImpl(), transitionFlowActivity0, transitionFlowActivity1, transitionFlowActivity2);
        FlowManagement flowManagement = flowTestingUtils.getFlowDefinitionsManager().getSessionFlowManagement();
        FlowState flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
        flowTestingUtils.advanceToEnd(flowState);
        FlowState nextFlowState = flowManagement.getCurrentFlowState();
        // the alternate condition was not met.
        assertNotNull(nextFlowState);
        assertEquals(nextFlowState.getFlowTypeName(), nextFlowType2);

        flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
        flowState.setFinishType(TransitionType.alternate.toString());
        flowTestingUtils.advanceToEnd(flowState);
        nextFlowState = flowManagement.getCurrentFlowState();
        assertNotNull(nextFlowState);
        assertEquals(nextFlowState.getFlowTypeName(), nextFlowType0);

        flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
        flowState.setFinishType("foo1");
        flowTestingUtils.advanceToEnd(flowState);
        nextFlowState = flowManagement.getCurrentFlowState();
        assertNotNull(nextFlowState);
        assertEquals(nextFlowState.getFlowTypeName(), nextFlowType1);
    }
}
