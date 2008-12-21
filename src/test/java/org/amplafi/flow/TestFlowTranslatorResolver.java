/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.flow.flowproperty.PropertyRequired;
import org.amplafi.flow.translator.BaseFlowTranslatorResolver;
import org.amplafi.flow.translator.CharSequenceFlowTranslator;
import org.amplafi.flow.translator.FlowTranslatorResolver;
import org.amplafi.flow.translator.ListFlowTranslator;
import org.amplafi.flow.translator.LongFlowTranslator;
import org.amplafi.flow.translator.MapFlowTranslator;
import org.amplafi.flow.translator.SetFlowTranslator;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.amplafi.flow.FlowConstants.*;

import java.util.Arrays;

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
        flowTranslatorResolver.addStandardFlowTranslators();
        flowTranslatorResolver.initializeService();
        return flowTranslatorResolver;
    }
    @Test
    public void testResolvingClassWithCollections() {

        DataClassDefinition dataClassDefinition = new DataClassDefinition(Long.class, Set.class);
        getFlowTranslatorResolver().resolve(dataClassDefinition);
        assertEquals(dataClassDefinition.getDataClass(), Set.class);
        assertNotNull(dataClassDefinition.getFlowTranslator());
        assertTrue(dataClassDefinition.getFlowTranslator() instanceof SetFlowTranslator, dataClassDefinition.getFlowTranslator().toString());
        DataClassDefinition elementDataClassDefinition = dataClassDefinition.getElementDataClassDefinition();
        assertNotNull(elementDataClassDefinition);
        assertEquals(elementDataClassDefinition.getDataClass(), Long.class);
        assertNotNull(elementDataClassDefinition.getFlowTranslator());
        assertTrue(elementDataClassDefinition.getFlowTranslator() instanceof LongFlowTranslator, elementDataClassDefinition.getFlowTranslator().toString());

        // verify that the resolved result can do something useful
        Set<Long> result = dataClassDefinition.deserialize(null, "[34,45,67]");
        assertTrue(result.containsAll(Arrays.asList(34L, 45L, 67L)));
        assertEquals(result.size(), 3);

        String serializedResult = (String) dataClassDefinition.serialize(null, result);
        assertEquals(serializedResult, "[34,45,67]");
    }
    @Test
    public void testResolvingClassWithMaps() {
        DataClassDefinition dataClassDefinition = DataClassDefinition.map(Long.class, String.class, List.class);
        getFlowTranslatorResolver().resolve(dataClassDefinition);

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
        map.put(new Long(34), expected34);
        List<String> expected3 = Arrays.asList("foo3", "bar3");
        map.put(new Long(3), expected3);

        String serializedResult = (String) dataClassDefinition.serialize(null, map);
        assertEquals(serializedResult.toString(), "{\"34\":[\"foo34\",\"bar34\"],\"3\":[\"foo3\",\"bar3\"]}");
        Map<Long, List<String>> reMap = dataClassDefinition.deserialize(null, serializedResult);
        assertEquals(reMap.size(), 2);
        List<String> set34 = reMap.get(new Long(34));
        List<String> set3 = reMap.get(new Long(3));
        assertTrue(set34.containsAll(expected34));
        assertTrue(set3.containsAll(expected3));
    }
    @Test
    public void testListCollectionHandling() throws Exception {
        FlowPropertyDefinition definition = new FlowPropertyDefinition(URI, URI.class, PropertyRequired.advance, List.class);
        getFlowTranslatorResolver().resolve(definition);
        List<URI> list = Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov"));
        String strV =definition.serialize(list);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");
        List<URI> result = definition.parse(strV);
        assertTrue(list.containsAll(result));
        assertTrue(result.containsAll(list));
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testSetCollectionHandling() throws Exception {
        FlowPropertyDefinition definition = new FlowPropertyDefinition(URI, URI.class, PropertyRequired.advance, Set.class);
        getFlowTranslatorResolver().resolve(definition);
        Set<URI> set = new LinkedHashSet<URI>(Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov")));
        String strV =definition.serialize(set);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");
        Set<URI> result =(Set<URI>) definition.parse(strV);
        assertTrue(set.containsAll(result));
        assertTrue(set.containsAll(set));
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testMapCollectionHandling() throws Exception {
        FlowPropertyDefinition definition = new FlowPropertyDefinition(URI, URI.class, PropertyRequired.advance, Map.class);
        getFlowTranslatorResolver().resolve(definition);
        Map<String, URI> map = new LinkedHashMap<String, URI>();
        map.put("first", new URI("http://foo.com"));
        map.put("second", new URI("http://gg.gov"));
        String strV =definition.serialize(map);
        assertEquals(strV, "{\"first\":\"http://foo.com\",\"second\":\"http://gg.gov\"}");
        Map<String, URI> result = (Map<String,URI>) definition.parse(strV);
        assertTrue(result.equals(map));
    }

    /**
     * Test to make sure the merge with the standard definition happens.
     * Make sure that the standard definition does not change.
     * @throws Exception
     */
    @Test(enabled=false) // for now #2179, #2192 forces these to be defined in FlowImpl
    public void testMergingWithStandardFlowPropertyDefinition() throws Exception {
        FlowPropertyDefinition flowPropertyDefinition = new FlowPropertyDefinition(FSHIDE_FLOW_CONTROL);
        assertEquals(flowPropertyDefinition.getDataClass(), String.class);
        FlowPropertyDefinition standardFlowPropertyDefinition = this.getFlowTranslatorResolver().getFlowPropertyDefinition(FSHIDE_FLOW_CONTROL);
        assertEquals(standardFlowPropertyDefinition.getDataClass(), boolean.class);
        getFlowTranslatorResolver().resolve(flowPropertyDefinition);
        assertEquals(flowPropertyDefinition.getDataClass(), boolean.class);
        assertNotSame(standardFlowPropertyDefinition, flowPropertyDefinition);
        standardFlowPropertyDefinition = this.getFlowTranslatorResolver().getFlowPropertyDefinition(FSHIDE_FLOW_CONTROL);
        assertNotSame(standardFlowPropertyDefinition, flowPropertyDefinition);
    }
}
