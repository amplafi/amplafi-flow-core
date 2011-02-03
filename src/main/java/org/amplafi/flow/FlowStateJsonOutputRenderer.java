package org.amplafi.flow;

import java.util.Collection;

import org.amplafi.json.IJsonWriter;

/**
 * Unlike {@link FlowStateJsonRenderer} doesn't renders flow state as strings map. Uses specific
 * renderers provided by writer.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 */
public class FlowStateJsonOutputRenderer extends FlowStateJsonRenderer {

    public final FlowStateJsonOutputRenderer INSTANCE = new FlowStateJsonOutputRenderer();

    @Override
    protected void renderState(IJsonWriter jsonWriter, FlowState flowState) {
        jsonWriter.key(FS_PARAMETERS);
        jsonWriter.object();
        Collection<FlowPropertyDefinition> propertyDefinitions = flowState.getPropertyDefinitions().values();
        for (FlowPropertyDefinition flowPropertyDefinition : propertyDefinitions) {
            String propertyName = flowPropertyDefinition.getName();
            if (flowState.isPropertyValueSet(propertyName)) {
                Object property = flowState.getProperty(propertyName);
                jsonWriter.keyValueIfNotNullValue(propertyName, property);
            }
        }
        jsonWriter.endObject();
    }

}
