/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.translator;

import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.flow.translator.BaseFlowTranslatorResolver;
import org.amplafi.flow.translator.FlowTranslator;
import org.amplafi.flow.translator.FlowTranslatorResolver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


import static org.testng.Assert.*;

import java.util.Map;
import java.util.HashMap;

/**
 * Recreate the {@link FlowTranslator}s a bunch of times to make sure that
 * no state is passed between serialize and deserialize steps.
 *
 */
public abstract class AbstractTestFlowTranslators<T> {

    private FlowTranslatorResolver flowTranslatorResolver;
    protected abstract FlowTranslator<T> createFlowTranslator();

    /**
     *
     * @return array of Object[]'s which in turn are (Object original, String serialized form)
     */
    @DataProvider(name="flowTranslatorExpectations")
    protected abstract Object[][] getFlowTranslatorExpectations();

    protected DataClassDefinition createDataClassDefinition() {
        FlowTranslator<T> flowTranslator = createFlowTranslator();
        DataClassDefinition dataClassDefinition = new DataClassDefinition(flowTranslator.getTranslatedClass());
        dataClassDefinition.setFlowTranslator(flowTranslator);
        return dataClassDefinition;
    }

    protected FlowPropertyDefinition createFlowPropertyDefinition() {
        return new FlowPropertyDefinition("foo", createDataClassDefinition());
    }

    @Test(dataProvider="flowTranslatorExpectations")
    public void serializeDeserializeAndCompare(Object object, String expectedSerialize) throws Exception {

        FlowPropertyDefinition flowPropertyDefinition = createFlowPropertyDefinition();
        DataClassDefinition dataClassDefinition = flowPropertyDefinition.getDataClassDefinition();

        String actual = flowPropertyDefinition.serialize(object);

        Object deserialized = createFlowTranslator().deserialize(flowPropertyDefinition, dataClassDefinition, actual);

        compareResults(object, expectedSerialize, actual, deserialized);
    }

    /**
     * some tests need to override if the re-deserialized objects aren't expected to be identical to the original object
     * @param object
     * @param expectedSerialize
     * @param actual
     * @param deserialized
     */
    protected void compareResults(Object object, String expectedSerialize, String actual, Object deserialized) {
        assertEquals(actual, expectedSerialize);
        assertEquals(deserialized, object);
    }

    /**
     * @param flowTranslatorResolver the flowTranslatorResolver to set
     */
    public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver) {
        this.flowTranslatorResolver = flowTranslatorResolver;
    }

    /**
     * @return the flowTranslatorResolver
     */
    public FlowTranslatorResolver getFlowTranslatorResolver() {
        if ( flowTranslatorResolver == null) {
            this.flowTranslatorResolver = new BaseFlowTranslatorResolver();
            ((BaseFlowTranslatorResolver)this.flowTranslatorResolver).addStandardFlowTranslators();
            ((BaseFlowTranslatorResolver)this.flowTranslatorResolver).initializeService();
        }
        return flowTranslatorResolver;
    }

    /**
     * Creates an array having the given data twice.
     */
    protected static Object[] twins(Object data) {
        return new Object[]{data, data};
    }

    /**
     * Creates an array with the given data.
     */
    protected static Object[] data(Object... data) {
        return data;
    }

    @SuppressWarnings("unchecked")
    protected static <K,T> Map<K,T> map(Object... data) {
        Map<K,T> map = new HashMap<K,T>();
        for (int i=0; i<data.length; i+=2) {
            map.put((K)data[i], (T)data[i+1]);
        }
        return map;
    }
}
