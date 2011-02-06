package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowActivityPhase;


/**
 * A simple version of FlowPropertyDefinitionProvider
 * @author patmoore
 *
 */
public class FlowPropertyDefinitionProviderImpl extends AbstractFlowPropertyDefinitionProvider implements
        FlowPropertyDefinitionProvider {

    public FlowPropertyDefinitionProviderImpl(FlowPropertyDefinitionImplementor... flowPropertyDefinitions) {
        super(flowPropertyDefinitions);
    }
    public FlowPropertyDefinitionProviderImpl(String name, Class<? extends Object> dataClass, FlowActivityPhase flowActivityPhase, Class<?>... collectionClasses) {
        this(new FlowPropertyDefinitionImpl(name, dataClass, flowActivityPhase, collectionClasses));
    }

    public FlowPropertyDefinitionProviderImpl(String name, Class<? extends Object> dataClass) {
        this(new FlowPropertyDefinitionImpl(name, dataClass));
    }

}
