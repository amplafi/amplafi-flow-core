package org.amplafi.flow.definitions;

import static com.sworddance.util.CUtilities.put;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowImpl;

import com.sworddance.beans.PropertyDefinition;

/**
 * This class allows you to turn multiple {@link PropertyDefinition} into its a single {@link FlowImplementor}, avoiding the boilerplate of FlowImplementor
 * when you only have a single property to access and no state.
 *
 * TODO: looks like we should be able to refactor some of these methods that create the flow. Bothered by the lack of DRY.
 * @author patmoore
 *
 */
public class FlowFromMultipleFlowPropertyDefinitionsDefinitionSource implements DefinitionSource<FlowImplementor> {

    private final Map<String, FlowImplementor> flows = new ConcurrentHashMap<String, FlowImplementor>();

    public FlowFromMultipleFlowPropertyDefinitionsDefinitionSource() {

    }

    /**
     * {@link FlowPropertyDefinitionProvider} can supply multiple properties. {@link #add(FlowPropertyDefinitionProvider...)} creates flows from
     * {@link FlowPropertyDefinitionProvider#getOutputFlowPropertyDefinitionNames()} property list (if supplied) or the first property
     * defined.
     *
     * Using this add() method allows the other properties to also be accessed in their own flow.
     * @param flowName ( should be cased as desired. First letter capitalization is not enforced )
     * @param flowPropertyDefinitionProvider
     * @param additionalConfigurationParameters
     */
    @SafeVarargs
    public final void add(String flowName, List<? extends FlowPropertyDefinitionProvider> flowPropertyDefinitionProviders, List<FlowPropertyExpectation>...additionalConfigurationParameters) {
        FlowImpl flow = new FlowImpl(flowName);

        FlowActivityImpl flowActivity = new FlowActivityImpl("FA");
        List<FlowPropertyExpectation> combinedAdditionalConfigurationParameters = FlowPropertyDefinitionBuilder.combine(additionalConfigurationParameters);
        for(FlowPropertyDefinitionProvider flowPropertyDefinitionProvider: flowPropertyDefinitionProviders) {
            flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity, combinedAdditionalConfigurationParameters);
        }
        flow.addActivity(flowActivity);
        put(this.flows, flow.getFlowPropertyProviderFullName(), flow);
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
