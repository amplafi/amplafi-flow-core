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

import java.util.Collection;

import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JSONObject;
import org.amplafi.json.JsonRenderer;



/**
 * used to render a flow state as part of the api / flow service functionality.
 * @author Patrick Moore
 */
public class FlowStateJsonRenderer implements JsonRenderer<FlowState> {

    public static final FlowStateJsonRenderer INSTANCE = new FlowStateJsonRenderer();
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
        // TODO: TO_KONSTA: The FlowStateJsonOutputRenderer indeed was that same output save the tweak. I have added the tweak to the TestFlowStateJsonRenderer, integrated the changes into this class and removed the FlowStateJsonOutputRenderer class. Please just remove this comment after you have read it.
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
