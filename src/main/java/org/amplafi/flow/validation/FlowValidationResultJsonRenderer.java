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

package org.amplafi.flow.validation;

import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;
import org.amplafi.flow.FlowValidationResult;
import org.amplafi.flow.FlowValidationTracking;


/**
 * Used to render as a json object the flow validation result.
 *
 * @see FlowValidationTrackingJsonRenderer
 * @author Patrick Moore
 */
public class FlowValidationResultJsonRenderer implements JsonRenderer<FlowValidationResult> {

    @Override
    public Class<FlowValidationResult> getClassToRender() {
        return FlowValidationResult.class;
    }

    @Override
    public JSONWriter toJson(JSONWriter jsonWriter, FlowValidationResult flowValidationResult) {
        jsonWriter.object();
        if ( !flowValidationResult.isValid() ) {
            jsonWriter.key("flowValidationTracking");
            jsonWriter.array();
            for(FlowValidationTracking tracking:flowValidationResult.getTrackings()) {
                jsonWriter.value(tracking);
            }
            jsonWriter.endArray();
        }
        return jsonWriter.endObject();
    }

    /**
     * @see org.amplafi.json.JsonRenderer#fromJson(java.lang.Class, java.lang.Object, Object...)
     */
    @Override
    @SuppressWarnings("unused")
    public <K> K fromJson(Class<K> clazz, Object value, Object... parameters) {
        throw new UnsupportedOperationException();
    }

}
