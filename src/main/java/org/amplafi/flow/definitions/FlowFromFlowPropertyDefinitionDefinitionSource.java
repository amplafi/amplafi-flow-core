package org.amplafi.flow.definitions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowPropertyDefinition;

import static org.amplafi.flow.FlowConstants.FSSINGLE_PROPERTY_NAME;

import org.amplafi.flow.FlowConfigurationException;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider;
import org.amplafi.flow.flowproperty.PropertyUsage;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowImpl;
import org.apache.commons.lang.StringUtils;

import com.sworddance.beans.PropertyDefinition;
import static com.sworddance.util.CUtilities.*;

/**
 * This class allows you to turn EACH {@link PropertyDefinition} into its own {@link FlowImplementor}, avoiding the boilerplate of FlowImplementor
 * when you only have a single property to access and no state.
 *
 * TODO: looks like we should be able to refactor some of these methods that create the flow. Bothered by the lack of DRY.
 * @author patmoore
 *
 */
public class FlowFromFlowPropertyDefinitionDefinitionSource implements DefinitionSource<FlowImplementor> {

    private final Map<String, FlowImplementor> flows = new ConcurrentHashMap<String, FlowImplementor>();

    public FlowFromFlowPropertyDefinitionDefinitionSource() {

    }

    /**
     * {@link FlowPropertyDefinitionProvider} can supply multiple properties. {@link #add(FlowPropertyDefinitionProvider...)} creates flows from
     * {@link FlowPropertyDefinitionProvider#getOutputFlowPropertyDefinitionNames()} property list (if supplied) or the first property
     * defined.
     *
     * Using this add() method allows the other properties to also be accessed in their own flow.
     * @param flowPropertyName
     * @param flowPropertyDefinitionProvider
     * @param additionalConfigurationParameters
     */
    public void add(String flowPropertyName, FlowPropertyDefinitionProvider flowPropertyDefinitionProvider, List<FlowPropertyExpectation>additionalConfigurationParameters) {
        String capitalizedFlowPropertyName = StringUtils.capitalize(flowPropertyName);
        FlowImpl flow = new FlowImpl(capitalizedFlowPropertyName);
        flow.addPropertyDefinitions(new FlowPropertyDefinitionBuilder(FSSINGLE_PROPERTY_NAME).
            internalOnly().initDefaultObject(flowPropertyName));

        FlowActivityImpl flowActivity = new FlowActivityImpl("FA");
        // TODO: use to reduce impact of the expectations?
        // otherwise don't we risk exposing internal parameters.
        //FlowPropertyDefinitionBuilder.merge(new FlowPropertyExpectationImpl(flowPropertyName), additionalConfigurationParameters);

        flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity, additionalConfigurationParameters);
        flow.addActivity(flowActivity);
        put(this.flows, flow.getFlowPropertyProviderFullName(), flow);
    }
    /**
     * Add {@link FlowPropertyDefinition}s from the flowPropertyDefinitionProviders as a flow for each property.
     *
     * Only the properties that have {@link FlowPropertyDefinition#getPropertyUsage()}.{@link PropertyUsage#isOutputedProperty()} != false get their own flow.
     * This makes it safe to list properties that are expected on input without having a flow created for those input only properties.
     *
     * @param flowPropertyDefinitionProviders
     */
    public void add(FlowPropertyDefinitionProvider... flowPropertyDefinitionProviders) {
        for(FlowPropertyDefinitionProvider flowPropertyDefinitionProvider :flowPropertyDefinitionProviders) {
            List<String> outputFlowPropertyDefinitionNames = flowPropertyDefinitionProvider.getOutputFlowPropertyDefinitionNames();
            FlowConfigurationException.valid(isNotEmpty(outputFlowPropertyDefinitionNames), flowPropertyDefinitionProvider.getClass(), " has no output properties defined.");
            for(String flowPropertyName : outputFlowPropertyDefinitionNames) {
                // TODO : should we apply expectations: readonly?
                this.add(flowPropertyName, flowPropertyDefinitionProvider, null);
            }
        }
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinition(java.lang.String)
     */
    @Override
    public FlowImplementor getFlowDefinition(String flowTypeName) {
        return this.flows.get(flowTypeName);
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinitions()
     */
    @Override
    public Map<String, FlowImplementor> getFlowDefinitions() {
        return this.flows;
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#isFlowDefined(java.lang.String)
     */
    @Override
    public boolean isFlowDefined(String flowTypeName) {
        return this.flows.containsKey(flowTypeName);
    }
}
