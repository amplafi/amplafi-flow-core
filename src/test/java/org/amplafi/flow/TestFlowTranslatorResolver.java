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

package org.amplafi.flow;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.amplafi.flow.flowproperty.DataClassDefinitionImpl;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.impl.FlowDefinitionsManagerImpl;
import org.amplafi.flow.translator.BaseFlowTranslatorResolver;
import org.amplafi.flow.translator.CharSequenceFlowTranslator;
import org.amplafi.flow.translator.ListFlowTranslator;
import org.amplafi.flow.translator.LongFlowTranslator;
import org.amplafi.flow.translator.MapFlowTranslator;
import org.amplafi.flow.translator.SetFlowTranslator;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author patmoore
 *
 */
public class TestFlowTranslatorResolver extends Assert {

    /**
     *
     */
    private static final String URI = "uri";

    public FlowTranslatorResolver getFlowTranslatorResolver() {
        BaseFlowTranslatorResolver flowTranslatorResolver = new BaseFlowTranslatorResolver();
        flowTranslatorResolver.setFlowDefinitionsManager(new FlowDefinitionsManagerImpl());
        flowTranslatorResolver.initializeService();
        return flowTranslatorResolver;
    }
    @Test
    public void testResolvingClassWithCollections() {
        FlowPropertyProvider flowPropertyProvider = null;

        DataClassDefinitionImpl dataClassDefinition = new DataClassDefinitionImpl(Long.class, Set.class);
        getFlowTranslatorResolver().resolve("", dataClassDefinition, true);
        assertEquals(dataClassDefinition.getDataClass(), Set.class);
        assertNotNull(dataClassDefinition.getFlowTranslator());
        assertTrue(dataClassDefinition.getFlowTranslator() instanceof SetFlowTranslator, dataClassDefinition.getFlowTranslator().toString());
        DataClassDefinition elementDataClassDefinition = dataClassDefinition.getElementDataClassDefinition();
        assertNotNull(elementDataClassDefinition);
        assertEquals(elementDataClassDefinition.getDataClass(), Long.class);
        assertNotNull(elementDataClassDefinition.getFlowTranslator());
        assertTrue(elementDataClassDefinition.getFlowTranslator() instanceof LongFlowTranslator, elementDataClassDefinition.getFlowTranslator().toString());

        // verify that the resolved result can do something useful
        Set<Long> result = dataClassDefinition.deserialize(flowPropertyProvider, null, "[34,45,67]");
        assertTrue(result.containsAll(Arrays.asList(34L, 45L, 67L)));
        assertEquals(result.size(), 3);

        String serializedResult = (String) dataClassDefinition.serialize(null, result);
        assertEquals(serializedResult, "[34,45,67]");
    }
    @Test
    public void testResolvingClassWithMaps() {
        FlowPropertyProvider flowPropertyProvider = null;

        DataClassDefinitionImpl dataClassDefinition = DataClassDefinitionImpl.map(Long.class, String.class, List.class);
        getFlowTranslatorResolver().resolve("", dataClassDefinition, true);

        assertEquals(dataClassDefinition.getDataClass(), Map.class);
        assertNotNull(dataClassDefinition.getFlowTranslator());
        assertTrue(dataClassDefinition.getFlowTranslator() instanceof MapFlowTranslator, dataClassDefinition.getFlowTranslator().toString());

        // look at the map's values
        DataClassDefinition elementDataClassDefinition = dataClassDefinition.getElementDataClassDefinition();
        assertNotNull(elementDataClassDefinition);
        assertEquals(elementDataClassDefinition.getDataClass(), List.class);
        assertNotNull(elementDataClassDefinition.getFlowTranslator());
        assertTrue(elementDataClassDefinition.getFlowTranslator() instanceof ListFlowTranslator,
                   elementDataClassDefinition.getFlowTranslator().toString());
        assertNotNull(elementDataClassDefinition.getElementDataClassDefinition().getFlowTranslator());
        assertTrue(elementDataClassDefinition.getElementDataClassDefinition().getFlowTranslator() instanceof CharSequenceFlowTranslator,
                   elementDataClassDefinition.getElementDataClassDefinition().getFlowTranslator().toString());

        // look at the maps's keys
        assertNotNull(dataClassDefinition.getKeyDataClassDefinition());
        assertNotNull(dataClassDefinition.getKeyDataClassDefinition().getFlowTranslator());
        assertTrue(dataClassDefinition.getKeyDataClassDefinition().getFlowTranslator() instanceof LongFlowTranslator);

        // verify that the result can be used.
        Map<Long, List<String>> map = new LinkedHashMap<Long, List<String>>();
        List<String> expected34 = Arrays.asList("foo34", "bar34");
        map.put(Long.valueOf(34), expected34);
        List<String> expected3 = Arrays.asList("foo3", "bar3");
        map.put(Long.valueOf(3), expected3);

        String serializedResult = (String) dataClassDefinition.serialize(null, map);
        assertEquals(serializedResult, "{\"34\":[\"foo34\",\"bar34\"],\"3\":[\"foo3\",\"bar3\"]}");
        Map<Long, List<String>> reMap = dataClassDefinition.deserialize(flowPropertyProvider, null, serializedResult);
        assertEquals(reMap.size(), 2);
        List<String> set34 = reMap.get(Long.valueOf(34));
        List<String> set3 = reMap.get(Long.valueOf(3));
        assertTrue(set34.containsAll(expected34));
        assertTrue(set3.containsAll(expected3));
    }
    @Test
    public void testListCollectionHandling() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl(URI, URI.class, FlowActivityPhase.advance, List.class);
        getFlowTranslatorResolver().resolve("", definition);
        List<URI> list = Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov"));
        String strV =definition.serialize(list);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");

        FlowPropertyProvider flowPropertyProvider = null;
        List<URI> result = definition.deserialize(flowPropertyProvider, strV);
        assertTrue(list.containsAll(result));
        assertTrue(result.containsAll(list));
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testSetCollectionHandling() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl(URI, URI.class, FlowActivityPhase.advance, Set.class);
        getFlowTranslatorResolver().resolve("", definition);
        Set<URI> set = new LinkedHashSet<URI>(Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov")));
        String strV =definition.serialize(set);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");
        FlowPropertyProvider flowPropertyProvider = null;
        Set<URI> result =(Set<URI>) definition.deserialize(flowPropertyProvider, strV);
        assertTrue(set.containsAll(result));
        assertTrue(set.containsAll(set));
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testMapCollectionHandling() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl(URI, URI.class, FlowActivityPhase.advance, Map.class);
        getFlowTranslatorResolver().resolve("", definition);
        Map<String, URI> map = new LinkedHashMap<String, URI>();
        map.put("first", new URI("http://foo.com"));
        map.put("second", new URI("http://gg.gov"));
        String strV =definition.serialize(map);
        assertEquals(strV, "{\"first\":\"http://foo.com\",\"second\":\"http://gg.gov\"}");
        FlowPropertyProvider flowPropertyProvider = null;
        Map<String, URI> result = (Map<String,URI>) definition.deserialize(flowPropertyProvider, strV);
        assertTrue(result.equals(map));
    }

//    /**
//     * Test to make sure the merge with the standard definition happens.
//     * Make sure that the standard definition does not change.
//     */
//    @Test(enabled=false) // for now #2179, #2192 forces these to be defined in FlowImpl
//    public void testMergingWithStandardFlowPropertyDefinition() {
//        FlowPropertyDefinitionImpl flowPropertyDefinition = new FlowPropertyDefinitionImpl(FSHIDE_FLOW_CONTROL);
//        assertEquals(flowPropertyDefinition.getDataClass(), String.class);
//        FlowPropertyDefinition standardFlowPropertyDefinition =
//                this.getFlowDefManager().getFlowPropertyDefinition(FSHIDE_FLOW_CONTROL);
//        assertEquals(standardFlowPropertyDefinition.getDataClass(), boolean.class);
//        getFlowTranslatorResolver().resolve("", flowPropertyDefinition);
//        assertEquals(flowPropertyDefinition.getDataClass(), boolean.class);
//        assertNotSame(standardFlowPropertyDefinition, flowPropertyDefinition);
//        standardFlowPropertyDefinition =
//                this.getFlowTranslatorResolver().getFlowPropertyDefinition(FSHIDE_FLOW_CONTROL);
//        assertNotSame(standardFlowPropertyDefinition, flowPropertyDefinition);
//    }
}
