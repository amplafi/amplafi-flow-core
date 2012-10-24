/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package org.amplafi.flow.translator;

import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.DataClassDefinitionImpl;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


import static org.testng.Assert.*;

import java.util.Map;
import java.util.HashMap;

/**
 * Recreate the {@link org.amplafi.flow.translator.FlowTranslator}s a bunch of times to make sure that
 * no state is passed between serialize and deserialize steps.
 * @param <T>
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

    protected DataClassDefinitionImpl createDataClassDefinition() {
        FlowTranslator<T> flowTranslator = createFlowTranslator();
        DataClassDefinitionImpl dataClassDefinition = new DataClassDefinitionImpl(flowTranslator.getTranslatedClass());
        dataClassDefinition.setFlowTranslator(flowTranslator);
        return dataClassDefinition;
    }

    protected FlowPropertyDefinitionImpl createFlowPropertyDefinition() {
        return new FlowPropertyDefinitionImpl("foo", createDataClassDefinition());
    }

    @Test(dataProvider="flowTranslatorExpectations")
    public void serializeDeserializeAndCompare(Object object, String expectedSerialize) throws Exception {

        FlowPropertyDefinitionImplementor flowPropertyDefinition = createFlowPropertyDefinition();
        DataClassDefinition dataClassDefinition = flowPropertyDefinition.getDataClassDefinition();

        String actual = flowPropertyDefinition.serialize(object);

        FlowPropertyProvider flowPropertyProvider = null;
        Object deserialized = createFlowTranslator().deserialize(flowPropertyProvider, flowPropertyDefinition, dataClassDefinition, actual);

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
