package org.amplafi.flow.flowproperty;

import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Test {@link InvalidatingFlowPropertyValueChangeListener}
 * @author patmoore
 *
 */
public class TestInvalidatingFlowPropertyValueChangeListener {

    @Test
    public void testSettingDependentOnProperties() {
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilderDependent = new FlowPropertyDefinitionBuilder("dep1", Boolean.class);
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = new FlowPropertyDefinitionBuilder("test", Boolean.class);
        flowPropertyDefinitionBuilder.addPropertiesDependentOn(flowPropertyDefinitionBuilderDependent.toFlowPropertyDefinition());

        FlowPropertyDefinitionImplementor flowPropertyDefinition = flowPropertyDefinitionBuilder.toFlowPropertyDefinition();
        assertFalse(flowPropertyDefinition.getPropertiesDependentOn().isEmpty());

    }
    /**
     * Test to make sure that dependent properties get cleared.
     *
     */
    @Test
    public void testClearingDependentProperties() {
        InvalidatingFlowPropertyValueChangeListener invalidatingFlowPropertyValueChangeListener = new InvalidatingFlowPropertyValueChangeListener();
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilderDependent = new FlowPropertyDefinitionBuilder("dep1", Boolean.class);
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = new FlowPropertyDefinitionBuilder("test", Boolean.class);
        final FlowPropertyDefinitionImplementor flowPropertyDefinitionDependent = flowPropertyDefinitionBuilderDependent.toFlowPropertyDefinition();
        flowPropertyDefinitionBuilder.addPropertiesDependentOn(flowPropertyDefinitionDependent);

        FlowPropertyDefinitionImplementor flowPropertyDefinition = flowPropertyDefinitionBuilder.toFlowPropertyDefinition();
        IMockBuilder<FlowPropertyProviderWithValues> mockBuilder = EasyMock.createMockBuilder(FlowPropertyProviderWithValues.class);
        FlowPropertyProviderWithValues flowPropertyProviderWithValues = EasyMock.createMock(FlowPropertyProviderWithValues.class);
        flowPropertyProviderWithValues.setProperty(EasyMock.eq("test"), EasyMock.isNull());
        EasyMock.replay(flowPropertyProviderWithValues);
        invalidatingFlowPropertyValueChangeListener.monitorDependencies(flowPropertyDefinition);
        invalidatingFlowPropertyValueChangeListener.propertyChange(flowPropertyProviderWithValues, null, flowPropertyDefinitionDependent, "false", "true");
        EasyMock.verify(flowPropertyProviderWithValues);
    }
}
