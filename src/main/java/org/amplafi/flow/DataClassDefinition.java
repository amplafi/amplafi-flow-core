package org.amplafi.flow;

import org.amplafi.json.JSONWriter;

/**
 * @author Andreas Andreou
 */
public interface DataClassDefinition {

    DataClassDefinition getKeyDataClassDefinition();

    DataClassDefinition getElementDataClassDefinition();

    <T> Object serialize(FlowPropertyDefinition flowPropertyDefinition, T value);

    @SuppressWarnings("unchecked")
    <T> JSONWriter serialize(FlowPropertyDefinition flowPropertyDefinition, JSONWriter jsonWriter, T value);

    @SuppressWarnings("unchecked")
    <T> T deserialize(FlowPropertyDefinition flowPropertyDefinition, Object value);

    /**
     * @return the flowTranslator
     */
    FlowTranslator getFlowTranslator();
    void setFlowTranslator(FlowTranslator flowTranslator);
    boolean isFlowTranslatorSet();

    /**
     * @return the element class (after unpeeling all the collection )
     */
    Class<?> getElementClass();

    /**
     * @return
     */
    Class<? extends Object> getCollection();

    /**
     * @return
     */
    boolean isDataClassDefined();
    /**
     * @param dataClass
     */
    void setDataClass(Class<? extends Object> dataClass);
    Class<?> getDataClass();

    /**
     * @return
     */
    boolean isCollection();

}
