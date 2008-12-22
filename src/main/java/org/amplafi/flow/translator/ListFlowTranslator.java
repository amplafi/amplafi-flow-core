package org.amplafi.flow.translator;

import java.util.ArrayList;
import java.util.List;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.json.renderers.IterableJsonOutputRenderer;


public class ListFlowTranslator<T> extends FlowCollectionTranslator<List<? extends T>, T> {
    public ListFlowTranslator() {
        super(new IterableJsonOutputRenderer<List<? extends T>>());
    }
    @SuppressWarnings("unchecked")
    @Override
    public List<? extends T> deserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) {
        if ( serializedObject != null ) {
            List<T> list = new ArrayList<T>();
            super.deserialize(flowPropertyDefinition, dataClassDefinition, list, serializedObject);
            return list;
        } else {
            return null;
        }
    }

    @Override
    public Class<?> getTranslatedClass() {
        return List.class;
    }

    @Override
    public List<? extends T> getDefaultObject(FlowActivity flowActivity) {
        return new ArrayList<T>();
    }

}
