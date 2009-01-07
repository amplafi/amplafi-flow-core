package org.amplafi.flow;

import org.amplafi.json.JSONWriter;

/**
 * Handles the issues around data structure for the {@link FlowPropertyDefinition}.
 * This way {@link FlowPropertyDefinition}  focuses on required status, name, etc. and DataClassDefinition
 * focuses just on the way the data is structured.
 * <p/>
 * Allows the FlowDefinitionProperty structure to be a middling complex chain of nested collections.
 *
 * @author patmoore
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

    /**
     * @return the {@link Class} that this definition describes
     */
    Class<?> getDataClass();

    /**
     * @return
     */
    boolean isCollection();

}
