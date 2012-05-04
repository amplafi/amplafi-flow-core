package org.amplafi.flow.flowproperty;

import static org.amplafi.flow.FlowAppearance.normal;
import static org.amplafi.flow.flowproperty.PropertyScope.flowLocal;
import static org.amplafi.flow.flowproperty.PropertyUsage.suppliesIfMissing;

import org.amplafi.flow.FlowAppearance;

/**
 * Defines property the {@link FlowAppearance}.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 */
public class FlowAppearanceFlowPropertyDefinitionProvider extends
		AbstractFlowPropertyDefinitionProvider implements
		FlowPropertyDefinitionProvider {
	public static final String FLOW_APPEARANCE = "flowAppearance";
	public static final FlowAppearanceFlowPropertyDefinitionProvider INSTANCE = new FlowAppearanceFlowPropertyDefinitionProvider();

	public FlowAppearanceFlowPropertyDefinitionProvider() {
		super(new FlowPropertyDefinitionBuilder()
				.createFlowPropertyDefinition(FLOW_APPEARANCE, FlowAppearance.class)
				.initDefaultObject(normal)
				.initAccess(flowLocal, suppliesIfMissing));
	}

}
