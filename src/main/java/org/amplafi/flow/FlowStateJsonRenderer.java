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
import org.amplafi.json.JsonConstruct;
import org.amplafi.json.JsonRenderer;
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

    @Override
    public Class<FlowState> getClassToRender() {
        return FlowState.class;
    }

    @Override
    public IJsonWriter toJson(IJsonWriter jsonWriter, FlowState flowState) {
        renderState(jsonWriter, flowState);
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
    	    Object property = flowState.getRawProperty(propertyName);
    	    //Only request property from flow state when there is no raw (already serialized) property available.
    	    //Avoids re-serealization overhead and allows JsonSelfRenderers not to implement from json.
    	    if (property == null) {
    	        //Only get the value if not serialized yet.
    	        property = flowState.getProperty(propertyName);
    	    } else {
    	        //If serialized, try to get JsonConstruct out of it to avoid overqouting.
    	        JsonConstruct json = JsonConstruct.Parser.toJsonConstruct((String) property);
    	        if (json != null) {
    	            property = json;
    	        }
    	    }
    	    if (property != null) {
    	        // a property may be set but set to null - which we can skip handling.
    	        // This reduces the clutter of optional values being outputed.
    		    jsonWriter.key(propertyName);
    		    jsonWriter.value(property);
    	    }
	    } catch (Exception e) {
	        // Don't let errors in serialization prevent the other properties from being serialized.
	        // TODO : handle errors caused by bad user data.
	        //getFlowManagement().getLog().warn(flowState.getFlowPropertyProviderFullName()+": getting property "+propertyName+ " caused exception ",e);
	    }
	}

    /**
     * @see org.amplafi.json.JsonRenderer#fromJson(java.lang.Class, java.lang.Object, Object...)
     */
    @Override
    public <K> K fromJson(Class<K> clazz, Object value, Object... parameters) {
        throw new UnsupportedOperationException();
    }

}
