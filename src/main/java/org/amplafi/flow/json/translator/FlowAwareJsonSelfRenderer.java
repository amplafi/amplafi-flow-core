package org.amplafi.flow.json.translator;

import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;
import org.amplafi.flow.json.JsonSelfRenderer;

public interface FlowAwareJsonSelfRenderer extends JsonSelfRenderer {

	void setValuesProvider(FlowPropertyProviderWithValues valuesProvider);
	
}
