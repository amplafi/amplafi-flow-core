package org.amplafi.flow.definitions;

import static org.testng.Assert.*;

import java.util.Map;

import org.amplafi.flow.FlowConstants;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.flowproperty.BaseFlowPropertyDefinitionBuilderProvider;
import org.amplafi.flow.flowproperty.FixedFlowPropertyValueProvider;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilderProvider;
import org.testng.annotations.Test;

/**
 * Test FlowFromMultipleFlowPropertyDefinitionsDefinitionSource
 * @author patmoore
 *
 */
public class TestFlowFromFlowPropertyDefinitionDefinitionSource {

    /**
     * Make sure only a flow is created for each property
     */
    @Test
    public void testSingleFlowCreate() {
        FlowFromFlowPropertyDefinitionDefinitionSource definitionSource = new FlowFromFlowPropertyDefinitionDefinitionSource();
        definitionSource.add("FPDP3_1", new FPDP3(), FlowPropertyDefinitionBuilder.CONSUMING);
        Map<String, FlowImplementor> definitions = definitionSource.getFlowDefinitions();
        assertEquals(definitions.size(), 1);
        checkDefinitionsMap(definitions);
    }
    @Test
    public void testMultipleFlowCreate() {
        FlowFromFlowPropertyDefinitionDefinitionSource definitionSource = new FlowFromFlowPropertyDefinitionDefinitionSource();
        definitionSource.add(new FPDP1(), new FPDP2(), new FPDP3());
        Map<String, FlowImplementor> definitions = definitionSource.getFlowDefinitions();
        assertEquals(definitions.size(), 3);
        checkDefinitionsMap(definitions);
    }
    private void checkDefinitionsMap(Map<String, FlowImplementor> definitions) {
        for(Map.Entry<String, FlowImplementor> entry: definitions.entrySet()) {
            FlowImplementor flow = entry.getValue();
            assertNotNull(flow);
            assertEquals(flow.getFlowPropertyProviderName(), entry.getKey());
            Map<String, FlowPropertyDefinition> propertyDefinitions = flow.getPropertyDefinitions();
    //        assertEquals(propertyDefinitions.size(), 6); // need to get rid of standard flow properties for this to work
            FlowPropertyDefinition singleProperty = propertyDefinitions.get(FlowConstants.FSSINGLE_PROPERTY_NAME);
            assertNotNull(singleProperty);
            FixedFlowPropertyValueProvider flowPropertyValueProvider = (FixedFlowPropertyValueProvider) singleProperty.getFlowPropertyValueProvider();
            String returnedPropertyName = (String) flowPropertyValueProvider.getDefaultObject();
            assertNotNull(propertyDefinitions.get(returnedPropertyName));
            assertEquals(returnedPropertyName, flow.getFlowPropertyProviderFullName());
        }
    }
    private class FPDP1 extends BaseFlowPropertyDefinitionBuilderProvider implements FlowPropertyDefinitionBuilderProvider {
        FPDP1() {
            super(new FlowPropertyDefinitionBuilder("FPDP1_1").returned());
        }
    }
    private class FPDP2 extends BaseFlowPropertyDefinitionBuilderProvider implements FlowPropertyDefinitionBuilderProvider {
        FPDP2() {
            super(new FlowPropertyDefinitionBuilder("FPDP2_1").returned(), new FlowPropertyDefinitionBuilder("FPDP2_2"));
        }
    }
    private class FPDP3 extends BaseFlowPropertyDefinitionBuilderProvider implements FlowPropertyDefinitionBuilderProvider {
        FPDP3() {
            super(new FlowPropertyDefinitionBuilder("FPDP3_1").returned(), new FlowPropertyDefinitionBuilder("FPDP3_2"), new FlowPropertyDefinitionBuilder("FPDP3_3"));
        }
    }
}
