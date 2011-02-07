package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowAppearance;

import static org.amplafi.flow.FlowAppearance.*;
import static org.amplafi.flow.flowproperty.PropertyScope.*;
import static org.amplafi.flow.flowproperty.PropertyUsage.*;

/**
 *  Defines property the {@link FlowAppearance}.
 *
 * @author Konstantin Burov (aectann@gmail.com)
 */
public class FlowAppearanceFlowPropertyDefinitionProvider extends AbstractFlowPropertyDefinitionProvider implements FlowPropertyDefinitionProvider {
    public static final String FLOW_APPEARANCE = "flowAppearance";
    public static final FlowAppearanceFlowPropertyDefinitionProvider INSTANCE = new FlowAppearanceFlowPropertyDefinitionProvider();

    public FlowAppearanceFlowPropertyDefinitionProvider() {
        super(new FlowPropertyDefinitionImpl(FLOW_APPEARANCE, FlowAppearance.class).initDefaultObject(normal).initAccess(flowLocal, suppliesIfMissing));
    }
}
