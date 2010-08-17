package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowAppearance;
import org.amplafi.flow.FlowValueMapKey;
import org.amplafi.flow.FlowValuesMap;

import static org.amplafi.flow.FlowAppearance.*;
import static org.amplafi.flow.flowproperty.PropertyScope.flowLocal;
import static org.amplafi.flow.flowproperty.PropertyUsage.creates;

/**
 *  Defines property the {@link FlowAppearance}.
 *
 * @author Konstantin Burov (aectann@gmail.com)
 */
public class FlowAppearanceFlowPropertyDefinitionProvider implements FlowPropertyDefinitionProvider {
    public static final String FLOW_APPEARANCE = "flowAppearance";
    public static final FlowAppearanceFlowPropertyDefinitionProvider INSTANCE = new FlowAppearanceFlowPropertyDefinitionProvider();

    @Override
    public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider,
        FlowValuesMap<? extends FlowValueMapKey, ? extends CharSequence> additionalConfigurationParameters) {
        flowPropertyProvider.addPropertyDefinitions(new FlowPropertyDefinitionImpl(FLOW_APPEARANCE, FlowAppearance.class)
            .initDefaultObject(normal).initAccess(flowLocal, creates));
    }
}
