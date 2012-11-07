package org.amplafi.flow;

import java.util.Arrays;
import org.amplafi.flow.flowproperty.AbstractFlowPropertyDefinitionProvider;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.FlowPropertyExpectationImpl;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.flowproperty.FlowPropertyValuePersister;
import org.amplafi.flow.flowproperty.PropertyUsage;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class TestFlowPropertyValuePersister {
    private static final boolean TEST_ENABLED = true;
    private static final String FLOW_TYPE = "TestFlowPropertyValuePersister";

    /**
     * Make sure properties only get a persister if their {@link PropertyUsage} allows the values to be saved.
     */
    @Test(enabled=TEST_ENABLED)
    public void testToMakeSureOnlyChangedPropertiesGetPersisters() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.addFlowDefinition(FLOW_TYPE, new AFlowActivity().initInvisible(false));
        FlowState flowState = flowTestingUtils.getFlowManagement().startFlowState(FLOW_TYPE, true, null);
        FlowPropertyDefinition flowPropertyDefinition = flowState.getPropertyDefinitions().get("property");
        assertNull(flowPropertyDefinition.getFlowPropertyValuePersister());

        flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.addFlowDefinition(FLOW_TYPE, new BFlowActivity().initInvisible(false));
        flowState = flowTestingUtils.getFlowManagement().startFlowState(FLOW_TYPE, true, null);
        flowPropertyDefinition = flowState.getPropertyDefinitions().get("property");
        assertNotNull(flowPropertyDefinition.getFlowPropertyValuePersister());
    }

    public static class AFlowActivity extends FlowActivityImpl {
        @Override
        protected void addStandardFlowPropertyDefinitions() {
            super.addStandardFlowPropertyDefinitions();
            PropertyValuePersister.INSTANCE.defineFlowPropertyDefinitions(this);
        }
    }
    public static class BFlowActivity extends FlowActivityImpl {
        @Override
        protected void addStandardFlowPropertyDefinitions() {
            super.addStandardFlowPropertyDefinitions();
            PropertyValuePersister.INSTANCE.defineFlowPropertyDefinitions(this, Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl("property", PropertyUsage.io)));
        }
    }
    private static class PropertyValuePersister extends AbstractFlowPropertyDefinitionProvider implements FlowPropertyValuePersister<FlowPropertyProvider> {
        public static final PropertyValuePersister INSTANCE = new PropertyValuePersister();

        PropertyValuePersister() {
            super.addFlowPropertyDefinitionImplementators(new FlowPropertyDefinitionImpl("property", Boolean.class).initAutoCreate().initFlowPropertyValuePersister(this));
        }
        @Override
        public void saveChanges(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
            throw new IllegalStateException("Should never be called");
        }

        @Override
        public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
            return true;
        }

    }
}
