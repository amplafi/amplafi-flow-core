package org.amplafi.flow.translator;

import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;
import org.amplafi.json.JsonSelfRenderer;

public interface FlowAwareJsonSelfRenderer extends JsonSelfRenderer {

	void setValuesProvider(FlowPropertyProviderWithValues valuesProvider);
	
}
