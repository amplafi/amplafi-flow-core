package org.amplafi.flow.definitions;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Map;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.flowproperty.BaseFlowPropertyDefinitionBuilderProvider;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilderProvider;
import org.testng.annotations.Test;

/**
 * Test FlowFromMultipleFlowPropertyDefinitionsDefinitionSource
 * @author patmoore
 *
 */
public class TestFlowFromMultipleFlowPropertyDefinitionsDefinitionSource {

    /**
     * Make sure only a single flow is created and that it has all the expected properties.
     */
    @Test
    public void testSingleFlowCreate() {
        FlowFromMultipleFlowPropertyDefinitionsDefinitionSource definitionSource = new FlowFromMultipleFlowPropertyDefinitionsDefinitionSource();
        definitionSource.add("test", Arrays.asList(new FPDP1(), new FPDP2(), new FPDP3()), FlowPropertyDefinitionBuilder.CONSUMING);
        Map<String, FlowImplementor> definitions = definitionSource.getFlowDefinitions();
        assertEquals(definitions.size(), 1);
        Flow flow = definitions.get("test");
        assertNotNull(flow);
        Map<String, FlowPropertyDefinition> propertyDefinitions = flow.getPropertyDefinitions();
//        assertEquals(propertyDefinitions.size(), 6); // need to get rid of standard flow properties for this to work
        assertNotNull(propertyDefinitions.get("FPDP1_1"));
        assertNotNull(propertyDefinitions.get("FPDP2_1"));
        assertNotNull(propertyDefinitions.get("FPDP2_2"));
        assertNotNull(propertyDefinitions.get("FPDP3_1"));
        assertNotNull(propertyDefinitions.get("FPDP3_2"));
        assertNotNull(propertyDefinitions.get("FPDP3_3"));
    }

    private class FPDP1 extends BaseFlowPropertyDefinitionBuilderProvider implements FlowPropertyDefinitionBuilderProvider {
        FPDP1() {
            super(new FlowPropertyDefinitionBuilder("FPDP1_1"));
        }
    }
    private class FPDP2 extends BaseFlowPropertyDefinitionBuilderProvider implements FlowPropertyDefinitionBuilderProvider {
        FPDP2() {
            super(new FlowPropertyDefinitionBuilder("FPDP2_1"), new FlowPropertyDefinitionBuilder("FPDP2_2"));
        }
    }
    private class FPDP3 extends BaseFlowPropertyDefinitionBuilderProvider implements FlowPropertyDefinitionBuilderProvider {
        FPDP3() {
            super(new FlowPropertyDefinitionBuilder("FPDP3_1"), new FlowPropertyDefinitionBuilder("FPDP3_2"), new FlowPropertyDefinitionBuilder("FPDP3_3"));
        }
    }
}
