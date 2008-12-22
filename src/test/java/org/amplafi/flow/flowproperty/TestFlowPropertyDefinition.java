package org.amplafi.flow.flowproperty;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.*;

import org.amplafi.flow.flowproperty.DataClassDefinitionImpl;
import org.amplafi.flow.*;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.PropertyRequired;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;


/**
 * Tests {@link FlowPropertyDefinitionImpl}.
 */
public class TestFlowPropertyDefinition {
    @Test
    public void testValidateWith_empty() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl();
        definition.validateWith("a", "b");

        assertFalse(definition.isRequired());

        definition.addValidator("required");
        assertTrue(definition.isRequired());
        assertEquals(definition.getValidators(), "flowField=a-b,required");
    }

    @Test
    public void testValidateWith_required() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Boolean.class, PropertyRequired.advance);
        definition.validateWith("pass");

        assertTrue(definition.isRequired());
        assertEquals(definition.getValidators(), "required,flowField=pass");
    }

    @Test
    public void testUriProperty() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("uri", URI.class, PropertyRequired.advance);
        new FlowTestingUtils().resolveAndInit(definition);
        assertNull(definition.getDefaultValue());
        assertNull(definition.getDefaultObject(null));
    }

    @Test
    public void testDefaultObjectForPrimitives() {
        assertDefaultObject(Boolean.class, false);
        assertDefaultObject(boolean.class, false);
        assertDefaultObject(Long.class, 0L);
        assertDefaultObject(long.class, 0L);
        assertDefaultObject(Integer.class, 0);
        assertDefaultObject(int.class, 0);
        assertDefaultObject(Short.class, (short) 0);
        assertDefaultObject(short.class, (short) 0);
    }

    /**
     * test default object with autoCreate set.
     * @throws Exception
     */
    @Test
    public void testDefaultHandlingWithAutoCreate() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo");
        new FlowTestingUtils().resolveAndInit(definition);
        assertFalse(definition.isAutoCreate());
        definition.initDefaultObject(Boolean.TRUE);
        assertEquals(definition.getDataClass(), Boolean.class);
        Boolean t = (Boolean) definition.parse(null);
        assertNull(t);
        t = (Boolean) definition.getDefaultObject(null);
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());

        // check behavior if everything is not defined in the Ctor call
        // (i.e. like the definition is being defined in the hivemind.xml)
        definition = new FlowPropertyDefinitionImpl("foo");
        definition.setDefaultValue("true");
        definition.setDataClass(Boolean.class);
        new FlowTestingUtils().resolveAndInit(definition);
        t = (Boolean) definition.parse(null);
        assertNull(t);
        t = (Boolean) definition.getDefaultObject(null);
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());

        final FlowPropertyDefinitionImpl definition1 = new FlowPropertyDefinitionImpl("foo");
        definition1.setFlowPropertyValueProvider(new FlowPropertyValueProvider<FlowActivity>() {
            @Override
            public <T> T get(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
                assertSame(flowPropertyDefinition, definition1);
                return (T) Boolean.TRUE;
            }

        });
        t = (Boolean) definition.getDefaultObject(null);
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());
    }

    private void assertDefaultObject(Class<?> clazz, Object value) {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("u", clazz, PropertyRequired.advance);
        new FlowTestingUtils().resolveAndInit(definition);
        assertEquals(definition.getDefaultObject(null), value);
    }

    @Test
    public void testFlowPropertyDefinitionCtor() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Boolean.class, PropertyRequired.advance);
        new FlowTestingUtils().resolveAndInit(definition);
        assertTrue(definition.isRequired());
        assertEquals(definition.getName(), "foo");

        definition = new FlowPropertyDefinitionImpl("foo1", Boolean.class).initDefaultObject(true);
        assertFalse(definition.isRequired());
        assertEquals(definition.getName(), "foo1");
    }

    /**
     * Check merging with no collection types
     * @throws Exception
     */
    @Test
    public void testFlowPropertyDefinitionSimpleMerging() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Boolean.class);
        FlowPropertyDefinitionImpl definition1 = new FlowPropertyDefinitionImpl("foo", Boolean.class).initDefaultObject(true);
        assertTrue( definition.isMergeable(definition1));
        assertTrue( definition1.isMergeable(definition));
        definition.merge(definition1);
        assertTrue((Boolean)definition.getDefaultObject(null));
        assertEquals(definition.getDataClass(), definition1.getDataClass());
    }

    @Test
    public void testFlowPropertyDefinitionComplexMerging() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Boolean.class, Set.class);
        FlowPropertyDefinitionImpl definition1 = new FlowPropertyDefinitionImpl("foo", Boolean.class).initDefaultObject(true);
        assertFalse( definition.isMergeable(definition1));
        assertFalse( definition1.isMergeable(definition));

        // definition with unknown element in a set should be able to merge with a set that has a defined element type.
        FlowPropertyDefinitionImpl definition2 = new FlowPropertyDefinitionImpl("foo", null, Set.class);
        assertTrue( definition.isMergeable(definition2));

        // merge check will be false because List and Boolean are not assignable between each other.
        FlowPropertyDefinitionImpl definition3 = new FlowPropertyDefinitionImpl("foo", Long.class, Set.class, List.class);
        assertFalse( definition.isMergeable(definition3));
        assertTrue(definition2.isMergeable(definition3));
        assertTrue(definition3.isMergeable(definition2));

        FlowPropertyDefinitionImpl definition4 = new FlowPropertyDefinitionImpl("foo", Boolean.class, Map.class);
        assertFalse(definition4.isMergeable(definition));
        assertFalse(definition4.isMergeable(definition3));
        FlowPropertyDefinitionImpl definition5 = new FlowPropertyDefinitionImpl("foo", Boolean.class, Map.class, Set.class);
        FlowPropertyDefinitionImpl definition6 = new FlowPropertyDefinitionImpl("foo", null, Map.class, Set.class);
        assertTrue(definition5.isMergeable(definition6));
        assertTrue(definition6.isMergeable(definition5));
    }

    @Test
    public void testDataClassDefinition() throws Exception {
        DataClassDefinitionImpl dataClassDefinition =
            new DataClassDefinitionImpl(Boolean.class, Set.class);
        assertEquals(dataClassDefinition.getElementDataClassDefinition().getDataClass(), Boolean.class);
        assertEquals(dataClassDefinition.getDataClass(), Set.class);
    }

    @Test
    public void testDataClassDefinitionCollection() throws Exception {
        DataClassDefinitionImpl dataClassDefinition =
            new DataClassDefinitionImpl(Boolean.class, Set.class);
        assertTrue(dataClassDefinition.isCollection());
        dataClassDefinition =
            new DataClassDefinitionImpl(Set.class);
        assertTrue(dataClassDefinition.isCollection());
        assertFalse(dataClassDefinition.isMap());

        dataClassDefinition =
            new DataClassDefinitionImpl(Map.class);
        assertTrue(dataClassDefinition.isCollection());
        assertTrue(dataClassDefinition.isMap());
        assertEquals(dataClassDefinition.getDataClass(), Map.class );
        dataClassDefinition =
            new DataClassDefinitionImpl(List.class);
        assertTrue(dataClassDefinition.isCollection());
        assertFalse(dataClassDefinition.isMap());
    }

    @Test
    public void testFlowPropertyDefinitionCloning() throws Exception {
        FlowPropertyDefinitionImpl original = new FlowPropertyDefinitionImpl("foo", Boolean.class, PropertyRequired.advance, Set.class, List.class);
        FlowPropertyDefinitionImpl cloned = new FlowPropertyDefinitionImpl(original);
        assertEquals(original, cloned, "cloning failed");
    }

    @Test(dataProvider="serializationData")
    public void testSerializeAndParse(String original) {
        FlowPropertyDefinitionImpl def = new FlowPropertyDefinitionImpl("test");
        String result = def.parse(def.serialize(original));
        assertEquals(result, original);
    }

    private static final String URI = "uri";

    /**
     * Tests to make sure that {@link FlowPropertyDefinitionImpl} can handle collections.
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testListCollectionHandling() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl(URI, URI.class, PropertyRequired.advance, List.class);
        new FlowTestingUtils().resolveAndInit(definition);
        List<URI> list = Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov"));
        String strV = definition.serialize(list);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");
        List<URI> result = (List<URI>) definition.parse(strV);
        assertTrue(list.containsAll(result));
        assertTrue(result.containsAll(list));
    }
    /**
     * test {@link Set} handling
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSetCollectionHandling() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl(URI, URI.class, PropertyRequired.advance, Set.class);
        new FlowTestingUtils().resolveAndInit(definition);
        Set<URI> set = new LinkedHashSet<URI>(Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov")));
        String strV = definition.serialize(set);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");
        Set<URI> result = (Set<URI>) definition.parse(strV);
        assertTrue(set.containsAll(result));
        assertTrue(set.containsAll(set));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMapCollectionHandling() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl(URI, URI.class, PropertyRequired.advance, Map.class);
        new FlowTestingUtils().resolveAndInit(definition);
        Map<String, URI> map = new LinkedHashMap<String, URI>();
        map.put("first", new URI("http://foo.com"));
        map.put("second", new URI("http://gg.gov"));
        String strV = definition.serialize(map);
        assertEquals(strV, "{\"first\":\"http://foo.com\",\"second\":\"http://gg.gov\"}");
        Map<String, URI> result = (Map<String, URI>) definition.parse(strV);
        assertTrue(result.equals(map));
    }
    /**
     * test handling "required" which can be in the validator list or as a separate boolean.
     */
    @Test
    public void testRemoveRequire() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Boolean.class, PropertyRequired.advance);
        assertTrue(definition.isRequired());
        assertTrue(definition.getValidators().contains("required"));
        definition.setRequired(false);
        assertNull(definition.getValidators());
        definition.setValidators("peanuts");
        definition.setRequired(false);
        assertEquals(definition.getValidators(), "peanuts");
        definition.setRequired(true);
        assertEquals(definition.getValidators(), "peanuts,required");
        definition.setRequired(false);
        assertEquals(definition.getValidators(), "peanuts");
        definition.setValidators("required,peanuts");
        assertTrue(definition.isRequired());
        assertTrue(definition.getValidators().contains("required"));
        definition.setRequired(false);
        assertEquals(definition.getValidators(), "peanuts");
        definition.setValidators("peas,required,peanuts");
        assertTrue(definition.isRequired());
        assertTrue(definition.getValidators().contains("required"));
        definition.setRequired(false);
        assertEquals(definition.getValidators(), "peas,peanuts");
    }
    @DataProvider(name = "serializationData")
    protected Object[][] getDataForSerialization() {
        return new Object[][]{
                data("hello"),
                data("hello <b>there</b>"),
                data("he//llo <b>th\\ere</b>")
        };
    }

    public Object[] data(Object...data) {
        return data;
    }

}
