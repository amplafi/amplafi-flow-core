/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;

import org.amplafi.flow.translator.CharSequenceFlowTranslator;
import org.amplafi.flow.translator.FlowTranslator;
import org.amplafi.json.JSONWriter;
import org.apache.commons.lang.builder.EqualsBuilder;


/**
 * Handles the issues around data structure for the {@link FlowPropertyDefinition}. This way {@link FlowPropertyDefinition}
 * focuses on required status, name, etc. and DataClassDefinition focuses just on the way the data is structured.
 * Allows the FlowDefinitionProperty structure to be a middling complex chain of nested collections.
 *
 * @author patmoore
 *
 */
public class DataClassDefinition {
    private Class<?> dataClass;
    @SuppressWarnings("unchecked")
    private FlowTranslator flowTranslator;
    private DataClassDefinition keyDataClassDefinition;
    private DataClassDefinition elementDataClassDefinition;

    public static final DataClassDefinition DEFAULT;
    static {
        DEFAULT = new DataClassDefinition(String.class);
        DEFAULT.setFlowTranslator(CharSequenceFlowTranslator.INSTANCE);
    }
    public DataClassDefinition() {
    }

    /**
     * Used to define Map structures that have complex keys.
     * @param mapClass
     * @param keyClassDefinition
     * @param elementClassDefinition
     */
    @SuppressWarnings("unchecked")
    public DataClassDefinition(Class<? extends Map> mapClass, DataClassDefinition keyClassDefinition, DataClassDefinition elementClassDefinition) {
        this.dataClass = mapClass;
        this.setKeyDataClassDefinition(keyClassDefinition);
        this.setElementDataClassDefinition(elementClassDefinition);
    }
    /**
     * clone ctor
     * @param dataClassDefinition
     */
    public DataClassDefinition(DataClassDefinition dataClassDefinition) {
        this.dataClass = dataClassDefinition.dataClass;
        this.setKeyDataClassDefinition(dataClassDefinition.keyDataClassDefinition == null? null: new DataClassDefinition(dataClassDefinition.keyDataClassDefinition));
        this.setElementDataClassDefinition(dataClassDefinition.elementDataClassDefinition == null? null: new DataClassDefinition(dataClassDefinition.elementDataClassDefinition));
        this.flowTranslator = dataClassDefinition.flowTranslator;
    }
    // don't use yet.
    public DataClassDefinition(Class<?> element, Class<?>... collections) {
        if ( collections.length == 0 ) {
            this.setDataClass(element);
        } else if ( collections.length == 1 ) {
            this.setDataClass(collections[0]);
            this.setElementDataClassDefinition(new DataClassDefinition(element));
        } else {
            this.setDataClass(collections[0]);
            this.setElementDataClassDefinition(new DataClassDefinition(element, Arrays.copyOfRange(collections, 1, collections.length)));
        }
    }

    /**
     * Helper to define a map.
     * @param keyClass
     * @param elementClass
     * @param collectionClasses
     * @return a {@link DataClassDefinition} that defines a {@link Map} property.
     */
    public static DataClassDefinition map(Class<?> keyClass, Class<?> elementClass, Class<?>... collectionClasses) {
        return new DataClassDefinition(Map.class, new DataClassDefinition(keyClass), new DataClassDefinition(elementClass, collectionClasses));
    }
    /**
     * Helper to define a NavigableMap (TreeMap for example)
     * @param keyClass
     * @param elementClass
     * @param collectionClasses
     * @return a {@link DataClassDefinition} that defines a {@link NavigableMap} property.
     */
    public static DataClassDefinition navigableMap(Class<?> keyClass, Class<?> elementClass, Class<?>... collectionClasses) {
        return new DataClassDefinition(NavigableMap.class, new DataClassDefinition(keyClass), new DataClassDefinition(elementClass, collectionClasses));
    }
    /**
     * Helper to define a map.
     * @param keyClass
     * @param elementDataClassDefinition
     * @return a {@link DataClassDefinition} that defines a {@link Map} property.
     */
    public static DataClassDefinition map(Class<?> keyClass, DataClassDefinition elementDataClassDefinition) {
        return new DataClassDefinition(Map.class, new DataClassDefinition(keyClass), elementDataClassDefinition);
    }
    /**
     * @param <T>
     * @param flowPropertyDefinition
     * @param value
     * @return deserialized value.
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(FlowPropertyDefinition flowPropertyDefinition, Object value) {
        return (T) this.getFlowTranslator().deserialize(flowPropertyDefinition, this, value);
    }
    public <T> Object serialize(FlowPropertyDefinition flowPropertyDefinition, T value) {
        if ( value == null) {
            return null;
        } else {
            JSONWriter jsonWriter = this.serialize(flowPropertyDefinition, null, value);
            String strV = jsonWriter.toString();
            // TODO: trimming quotes is probably not needed anymore - CharSequenceFlowTranslator uses unquote...
            if (strV != null && strV.startsWith("\"") && strV.endsWith("\"")) {
                // trim off unneeded " that appear when handling simple objects.
                strV = strV.substring(1, strV.length()-1);
            }
            return strV;
        }
    }
    @SuppressWarnings("unchecked")
    public <T> JSONWriter serialize(FlowPropertyDefinition flowPropertyDefinition, JSONWriter jsonWriter, T value) {
        return this.getFlowTranslator().serialize(flowPropertyDefinition, this, jsonWriter, value);
    }

    /**
     * @param dataClassDefinition
     */
    public void merge(DataClassDefinition dataClassDefinition) {
        if ( dataClassDefinition == null ) {
            return;
        }
        if ( getDataClassReplaced(dataClassDefinition) == Boolean.TRUE) {
            this.dataClass = dataClassDefinition.dataClass;
        }
        this.setElementDataClassDefinition(mergeIt(this.elementDataClassDefinition, dataClassDefinition.elementDataClassDefinition));
        this.setKeyDataClassDefinition(mergeIt(this.keyDataClassDefinition, dataClassDefinition.keyDataClassDefinition));
    }

    /**
     * @param dataClassDefinition
     */
    private DataClassDefinition mergeIt(DataClassDefinition original, DataClassDefinition dataClassDefinition) {
        if ( dataClassDefinition != null ) {
            if( original == null ) {
                return new DataClassDefinition(dataClassDefinition);
            } else {
                original.merge(dataClassDefinition);
            }
        }
        return original;
    }
    public boolean isMergable(DataClassDefinition dataClassDefinition) {
        if(equals(dataClassDefinition) || dataClassDefinition == null) {
            return true;
        } else if (dataClassDefinition.dataClass != null &&getDataClassReplaced(dataClassDefinition)==null) {
            return false;
        } else if (this.elementDataClassDefinition != null
                && !this.elementDataClassDefinition.isMergable(dataClassDefinition.elementDataClassDefinition)) {
            return false;
        } else if (this.keyDataClassDefinition != null
                && !this.keyDataClassDefinition.isMergable(dataClassDefinition.keyDataClassDefinition)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param dataClassDefinition
     * @return
     */
    private Boolean getDataClassReplaced(DataClassDefinition dataClassDefinition) {
        if ( dataClassDefinition == null || this.dataClass == dataClassDefinition.dataClass || dataClassDefinition.dataClass == null) {
            return false;
        } else if ( this.dataClass == null ) {
            return true;
        } else {
            // currently have a superclass ( we know because earlier == check failed. )
            return this.dataClass.isAssignableFrom(dataClassDefinition.dataClass)?true:null;
        }
    }

    /**
     * if this represents a collection then the {@link #elementDataClassDefinition} will not be null.
     * @return the collection
     */
    public Class<?> getCollection() {
        return this.isCollection()?this.getDataClass():null;
    }
    /**
     * @param dataClass the dataClass to set
     */
    public void setDataClass(Class<?> dataClass) {
        this.dataClass = dataClass != String.class && dataClass != CharSequence.class?dataClass:null;
        this.flowTranslator = null;
    }
    /**
     * @return the dataClass
     */
    public Class<?> getDataClass() {
        if ( this.dataClass != null ) {
            return dataClass;
        } else if ( isMap()){
            return Map.class;
        } else if ( isCollection()) {
            return Collection.class;
        } else {
            return String.class;
        }
    }

    public boolean isDataClassDefined() {
        return dataClass != null;
    }
    @Override
    public boolean equals(Object o) {
        if ( o == this) {
            return true;
        } else if ( o == null || !(o instanceof DataClassDefinition)) {
            return false;
        }
        DataClassDefinition dataClassDefinition = (DataClassDefinition) o;
        // selectively use the accessor methods so that defaults will compare to the equivalent explicitly
        // specified value.
        // cannot do this on element and key because of infinite loop.
        EqualsBuilder equalsBuilder = new EqualsBuilder()
            .append(this.getDataClass(), dataClassDefinition.getDataClass())
            .append(this.elementDataClassDefinition, dataClassDefinition.elementDataClassDefinition)
            .append(this.keyDataClassDefinition, dataClassDefinition.keyDataClassDefinition)
            .append(this.getFlowTranslator(), dataClassDefinition.getFlowTranslator());
        return equalsBuilder.isEquals();
    }
    /**
     * @param keyDataClassDefinition the keyDataClassDefinition to set
     */
    public void setKeyDataClassDefinition(DataClassDefinition keyDataClassDefinition) {
        this.keyDataClassDefinition = keyDataClassDefinition;
    }
    /**
     * @return the keyDataClassDefinition
     */
    public DataClassDefinition getKeyDataClassDefinition() {
        if ( keyDataClassDefinition != null) {
            return keyDataClassDefinition;
        } else if ( isMap() ){
            return DEFAULT;
        } else {
            return null;
        }
    }
    /**
     * @param elementDataClassDefinition the elementDataClassDefinition to set
     */
    public void setElementDataClassDefinition(DataClassDefinition elementDataClassDefinition) {
        this.elementDataClassDefinition = elementDataClassDefinition;
    }
    /**
     * @return the elementDataClassDefinition
     */
    public DataClassDefinition getElementDataClassDefinition() {
        if ( elementDataClassDefinition != null ) {
            return elementDataClassDefinition;
        } else if ( isCollection()){
            return DEFAULT;
        } else {
            return null;
        }
    }
    /**
     * @param flowTranslator the flowTranslator to set
     */
    @SuppressWarnings("unchecked")
    public void setFlowTranslator(FlowTranslator flowTranslator) {
        this.flowTranslator = flowTranslator;
    }
    /**
     * @return the flowTranslator
     */
    @SuppressWarnings("unchecked")
    public FlowTranslator getFlowTranslator() {
        if ( this.flowTranslator == null ) {
            return CharSequenceFlowTranslator.INSTANCE;
        } else {
            return flowTranslator;
        }
    }

    /**
     * @param clazz
     * @return true if objects of 'clazz' can be stored in the structure defined by this.
     */
    @SuppressWarnings("unchecked")
    public boolean isAssignableFrom(Class<?> clazz) {
        return getFlowTranslator().isAssignableFrom(clazz);
    }

    @Override
    public String toString() {
        String name = this.getDataClass().getName();
        if ( isCollection()) {
            name +="<";
            if ( this.keyDataClassDefinition != null || Map.class.isAssignableFrom(this.getDataClass())) {
                name += this.getKeyDataClassDefinition()+", ";
            }
            if ( isCollection()) {
                name += this.getElementDataClassDefinition();
            }
            name +=">";
        }
        return name;
    }

    public boolean isCollection() {
        return
            this.elementDataClassDefinition != null
            || isMap()
            || (this.dataClass != null && Collection.class.isAssignableFrom(this.dataClass))
            ;
    }

    /**
     * @return this represents a map
     */
    public boolean isMap() {
        return this.keyDataClassDefinition != null
        || (this.dataClass != null && Map.class.isAssignableFrom(this.dataClass));
    }

    /**
     * @param flowPropertyDefinition
     * @param value
     * @return true if value can be deserialized
     */
    public boolean isDeserializable(FlowPropertyDefinition flowPropertyDefinition, Object value) {
        return value == null || this.getFlowTranslator().isDeserializable(flowPropertyDefinition, this, value);
    }

    /**
     * @return true if the flowTranslator is set or {@link #getDataClass()} == String.class
     */
    public boolean isFlowTranslatorSet() {
        return this.flowTranslator != null || this.getDataClass() == String.class;
    }
}