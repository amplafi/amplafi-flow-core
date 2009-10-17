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
package org.amplafi.flow.flowproperty;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.*;

import org.amplafi.flow.flowproperty.DataClassDefinitionImpl;
import org.amplafi.flow.*;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowStateImpl;
import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowTestingUtils;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static org.amplafi.flow.flowproperty.PropertyScope.*;
import static org.amplafi.flow.flowproperty.PropertyUsage.*;

/**
 * Tests {@link FlowPropertyDefinitionImpl}.
 */
public class TestFlowPropertyDefinition {
    private static final boolean TEST_ENABLED = true;
    @Test(enabled=TEST_ENABLED)
    public void testValidateWith_empty() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl();
        definition.validateWith("a", "b");

        assertFalse(definition.isRequired());

        definition.addValidator("required");
        assertTrue(definition.isRequired());
        assertEquals(definition.getValidators(), "flowField=a-b,required");
    }

    @Test(enabled=TEST_ENABLED)
    public void testValidateWith_required() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Boolean.class, PropertyRequired.advance);
        definition.validateWith("pass");

        assertTrue(definition.isRequired());
        assertEquals(definition.getValidators(), "required,flowField=pass");
    }

    @Test(enabled=TEST_ENABLED)
    public void testUriProperty() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("uri", URI.class, PropertyRequired.advance);
        new FlowTestingUtils().resolveAndInit(definition);
        assertNull(definition.getDefaultObject(null));
    }

    @Test(enabled=TEST_ENABLED)
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
    @Test(enabled=TEST_ENABLED)
    public void testDefaultHandlingWithAutoCreate() throws Exception {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo");
        new FlowTestingUtils().resolveAndInit(definition);
        assertFalse(definition.isAutoCreate());
        definition.initDefaultObject(Boolean.TRUE);
        assertEquals(definition.getDataClass(), Boolean.class);

        new FlowTestingUtils().resolveAndInit(definition);
        Boolean t = (Boolean) definition.parse(null);
        assertNull(t);
        t = (Boolean) definition.getDefaultObject(null);
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());

        // check behavior if everything is not defined in the Ctor call
        // (i.e. like the definition is being defined in the hivemind.xml)
        definition = new FlowPropertyDefinitionImpl("foo");
        definition.setDefaultObject("true");
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
            @SuppressWarnings({ "unused", "unchecked" })
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

    @Test(enabled=TEST_ENABLED)
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
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyDefinitionSimpleMerging() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Boolean.class);
        FlowPropertyDefinitionImpl definition1 = new FlowPropertyDefinitionImpl("foo", Boolean.class).initDefaultObject(true);
        assertTrue( definition.isMergeable(definition1));
        assertTrue( definition1.isMergeable(definition));
        definition.merge(definition1);
        new FlowTestingUtils().resolveAndInit(definition);
        assertTrue((Boolean)definition.getDefaultObject(null));
        assertEquals(definition.getDataClass(), definition1.getDataClass());
    }

    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyDefinitionComplexMergingBecauseOfDataClass() {
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

    @Test(enabled=TEST_ENABLED)
    public void testDataClassDefinition() {
        DataClassDefinitionImpl dataClassDefinition =
            new DataClassDefinitionImpl(Boolean.class, Set.class);
        assertEquals(dataClassDefinition.getElementDataClassDefinition().getDataClass(), Boolean.class);
        assertEquals(dataClassDefinition.getDataClass(), Set.class);
    }

    @Test(enabled=TEST_ENABLED)
    public void testDataClassDefinitionCollection() {
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

    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyDefinitionCloning() {
        FlowPropertyDefinitionImpl original = new FlowPropertyDefinitionImpl("foo", Boolean.class, PropertyRequired.advance, Set.class, List.class);
        FlowPropertyDefinitionImpl cloned = new FlowPropertyDefinitionImpl(original);
        assertEquals(original, cloned, "cloning failed");
    }

    /**
     * Test some values to make sure that the serialization /parse operations end up with the original result.
     * @param original
     */
    @Test(enabled=TEST_ENABLED, dataProvider="serializationData")
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
    @Test(enabled=TEST_ENABLED)
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
    @Test(enabled=TEST_ENABLED)
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

    @Test(enabled=TEST_ENABLED)
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
    @Test(enabled=TEST_ENABLED)
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

    /**
     * test all combinations of PropertyUsage and PropertyScope.
     * Only the correct combinations will result in external values initializing the flow
     * Test to make sure only exposed values will be copied back to the global namespace.
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyInitialization() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String activityName = "activity";
        String flowTypeName = "myflow";
        Map<String, String> initialFlowState= new HashMap<String, String>();
        {
            FlowActivityImpl flowActivity = new FlowActivityImpl();
            flowActivity.setActivityName(activityName);
            for (PropertyScope propertyScope: PropertyScope.values()) {
                for(PropertyUsage propertyUsage: PropertyUsage.values()) {
                    String name= propertyScope+"_"+propertyUsage;
                    String externalInitial = "ext_"+name;
                    FlowPropertyDefinition flowPropertyDefinition = new FlowPropertyDefinitionImpl(name).initAccess(propertyScope, propertyUsage);
                    flowActivity.addPropertyDefinitions(flowPropertyDefinition);
                    initialFlowState.put(name, externalInitial);
                }
            }
            flowTestingUtils.addFlowDefinition(flowTypeName, flowActivity);
        }


        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowState flowState = flowManagement.startFlowState(flowTypeName, true, initialFlowState, null);

        // now make sure that only the properties allowed to be set externally are set.
        for (PropertyScope propertyScope: PropertyScope.values()) {
            for(PropertyUsage propertyUsage: PropertyUsage.values()) {
                String name= propertyScope+"_"+propertyUsage;
                String externalInitial = "ext_"+name;
                String actual = flowState.getProperty(name);
                if ( !propertyScope.isCacheOnly() && propertyUsage.isExternallySettable()) {
                    assertEquals(actual, externalInitial, "PropertyUsage="+propertyUsage+" flowState="+flowState);
                } else {
                    assertNull(actual, "PropertyUsage="+propertyUsage+" flowState="+flowState);
                }
                String changed = "chg_"+name;
                flowState.setProperty(name, changed);
            }
        }
        flowState.finishFlow();
        Map<String, String> finalMap = flowState.getExportedValuesMap();
        // now make sure that only the properties allowed to be set externally are set.
        for (PropertyScope propertyScope: PropertyScope.values()) {
            for(PropertyUsage propertyUsage: PropertyUsage.values()) {
                String name= propertyScope+"_"+propertyUsage;
                String externalInitial = "ext_"+name;
                String changed = "chg_"+name;
                String actual = finalMap.get(name);
                if ( !propertyScope.isCacheOnly() && propertyUsage.isCopyBackOnFlowSuccess() || propertyScope == global) {
                    assertEquals(actual, changed, "name="+name+" PropertyUsage="+propertyUsage+" finalMap="+finalMap);
                } else if ( !propertyScope.isCacheOnly() && propertyUsage.isCleanOnInitialization()) {
                    assertNull(actual, "name="+name+" PropertyUsage="+propertyUsage+" finalMap="+finalMap);
                } else {
                    assertEquals(actual, externalInitial, "name="+name+" PropertyUsage="+propertyUsage+" finalMap="+finalMap);
                }
            }
        }

    }

    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyDefinitionNamespace() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String activityName = "activity";
        String namespace;
        String flowTypeName = "myflow";
        FlowPropertyDefinitionImpl flowLocalProperty = new FlowPropertyDefinitionImpl("foo", Boolean.class).initPropertyScope(flowLocal);
        FlowPropertyDefinitionImpl activityLocalProperty = new FlowPropertyDefinitionImpl("foo", Boolean.class).initPropertyScope(activityLocal);
        List<String> namespaces;
        {
            FlowActivityImpl flowActivity = new FlowActivityImpl();
            flowActivity.setActivityName(activityName);

            flowTestingUtils.addFlowDefinition(flowTypeName, flowActivity);
            namespace = flowLocalProperty.getNamespaceKey(null, flowActivity);
            assertEquals(namespace, flowTypeName);
            namespace = activityLocalProperty.getNamespaceKey(null, flowActivity);
            assertEquals(namespace, flowTypeName+"."+activityName);
            namespaces = flowLocalProperty.getNamespaceKeySearchList(null, flowActivity);
            assertEquals(namespaces.get(0), flowTypeName, "namespaces="+namespaces);
            assertEquals(namespaces.get(1), flowTypeName, "namespaces="+namespaces);
            assertEquals(namespaces.get(2), null, "namespaces="+namespaces);
            assertEquals(namespaces.size(), 3, "namespaces="+namespaces);
            namespaces = activityLocalProperty.getNamespaceKeySearchList(null, flowActivity);
            assertEquals(namespaces.get(0), flowActivity.getFullActivityName(), "namespaces="+namespaces);
            assertEquals(namespaces.get(1), flowActivity.getActivityName(), "namespaces="+namespaces);
            assertEquals(namespaces.get(2), flowTypeName, "namespaces="+namespaces);
            assertEquals(namespaces.get(3), null, "namespaces="+namespaces);
            assertEquals(namespaces.size(), 4, "namespaces="+namespaces);
        }

        // now as part of a running flow
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowStateImpl flowStateImpl = flowManagement.startFlowState(flowTypeName, true, null, null);
        FlowActivityImpl flowActivity = flowStateImpl.getCurrentActivity();
        namespace = flowLocalProperty.getNamespaceKey(flowStateImpl, flowActivity);
        assertEquals(namespace, flowStateImpl.getLookupKey());

        namespaces = flowLocalProperty.getNamespaceKeySearchList(flowStateImpl, flowActivity);
        assertEquals(namespaces.get(0), namespace, "namespaces="+namespaces);
        assertEquals(namespaces.get(1), flowTypeName, "namespaces="+namespaces);
        assertEquals(namespaces.get(2), null, "namespaces="+namespaces);
        assertEquals(namespaces.size(), 3, "namespaces="+namespaces);

        namespace = activityLocalProperty.getNamespaceKey(flowStateImpl, flowActivity);
        assertTrue(namespace.contains(flowStateImpl.getLookupKey()), "namespace="+namespace);
        assertTrue(namespace.contains(activityName), "namespace="+namespace);
        namespaces = activityLocalProperty.getNamespaceKeySearchList(flowStateImpl, flowActivity);
        assertEquals(namespaces.get(0), namespace, "namespaces="+namespaces);
        assertEquals(namespaces.get(1), activityName, "namespaces="+namespaces);
        assertEquals(namespaces.get(2), flowTypeName, "namespaces="+namespaces);
        assertEquals(namespaces.get(3), null, "namespaces="+namespaces);
        assertEquals(namespaces.size(), 4, "namespaces="+namespaces);
    }

    /**
     * There are tests around FlowTransitions in {@link TestFlowTransitions#testAvoidConflictsOnFlowTransitions()}
     *
     * Demonstrates problem with {@link #initialize} and a null initialization value. This allows the definition in flowActivity2 to decide to initialize from the
     * external environment.
     *
     * TODO merging of properties so that the initialize is the surviving PropertyUsage in the merged property.
     */
    @Test(enabled=false)
    public void testFlowPropertyUsageWithNullInitialization() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String propertyName = "propertyName";

        FlowPropertyDefinitionImpl flowLocalProperty = new FlowPropertyDefinitionImpl(propertyName, Boolean.class).initAccess(flowLocal,initialize);
        FlowActivityImpl flowActivity0 = new FlowActivityImpl();
        flowActivity0.setActivityName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);

        FlowActivityImpl flowActivity1 = new FlowActivityImpl();
        flowActivity1.setActivityName("activity1");
        FlowPropertyDefinitionImpl activityLocalProperty = new FlowPropertyDefinitionImpl(propertyName, Boolean.class).initAccess(activityLocal,internalState);
        flowActivity1.addPropertyDefinitions(activityLocalProperty);

        FlowActivityImpl flowActivity2 = new FlowActivityImpl();
        flowActivity2.setActivityName("activity2");
        FlowPropertyDefinitionImpl globalProperty = new FlowPropertyDefinitionImpl(propertyName, Boolean.class).initAccess(flowLocal,io);
        flowActivity2.addPropertyDefinitions(globalProperty);

        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0, flowActivity1, flowActivity2);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(propertyName, "true");
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, initialFlowState , null);
        assertNotNull(flowState);
        // expect null because
        Boolean propertyValue = flowState.getProperty(propertyName, Boolean.class);
        assertNull(propertyValue, "flowState="+flowState+" propertyValue="+propertyValue);
        flowState.next();
        propertyValue = flowState.getProperty(propertyName, Boolean.class);
        assertNull(propertyValue, "flowState="+flowState+" propertyValue="+propertyValue);
        flowState.next();
        propertyValue = flowState.getProperty(propertyName, Boolean.class);
        assertNotNull(propertyValue, "flowState="+flowState+" propertyValue="+propertyValue);
        assertTrue(propertyValue.booleanValue(), "flowState="+flowState+" propertyValue="+propertyValue);
    }
    /**
     * There are tests around FlowTransitions in {@link TestFlowTransitions#testAvoidConflictsOnFlowTransitions()}
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyUsage() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String propertyName = "propertyName";

        FlowPropertyDefinitionImpl flowLocalProperty = new FlowPropertyDefinitionImpl(propertyName, String.class).initAccess(flowLocal,initialize).initInitial("true");
        FlowActivityImpl flowActivity0 = new FlowActivityImpl();
        flowActivity0.setActivityName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);

        FlowActivityImpl flowActivity1 = new FlowActivityImpl();
        flowActivity1.setActivityName("activity1");
        FlowPropertyDefinitionImpl activityLocalProperty = new FlowPropertyDefinitionImpl(propertyName, String.class).initAccess(activityLocal,internalState);
        flowActivity1.addPropertyDefinitions(activityLocalProperty);

        FlowActivityImpl flowActivity2 = new FlowActivityImpl();
        flowActivity2.setActivityName("activity2");
        FlowPropertyDefinitionImpl globalProperty = new FlowPropertyDefinitionImpl(propertyName, String.class).initAccess(flowLocal,io);
        flowActivity2.addPropertyDefinitions(globalProperty);

        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0, flowActivity1, flowActivity2);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(propertyName, "maybe");
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, initialFlowState , null);
        assertNotNull(flowState);
        String propertyValue = flowState.getProperty(propertyName, String.class);
        assertEquals("true",propertyValue, "flowState="+flowState+" propertyValue="+propertyValue);
        flowState.next();
        propertyValue = flowState.getProperty(propertyName, String.class);
        assertNull(propertyValue, "flowState="+flowState+" propertyValue="+propertyValue);
        flowState.next();
        propertyValue = flowState.getProperty(propertyName, String.class);
        assertNotNull(propertyValue, "flowState="+flowState+" propertyValue="+propertyValue);
        assertEquals("true", propertyValue, "flowState="+flowState+" propertyValue="+propertyValue);
    }

    /**
     * Test to make sure property initialization is forced and that the initialization code does not expect a String.
     */
    @Test(enabled=TEST_ENABLED)
    public void testForcedInitializationWithFlowPropertyValueProvider() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String propertyName = "propertyName";

        FlowPropertyDefinitionImpl flowLocalProperty = new FlowPropertyDefinitionImpl(propertyName, Boolean.class).initAccess(flowLocal,initialize);
        flowLocalProperty.initFlowPropertyValueProvider(new FlowPropertyValueProvider<FlowActivity>() {

            @Override
            public <T> T get(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
                // return a non-String value to make sure initialization does not expect a string.
                return (T) Boolean.TRUE;
            }
        });
        FlowActivityImpl flowActivity0 = new FlowActivityImpl();
        flowActivity0.setActivityName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(propertyName, "maybe");
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, initialFlowState , null);
        assertNotNull(flowState);
        Boolean propertyValue = flowState.getProperty(propertyName, Boolean.class);
        assertEquals(Boolean.TRUE,propertyValue, "flowState="+flowState+" propertyValue="+propertyValue);
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
