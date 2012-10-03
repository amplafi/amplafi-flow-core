/**
 * Copyright 2006-2011 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import java.util.HashMap;

import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowTestingUtils;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
/**
 * @author patmoore
 *
 */
public class TestOneOfManyFlowPropertyValueProvider {

    /**
     * Test when the property "prop" can come from 1 of 2 different underlying properties ("prop1", "prop2" )
     */
    @Test
    public void testAlternative1() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        FlowActivityImpl activity = new FlowActivityImpl("FA");
        OneOfManyFlowPropertyValueProvider oneOfManyFlowPropertyValueProvider = new OneOfManyFlowPropertyValueProvider("prop", String.class, "prop1", "prop2");
        oneOfManyFlowPropertyValueProvider.defineFlowPropertyDefinitions(activity);
        flowTestingUtils.addFlowDefinition("foo", activity);
        FlowState flowState= flowTestingUtils.getFlowManagement().startFlowState("foo", false, null, null);
        assertNull(flowState.getProperty("prop"), "should not be set");

        // prop1 ( the first choice ) is null
        HashMap<String, String> initialFlowState = new HashMap<>();
        initialFlowState.put("prop2", "value2");
        flowState= flowTestingUtils.getFlowManagement().startFlowState("foo", false, initialFlowState, null);
        assertEquals(flowState.getProperty("prop"), "value2", "should be set");

        // prop1 overrides prop2
        initialFlowState.put("prop1", "value1");
        flowState= flowTestingUtils.getFlowManagement().startFlowState("foo", false, initialFlowState, null);
        assertEquals(flowState.getProperty("prop"), "value1", "should be set");
    }
}
