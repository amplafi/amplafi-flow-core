package org.amplafi.flow.translator;

import org.amplafi.flow.FlowRenderer;

/**
 * default implementation to output {@link Enum}.
 * @author Patrick Moore
 * @param <T>
 *
 */
@SuppressWarnings("unchecked")
public class EnumFlowRenderer<T extends Enum<T>> implements FlowRenderer<T> {

    public static final EnumFlowRenderer INSTANCE = new EnumFlowRenderer();
    public Class getClassToRender() {
        return Enum.class;
    }
    public <W extends SerializationWriter> W toSerialization(W jsonWriter, T o) {
    	if (jsonWriter.isInKeyMode()) {
    		jsonWriter.key(o.toString());
    	} else {
    		jsonWriter.value(o.toString());
    	}
        return jsonWriter;
    }

    public <K> K fromSerialization(Class<K> clazz, Object value, Object...parameters) {
        if ( value == null ) {
            return null;
        } else if ( clazz.isAssignableFrom(value.getClass())) {
        	return (K) value;
        } else {
//	        try {
	            return (K) Enum.valueOf((Class<? extends Enum>) clazz, value.toString());
//	        } catch (IllegalArgumentException e) {
//	            throw new JSONException(e);
//	        }
	    }
	}
}