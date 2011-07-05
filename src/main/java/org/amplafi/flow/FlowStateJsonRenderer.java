/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package org.amplafi.flow;

import java.util.Map;

import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JSONObject;
import org.amplafi.json.JsonRenderer;



/**
 * used to render a flow state as part of the api / flow service functionality.
 * @author Patrick Moore
 */
public class FlowStateJsonRenderer implements JsonRenderer<FlowState> {

    public static final String FS_PARAMETERS = "fsParameters";
    public static final String FS_LOOKUP_KEY = "fsLookupKey";
    public static final String FS_CURRENT_ACTIVITY_BY_NAME = "fsCurrentActivityByName";
    public static final String FS_COMPLETE = "fsComplete";

    private FlowManagement flowManagement;

    @Override
    public Class<FlowState> getClassToRender() {
        return FlowState.class;
    }

    @Override
    public IJsonWriter toJson(IJsonWriter jsonWriter, FlowState flowState) {
        jsonWriter.object();
        jsonWriter.key(FS_COMPLETE).value(flowState.isCompleted());
        if (flowState.isActive()) {
            jsonWriter.key(FS_CURRENT_ACTIVITY_BY_NAME).value(flowState.getCurrentActivityByName());
        }
        jsonWriter.keyValueIfNotBlankValue(FS_LOOKUP_KEY, flowState.getLookupKey());

        renderState(jsonWriter, flowState);
        return jsonWriter.endObject();
    }


    protected void renderState(IJsonWriter jsonWriter, FlowState flowState) {
        jsonWriter.key(FS_PARAMETERS);
        renderFlowsValueMap(jsonWriter, flowState);
    }

    // TO_TIRIS : This change is normally o.k. But can you please check
    // FlowStateJsonOutputRenderer to see if it does the same thing?
    // I prefer the FlowStateJsonOutputRenderer version ( with a tweak )
    // you are correct that null values should not be outputted
    // if so then see if we can delete FlowStateJsonOutputRenderer
    private void renderFlowsValueMap(IJsonWriter jsonWriter, FlowState flowState) {
        Map fsParametersMap = flowState.getExportedValuesMap();
        jsonWriter.object();
        if (fsParametersMap != null) {
            for (Object entry : fsParametersMap.entrySet()) {
                Object key = ((Map.Entry) entry).getKey();
                Object value = ((Map.Entry) entry).getValue();
                // TODO: TO_KONSTA are null values allowed for this object?
                if (key != null && value != null) {
                    jsonWriter.key(key);
                    /*
                     * All objects stored in the flow state are converted to json strings, so there
                     * are two cases from here. Either the value is a an object and starts with '{'
                     * or '[' or it is not an object.
                     */
                    if (value instanceof String) {
                        String valueAsString = (String) value;
                        if (valueAsString.startsWith("{") || valueAsString.startsWith("[")) {
                            jsonWriter.append(valueAsString);
                        } else {
                            jsonWriter.value(valueAsString);
                        }
                    }
                }
            }
        }
        jsonWriter.endObject();
    }

    /**
     * @see org.amplafi.json.JsonRenderer#fromJson(java.lang.Class, java.lang.Object, Object...)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K> K fromJson(Class<K> clazz, Object value, Object... parameters) {
        JSONObject jsonObject = (JSONObject) value;
        String lookupKey = jsonObject.getString(FS_LOOKUP_KEY);
        // TODO apply any changes back to the flowState?
        FlowState flowState = getFlowManagement().getFlowState(lookupKey);
        return (K) flowState;
    }

    /**
     * @param flowManagement the flowManagement to set
     */
    public void setFlowManagement(FlowManagement flowManagement) {
        this.flowManagement = flowManagement;
    }
    /**
     * @return the flowManagement
     */
    public FlowManagement getFlowManagement() {
        return flowManagement;
    }

}
