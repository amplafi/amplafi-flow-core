package org.amplafi.flow.flowproperty;

/**
 * This is a parameter pattern object.
 * It holds the information needed to do a deserialization
 *
 * @author patmoore
 *
 */
public class DeserializaRequest {
    public FlowPropertyProvider flowPropertyProvider;
    public FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor;
    /**
     * Used for collections? (optional)
     */
    public DataClassDefinition dataClassDefinition;
    /**
     * TODO: Seems like this should be a SerializationReader....
     */
    public Object objectToDeserialize;
}
