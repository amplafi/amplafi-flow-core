package org.amplafi.flow;

import org.amplafi.json.JSONWriter;

/**
 * @author Andreas Andreou
 */
public interface DataClassDefinition {
    Class<?> getDataClass();

    DataClassDefinition getKeyDataClassDefinition();

    DataClassDefinition getElementDataClassDefinition();

    boolean isFlowTranslatorSet();

    void setFlowTranslator(FlowTranslator flowTranslator);

    <T> Object serialize(FlowPropertyDefinition flowPropertyDefinition, T value);

    @SuppressWarnings("unchecked")
    <T> JSONWriter serialize(FlowPropertyDefinition flowPropertyDefinition, JSONWriter jsonWriter, T value);

    @SuppressWarnings("unchecked")
    <T> T deserialize(FlowPropertyDefinition flowPropertyDefinition, Object value);
}
