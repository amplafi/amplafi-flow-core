package org.amplafi.flow;

import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;
import org.amplafi.json.JsonSelfRenderer;

public interface FlowAwareJsonSelfRenderer extends JsonSelfRenderer {

	void setValuesProvider(FlowPropertyProviderWithValues valuesProvider);
	
}
