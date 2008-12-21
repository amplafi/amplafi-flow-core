/*
 * Created on Jan 7, 2008
 * Copyright 2006 by Amplafi, Inc.
 */
package org.amplafi.flow.validation;

import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;


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
    public <K> K fromJson(Class<K> clazz, Object value, Object... parameters) {
        throw new UnsupportedOperationException();
    }

}
