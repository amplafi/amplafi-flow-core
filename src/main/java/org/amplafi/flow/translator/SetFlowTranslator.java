package org.amplafi.flow.translator;

import java.util.LinkedHashSet;
import java.util.Set;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.renderers.IterableJsonOutputRenderer;


public class SetFlowTranslator<T> extends FlowCollectionTranslator<Set<? extends T>, T> {

    public SetFlowTranslator() {
        super(new IterableJsonOutputRenderer<Set<? extends T>>(false));
    }
    @Override
    public Set<? extends T> deserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serialized) {
        if ( serialized != null) {
            Set<T> set = new LinkedHashSet<T>();
            super.deserialize(flowPropertyDefinition, dataClassDefinition, set, serialized);
            return set;
        } else {
            return null;
        }
    }

    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, Set<? extends T> object) {
        return super.doSerialize(flowPropertyDefinition, dataClassDefinition, jsonWriter, object);
    }

    @Override
    public Class<?> getTranslatedClass() {
        return Set.class;
    }

    @Override
    public Set<? extends T> getDefaultObject(FlowActivity flowActivity) {
        return new LinkedHashSet<T>();
    }

}
