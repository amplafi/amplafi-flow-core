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

package org.amplafi.flow.translator;

import org.amplafi.flow.validation.FlowValidationTracking;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JsonRenderer;


/**
 * JsonRender to render validation results. This is used at least initially in the api code.
 *
 * @author Patrick Moore
 */
public class FlowValidationTrackingJsonRenderer implements JsonRenderer<FlowValidationTracking> {

    public static final FlowValidationTrackingJsonRenderer INSTANCE = new FlowValidationTrackingJsonRenderer();
    @Override
    public Class<FlowValidationTracking> getClassToRender() {
        return FlowValidationTracking.class;
    }

    @Override
    public IJsonWriter toJson(IJsonWriter jsonWriter, FlowValidationTracking flowValidationTracking) {
        jsonWriter.object();
        jsonWriter.key("key").value(flowValidationTracking.getMessageKey());
        if ( flowValidationTracking.getMessageParameters() != null ) {
            jsonWriter.key("parameters");
            jsonWriter.array();
            for(Object parameter: flowValidationTracking.getMessageParameters()) {
                jsonWriter.value(parameter);
            }
            jsonWriter.endArray();
        }
        return jsonWriter.endObject();
    }

    /**
     * @see org.amplafi.json.JsonRenderer#fromJson(java.lang.Class, java.lang.Object, Object...)
     */
    @Override
    public <K> K fromJson(Class<K> clazz, Object value, Object... parameters) {
        throw new UnsupportedOperationException();
    }

}
