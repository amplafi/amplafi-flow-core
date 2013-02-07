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
import java.util.Map;

import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JSONObject;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;
import org.apache.commons.lang.ObjectUtils;

import static com.sworddance.util.CUtilities.*;


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
//        jsonWriter.object();
//        try {
//            jsonWriter.key(FS_COMPLETE).value(flowState.isCompleted());
//            if (flowState.isActive()) {
//                jsonWriter.key(FS_CURRENT_ACTIVITY_BY_NAME).value(flowState.getCurrentActivityByName());
//            }
//            jsonWriter.keyValueIfNotBlankValue(FS_LOOKUP_KEY, flowState.getLookupKey());

            renderState(jsonWriter, flowState);
//        } finally {
//            jsonWriter.endObject();
//        }
        return jsonWriter;
    }


    protected void renderState(IJsonWriter jsonWriter, FlowState flowState) {
        Map<String, FlowPropertyDefinition> propertyDefinitionsMap = flowState.getPropertyDefinitions();
        if ( isNotEmpty(propertyDefinitionsMap) ) {
            Collection<FlowPropertyDefinition> propertyDefinitions = propertyDefinitionsMap.values();
            for (FlowPropertyDefinition flowPropertyDefinition : propertyDefinitions) {
            	if (flowState.isPropertySet(flowPropertyDefinition.getName()) && 
            	        flowPropertyDefinition.isExportable() &&
            	        flowPropertyDefinition.getPropertyUsage().isOutputedProperty()) {
            		renderProperty(jsonWriter, flowState, flowPropertyDefinition);
            	}
            }
        }
    }

	protected void renderProperty(IJsonWriter jsonWriter, FlowState flowState,
			FlowPropertyDefinition flowPropertyDefinition) {
	    String propertyName = flowPropertyDefinition.getName();
	    try {
	        // This is needed because the flowPropertyDefinition may have a specialized flow serialization mechanism and so we can't rely on the default
	        // serialization in the jsonWriter.
	        // TODO : explain above comment better. What is the exact use cases? Does it still apply?
	        // note that not all property values may have been serialized - but may be we can look for
	        // already serialized values? However, need to check to see if this would skip error checking
	        // when property was passed as part of the request.
    	    Object property = flowState.getProperty(propertyName);
    	    if (property != null) {
    	        // a property may be set but set to null - which we can skip handling.
    	        // This reduces the clutter of optional values being outputed.
    	        String serializedValue = ObjectUtils.toString(flowPropertyDefinition.serialize(new JSONWriter(), property), null);
    		    jsonWriter.key(propertyName);
    		    // If the property is a string, the serialization of the property will already have "
    		    // normal use of jsonWriter.value() would result in ""real string""
    		    // Using append avoids such double quoting
    		    jsonWriter.append(serializedValue);
    	    }
	    } catch (Exception e) {
	        // Don't let errors in serialization prevent the other properties from being serialized.
	        // TODO : handle errors caused by bad user data.
	        getFlowManagement().getLog().warn(flowState.getFlowPropertyProviderFullName()+": getting property "+propertyName+ " caused exception ",e);
	    }
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
