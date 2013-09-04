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

import static org.amplafi.flow.flowproperty.PropertyScope.activityLocal;
import static org.amplafi.flow.flowproperty.PropertyScope.flowLocal;
import static org.amplafi.flow.flowproperty.PropertyScope.global;
import static org.amplafi.flow.flowproperty.PropertyUsage.consume;
import static org.amplafi.flow.flowproperty.PropertyUsage.initialize;
import static org.amplafi.flow.flowproperty.PropertyUsage.internalState;
import static org.amplafi.flow.flowproperty.PropertyUsage.io;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowConfigurationException;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowTestingUtils;
import org.amplafi.flow.FlowUtils;
import org.amplafi.flow.FlowValueMapKey;
import org.amplafi.flow.FlowValuesMap;
import org.amplafi.flow.impl.FactoryFlowPropertyDefinitionProvider;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowImpl;
import org.amplafi.flow.impl.FlowStateImpl;
import org.amplafi.flow.impl.FlowStateImplementor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests {@link FlowPropertyDefinitionBuilder} and resulting {@link FlowPropertyDefinitionImplementor}.
 */
public class TestFlowPropertyDefinition {

    private static final String FLOW_TYPE = "ftype1";
    private static final boolean TEST_ENABLED = true;

    @Test(enabled=TEST_ENABLED)
    public void testUriProperty() {
        FlowPropertyDefinitionImplementor definition = new FlowPropertyDefinitionBuilder("uri", URI.class).initPropertyRequired(FlowActivityPhase.advance).toFlowPropertyDefinition();
        new FlowTestingUtils().resolveAndInit(definition);
        assertNull(definition.getDefaultObject(new Dummy()));
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
        FlowPropertyProvider flowPropertyProvider = null;

        FlowPropertyDefinitionImplementor definition = new FlowPropertyDefinitionBuilder("foo").toFlowPropertyDefinition();
        new FlowTestingUtils().resolveAndInit(definition);
        assertFalse(definition.isAutoCreate());
        definition = new FlowPropertyDefinitionBuilder(definition).initDefaultObject(Boolean.TRUE).toFlowPropertyDefinition();
        assertEquals(definition.getDataClass(), Boolean.class, "should have determined the property type from the defaultObject");

        new FlowTestingUtils().resolveAndInit(definition);
        Boolean t = (Boolean) definition.deserialize(flowPropertyProvider, null);
        assertNull(t);
        t = (Boolean) definition.getDefaultObject(new Dummy());
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());

        // check behavior if everything is not defined in the Ctor call
        // (i.e. like the definition is being defined in the hivemind.xml)
        // make sure lack of class defined in the FlowPropertyDefinitionBuilder does not mess up setting the
        definition = new FlowPropertyDefinitionBuilder("foo").initDefaultObject("true").setDataClass(Boolean.class).toFlowPropertyDefinition();
        new FlowTestingUtils().resolveAndInit(definition);
        t = (Boolean) definition.deserialize(flowPropertyProvider, null);
        assertNull(t);
        t = (Boolean) definition.getDefaultObject(new Dummy());
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());

        final FlowPropertyDefinitionImplementor definition1 = new FlowPropertyDefinitionBuilder("foo").toFlowPropertyDefinition();
        definition1.setFlowPropertyValueProvider(new FlowPropertyValueProvider<FlowPropertyProvider>() {
            @Override
            @SuppressWarnings({ "unchecked" })
            public <T> T get(FlowPropertyProvider flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
                assertSame(flowPropertyDefinition, definition1);
                return (T) Boolean.TRUE;
            }

            @Override
            public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
                return FlowPropertyProvider.class;
            }
        	@Override
        	@Deprecated // provide better definition
        	public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
        		// not checking so sure.
        		return true;
        	}
        });
        t = (Boolean) definition.getDefaultObject(new Dummy());
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());
    }

    private void assertDefaultObject(Class<?> clazz, Object value) {
        FlowPropertyDefinitionImplementor definition = new FlowPropertyDefinitionBuilder("u", clazz).initPropertyRequired(FlowActivityPhase.advance).toFlowPropertyDefinition();
        new FlowTestingUtils().resolveAndInit(definition);
        assertEquals(definition.getDefaultObject(new Dummy()), value);
    }

    /**
     * Check merging with no collection types
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyDefinitionSimpleMerging() {
        FlowPropertyDefinitionImplementor definition = new FlowPropertyDefinitionBuilder("foo", Boolean.class).toFlowPropertyDefinition();
        FlowPropertyDefinitionImplementor definition1 = new FlowPropertyDefinitionBuilder("foo", Boolean.class).initDefaultObject(true).toFlowPropertyDefinition();
        assertTrue( definition.isMergeable(definition1));
        assertTrue( definition1.isMergeable(definition));
        definition.merge(definition1);
        new FlowTestingUtils().resolveAndInit(definition);
        assertTrue((Boolean)definition.getDefaultObject(new Dummy()));
        assertEquals(definition.getDataClass(), definition1.getDataClass());
    }

    /**
     * Try to merge definitions with a collection of unknown types.
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyDefinitionComplexMergingBecauseOfDataClass() {
        FlowPropertyDefinitionImplementor definition = new FlowPropertyDefinitionBuilder("foo").set(Boolean.class).toFlowPropertyDefinition();
        FlowPropertyDefinitionImplementor definition1 = new FlowPropertyDefinitionBuilder("foo", Boolean.class).initDefaultObject(true).toFlowPropertyDefinition();
        assertFalse( definition.isMergeable(definition1));
        assertFalse( definition1.isMergeable(definition));

        // definition with unknown element in a set should be able to merge with a set that has a defined element type.
        FlowPropertyDefinitionImplementor definition2 = new FlowPropertyDefinitionBuilder("foo").set(null).toFlowPropertyDefinition();
        assertTrue( definition.isMergeable(definition2));

        // merge check will be false because List and Boolean are not assignable between each other.
        FlowPropertyDefinitionImplementor definition3 = new FlowPropertyDefinitionBuilder("foo", Long.class, Set.class, List.class).toFlowPropertyDefinition();
        assertFalse( definition.isMergeable(definition3));
        assertTrue(definition2.isMergeable(definition3));
        assertTrue(definition3.isMergeable(definition2));

        FlowPropertyDefinitionImplementor definition4 = new FlowPropertyDefinitionBuilder("foo").map(Boolean.class).toFlowPropertyDefinition();
        assertFalse(definition4.isMergeable(definition));
        assertFalse(definition4.isMergeable(definition3));
        FlowPropertyDefinitionImplementor definition5 = new FlowPropertyDefinitionBuilder("foo", Boolean.class, Map.class, Set.class).toFlowPropertyDefinition();
        FlowPropertyDefinitionImplementor definition6 = new FlowPropertyDefinitionBuilder("foo", null, Map.class, Set.class).toFlowPropertyDefinition();
        assertTrue(definition5.isMergeable(definition6));
        assertTrue(definition6.isMergeable(definition5));
    }

    /**
     * Make sure FlowPropertyDefinitionImplementor cloning works.
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyDefinitionCloning() {
        FlowPropertyDefinitionImplementor original = new FlowPropertyDefinitionBuilder("foo", Boolean.class, Set.class, List.class).initPropertyRequired(FlowActivityPhase.advance)
                .initAccess(PropertyScope.activityLocal, PropertyUsage.consume, ExternalPropertyAccessRestriction.noAccess).toFlowPropertyDefinition();
        FlowPropertyDefinitionImplementor cloned = new FlowPropertyDefinitionBuilder(original).toFlowPropertyDefinition();
        assertEquals(original, cloned, "cloning failed");
    }

    /**
     * Test some values to make sure that the serialization /parse operations end up with the original result.
     * @param original
     */
    @Test(enabled=TEST_ENABLED, dataProvider="serializationData")
    public void testSerializeAndParse(String original) {
        FlowPropertyDefinitionImplementor def = new FlowPropertyDefinitionBuilder("test").toFlowPropertyDefinition();
        FlowPropertyProvider flowPropertyProvider = null;

        String result = def.deserialize(flowPropertyProvider, def.serialize(original));
        assertEquals(result, original);
    }

    private static final String URI = "uri";

    /**
     * Tests to make sure that {@link FlowPropertyDefinition} can handle serializing/deserializing lists.
     * @throws Exception
     */
    @Test(enabled=TEST_ENABLED)
    @SuppressWarnings("unchecked")
    public void testListSerializing() throws Exception {
        FlowPropertyProvider flowPropertyProvider = null;

        FlowPropertyDefinitionImplementor definition = new FlowPropertyDefinitionBuilder(URI).list(URI.class).initPropertyRequired(FlowActivityPhase.advance).toFlowPropertyDefinition();
        new FlowTestingUtils().resolveAndInit(definition);
        List<URI> list = Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov"));
        String strV = definition.serialize(list);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");
        List<URI> result = (List<URI>) definition.deserialize(flowPropertyProvider, strV);
        assertTrue(list.containsAll(result));
        assertTrue(result.containsAll(list));
    }
    /**
     * test serializing {@link Set}  handling
     * @throws Exception
     */
    @Test(enabled=TEST_ENABLED)
    @SuppressWarnings("unchecked")
    public void testSetSerializing() throws Exception {
        FlowPropertyProvider flowPropertyProvider = null;
        FlowPropertyDefinitionImplementor definition = new FlowPropertyDefinitionBuilder(URI).set(URI.class).initPropertyRequired(FlowActivityPhase.advance).toFlowPropertyDefinition();
        new FlowTestingUtils().resolveAndInit(definition);
        Set<URI> set = new LinkedHashSet<URI>(Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov")));
        String strV = definition.serialize(set);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");
        Set<URI> result = (Set<URI>) definition.deserialize(flowPropertyProvider, strV);
        assertTrue(set.containsAll(result));
        assertTrue(set.containsAll(set));
    }

    @Test(enabled=TEST_ENABLED)
    @SuppressWarnings("unchecked")
    public void testMapSerializing() throws Exception {
        FlowPropertyProvider flowPropertyProvider = null;
        FlowPropertyDefinitionImplementor definition = new FlowPropertyDefinitionBuilder(URI).map(URI.class).initPropertyRequired(FlowActivityPhase.advance).toFlowPropertyDefinition();
        new FlowTestingUtils().resolveAndInit(definition);
        Map<String, URI> map = new LinkedHashMap<String, URI>();
        map.put("first", new URI("http://foo.com"));
        map.put("second", new URI("http://gg.gov"));
        String strV = definition.serialize(map);
        assertEquals(strV, "{\"first\":\"http://foo.com\",\"second\":\"http://gg.gov\"}");
        Map<String, URI> result = (Map<String, URI>) definition.deserialize(flowPropertyProvider, strV);
        assertTrue(result.equals(map));
    }
    /**
     * test all combinations of PropertyUsage and PropertyScope.
     * Only the correct combinations will result in external values initializing the flow
     * Test to make sure only exposed values will be copied back to the global namespace.
     */
    @Test(enabled=TEST_ENABLED, dataProvider="getScopeAndUsageCombinations")
    public void testFlowPropertyInitialization(PropertyScope propertyScope, PropertyUsage propertyUsage) {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String activityName = "activity";
        String flowTypeName = "myflow";
        Map<String, String> initialFlowState= new HashMap<String, String>();
        {
            FlowActivityImpl flowActivity = newFlowActivity();
            flowActivity.setFlowPropertyProviderName(activityName);
            String name= propertyScope+"_"+propertyUsage;
            String externalInitial = "ext_"+name;
            FlowPropertyDefinitionImplementor flowPropertyDefinition = new FlowPropertyDefinitionBuilder(name).initAccess(propertyScope, propertyUsage).toFlowPropertyDefinition();
            flowActivity.addPropertyDefinitions(flowPropertyDefinition);
            initialFlowState.put(name, externalInitial);
            flowTestingUtils.addFlowDefinition(flowTypeName, flowActivity);
        }

        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowState flowState = flowManagement.startFlowState(flowTypeName, true, initialFlowState);

        // now make sure that only the properties allowed to be set externally are set.
        {
            String name= propertyScope+"_"+propertyUsage;
            String externalInitial = "ext_"+name;
            String actual = flowState.getProperty(name);
            if ( !propertyScope.isCacheOnly() && propertyUsage.isExternallySettable()) {
                assertEquals(actual, externalInitial, "PropertyUsage="+propertyUsage+" flowState="+flowState);
            } else {
                // HACK: This needs to be re-enabled
//                    assertNull(actual, "PropertyUsage="+propertyUsage+" flowState="+flowState);
            }
            String changed = "chg_"+name;
            flowState.setProperty(name, changed);
        }
        flowState.finishFlow();
        FlowValuesMap<FlowValueMapKey, CharSequence> finalMap = flowState.getExportedValuesMap();
        // now make sure that only the properties allowed to be set externally are set.
        {
            String name= propertyScope+"_"+propertyUsage;
            String externalInitial = "ext_"+name;
            String changed = "chg_"+name;
            CharSequence actual = finalMap.get(name);
            if ( !propertyScope.isCacheOnly() && propertyUsage.isOutputedProperty() || propertyScope == global) {
                assertEquals(actual, changed, "name="+name+" PropertyUsage="+propertyUsage+" finalMap="+finalMap);
            } else if ( /*!propertyScope.isCacheOnly() && */propertyUsage.isCleanOnInitialization()) {
                assertNull(actual, "name="+name+" PropertyUsage="+propertyUsage+" finalMap="+finalMap);
            } else {
                assertEquals(actual, externalInitial, "name="+name+"PropertyScope="+propertyScope+" PropertyUsage="+propertyUsage+" finalMap="+finalMap);
            }
        }
    }

    @DataProvider(name="getScopeAndUsageCombinations")
    public Object[][] getScopeAndUsageCombinations() {
        Object[][] testData = new Object[PropertyScope.values().length*PropertyUsage.values().length][];
        int i = 0;
        for (PropertyScope propertyScope: PropertyScope.values()) {
            for(PropertyUsage propertyUsage: PropertyUsage.values()) {
                testData[i++] = new Object[] { propertyScope, propertyUsage };
            }
        }
        return testData;
    }

    /**
     * test to make sure that undeclared properties (which is a sign of an error or they represent internal state)
     * don't leak out.
     */
    @Test(enabled=TEST_ENABLED)
    public void testExportingUndeclaredProperties() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        FlowActivityImpl flowActivity =newFlowActivity();
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity);
        Map<String, String> initialFlowState = null;
        FlowState flowState = flowTestingUtils.getFlowManagement().startFlowState(flowTypeName, false, initialFlowState);
        FlowValuesMap exportedMap = flowState.getExportedValuesMap();
        assertFalse(exportedMap.containsKey("not-a-property"), exportedMap+" should contain ");
        // undeclared properties are not exported.
        flowState.setProperty("not-a-property", true);
    }
    /**
     * explicit test to make sure that only properties that should be exported are exported.
     * @param propertyUsage
     * @param propertyScope
     */
    @Test(enabled=TEST_ENABLED, dataProvider="exportingPropertiesData")
    public void testExportingProperties(PropertyUsage propertyUsage, PropertyScope propertyScope) {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String key = "testProp";
        FlowPropertyDefinitionImplementor flowPropertyDefinition = new FlowPropertyDefinitionBuilder(key, boolean.class).initAccess(propertyScope, propertyUsage).toFlowPropertyDefinition();
        FlowActivityImpl flowActivity =newFlowActivity();
        flowActivity.addPropertyDefinitions(flowPropertyDefinition);
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity);
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(
            "outside-property", "out-1",
            key, "false");
        FlowState flowState = flowTestingUtils.getFlowManagement().startFlowState(flowTypeName, false, initialFlowState);
        flowState.setProperty(key, true);
        flowState.finishFlow();
        FlowValuesMap exportedMap = flowState.getExportedValuesMap();
        if ( flowPropertyDefinition.isCopyBackOnFlowSuccess()) {
            assertEquals(exportedMap.get(key), "true");
            assertEquals(exportedMap.size(), 2, "wrong size "+exportedMap);
        } else if ( propertyUsage == consume) {
            assertNull(exportedMap.get(key), "exportedMap="+exportedMap+" key="+key+ " flowState="+flowState);
            assertEquals(exportedMap.size(), 1, "wrong size "+exportedMap);
        } else {
            assertEquals(exportedMap.get(key), "false", "exportedMap="+exportedMap+" key="+key+ " flowState="+flowState);
            assertEquals(exportedMap.size(), 2, "wrong size "+exportedMap);
        }
    }
    @DataProvider(name="exportingPropertiesData")
    protected Object[][] getExportingPropertiesData() {
        List<Object[]> testCases = new ArrayList<Object[]>();

        PropertyScope[] values = PropertyScope.values();
        for (PropertyScope propertyScope : values) {
            Set<PropertyUsage> allowPropertyUsages = propertyScope.getAllowPropertyUsages();
            for(PropertyUsage propertyUsage: allowPropertyUsages) {
                testCases.add( new Object[] { propertyUsage, propertyScope });
            }
        }
        return testCases.toArray(new Object[testCases.size()][]);
    }
    /**
     * Test to make sure the order of the namespaces returned by {@link FlowPropertyDefinitionImplementor#getNamespaceKeySearchList(FlowState, FlowPropertyProvider, boolean)}
     * is in the most specific to least specific order.
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyDefinitionNamespace() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String activityName = "activity";
        String namespace;
        String flowTypeName = "myflow";
        FlowPropertyDefinitionImplementor flowLocalProperty = new FlowPropertyDefinitionBuilder("foo", Boolean.class).initPropertyScope(flowLocal).toFlowPropertyDefinition();
        FlowPropertyDefinitionImplementor activityLocalProperty = new FlowPropertyDefinitionBuilder("foo", Boolean.class).initPropertyScope(activityLocal).toFlowPropertyDefinition();
        List<String> namespaces;
        {
            // static test using FlowActivity
            FlowActivityImpl flowActivity = newFlowActivity();
            flowActivity.setFlowPropertyProviderName(activityName);

            flowTestingUtils.addFlowDefinition(flowTypeName, flowActivity);
            namespace = flowLocalProperty.getNamespaceKey(null, flowActivity);
            assertEquals(namespace, flowTypeName);
            namespace = activityLocalProperty.getNamespaceKey(null, flowActivity);
            assertEquals(namespace, flowTypeName+"."+activityName);
            namespaces = flowLocalProperty.getNamespaceKeySearchList(null, flowActivity, false);
            assertEquals(namespaces.get(0), flowTypeName, "namespaces="+namespaces);
            assertEquals(namespaces.get(1), flowTypeName, "namespaces="+namespaces);
            assertEquals(namespaces.get(2), null, "namespaces="+namespaces);
            assertEquals(namespaces.size(), 3, "namespaces="+namespaces);
            namespaces = activityLocalProperty.getNamespaceKeySearchList(null, flowActivity, false);
            int i = 0;
            assertEquals(namespaces.get(i++), flowActivity.getFullActivityInstanceNamespace(), "namespaces="+namespaces);
            assertEquals(namespaces.get(i++), flowActivity.getFlowPropertyProviderFullName(), "namespaces="+namespaces);
            assertEquals(namespaces.get(i++), flowActivity.getFlowPropertyProviderName(), "namespaces="+namespaces);
            assertEquals(namespaces.get(i++), flowTypeName, "namespaces="+namespaces);
            assertEquals(namespaces.get(i++), null, "namespaces="+namespaces);
            assertEquals(namespaces.size(), i, "namespaces="+namespaces);
        }

        {
            // static test for FlowImpl
            Flow flow = flowTestingUtils.getFlowDefinitionsManager().getFlowDefinition(flowTypeName);
            namespace = flowLocalProperty.getNamespaceKey(null, flow);
            assertEquals(namespace, flowTypeName);
            namespaces = flowLocalProperty.getNamespaceKeySearchList(null, flow, false);
            assertEquals(namespaces.get(0), flowTypeName, "namespaces="+namespaces);
            assertEquals(namespaces.get(1), flowTypeName, "namespaces="+namespaces);
            assertEquals(namespaces.get(2), null, "namespaces="+namespaces);
            assertEquals(namespaces.size(), 3, "namespaces="+namespaces);

            try {
                namespaces = activityLocalProperty.getNamespaceKeySearchList(null, flow, false);
                fail("should throw exception");
            } catch (IllegalStateException e) {
                // expected
            }
            try {
                namespace = activityLocalProperty.getNamespaceKey(null, flow);
                fail("should throw exception");
            } catch (IllegalStateException e) {
                // expected
            }
        }

        // now as part of a running flow
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowStateImpl flowStateImpl = flowManagement.startFlowState(flowTypeName, true, null);
        FlowActivityImpl flowActivity = flowStateImpl.getCurrentActivity();
        for(FlowPropertyProvider flowPropertyProvider: new FlowPropertyProvider[] {flowActivity, flowStateImpl.getFlow()}) {
            namespace = flowLocalProperty.getNamespaceKey(flowStateImpl, flowPropertyProvider);
            assertEquals(namespace, flowStateImpl.getLookupKey());

            namespaces = flowLocalProperty.getNamespaceKeySearchList(flowStateImpl, flowPropertyProvider, false);
            assertEquals(namespaces.get(0), namespace, "namespaces="+namespaces);
            assertEquals(namespaces.get(1), flowTypeName, "namespaces="+namespaces);
            assertEquals(namespaces.get(2), null, "namespaces="+namespaces);
            assertEquals(namespaces.size(), 3, "namespaces="+namespaces);
        }
        namespace = activityLocalProperty.getNamespaceKey(flowStateImpl, flowActivity);
        assertTrue(namespace.contains(flowStateImpl.getLookupKey()), "namespace="+namespace+" fsLookupKey="+flowStateImpl.getLookupKey());
        assertTrue(namespace.contains(activityName), "namespace="+namespace);
        namespaces = activityLocalProperty.getNamespaceKeySearchList(flowStateImpl, flowActivity, false);
        int i = 0;
        assertEquals(namespaces.get(i++), namespace, "namespaces="+namespaces);
        assertEquals(namespaces.get(i++), flowActivity.getFlowPropertyProviderFullName(), "namespaces="+namespaces);
        assertEquals(namespaces.get(i++), activityName, "namespaces="+namespaces);
        assertEquals(namespaces.get(i++), flowTypeName, "namespaces="+namespaces);
        assertEquals(namespaces.get(i++), null, "namespaces="+namespaces);
        assertEquals(namespaces.size(), i, "namespaces="+namespaces);
    }

    /**
     * There are tests around FlowTransitions in {@link TestFlowTransitions#testAvoidConflictsOnFlowTransitions()}
     *
     * Demonstrates problem with {@link #initialize} and a null initialization value. This allows the definition in flowActivity2 to decide to initialize from the
     * external environment.
     *
     * This also tests to make sure the internalState property for the second flowActivity is respected.
     * TODO merging of properties so that the initialize is the surviving PropertyUsage in the merged property.
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyUsageWithNullInitialization() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String propertyName = "propertyName";

        FlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider = new FlowPropertyValueProvider<FlowPropertyProvider>() {

            @SuppressWarnings("unchecked")
            @Override
            public <T> T get(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
                return (T) Boolean.TRUE;
            }

            @Override
            public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
                return FlowPropertyProvider.class;
            }
        	@Override
        	@Deprecated // provide better definition
        	public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
        		// not checking so sure.
        		return true;
        	}
        };
        FlowPropertyDefinitionImplementor flowLocalProperty = new FlowPropertyDefinitionBuilder(propertyName, Boolean.class).initFlowPropertyValueProvider(flowPropertyValueProvider).initAccess(PropertyScope.flowLocal,
            PropertyUsage.initialize).toFlowPropertyDefinition();
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);

        FlowActivityImpl flowActivity1 = newFlowActivity();
        flowActivity1.setFlowPropertyProviderName("activity1");
        FlowPropertyDefinitionImplementor activityLocalProperty = new FlowPropertyDefinitionBuilder(propertyName, Map.class).initAccess(activityLocal,internalState).toFlowPropertyDefinition();
        flowActivity1.addPropertyDefinitions(activityLocalProperty);

        FlowActivityImpl flowActivity2 = newFlowActivity();
        flowActivity2.setFlowPropertyProviderName("activity2");
        FlowPropertyDefinitionImplementor globalProperty = new FlowPropertyDefinitionBuilder(propertyName, Boolean.class).initAccess(flowLocal,io).toFlowPropertyDefinition();
        flowActivity2.addPropertyDefinitions(globalProperty);

        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0, flowActivity1, flowActivity2);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(propertyName, "true");
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, initialFlowState);
        assertNotNull(flowState);
        // expect null because propertyName is set to initialize
        assertFalse(flowState.isPropertySet(propertyName), flowState.toString());
        Boolean propertyValue = flowState.getProperty(propertyName, Boolean.class);
        assertEquals(propertyValue, Boolean.TRUE, "flowState="+flowState+" propertyName="+propertyName+" propertyValue="+propertyValue);
        flowState.next();
        Map mapPropertyValue = flowState.getProperty(propertyName, Map.class);
        assertNull(mapPropertyValue, "flowState="+flowState+" propertyValue="+mapPropertyValue);
        flowState.setProperty(propertyName, new HashMap());
        flowState.next();
        propertyValue = flowState.getProperty(propertyName, Boolean.class);
        assertNotNull(propertyValue, "flowState="+flowState+" propertyValue="+propertyValue);
        assertTrue(propertyValue.booleanValue(), "flowState="+flowState+" propertyValue="+propertyValue);
    }

    /**
     * Test to make sure that flowPropertyValueProviders are correctly hooked up.
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyUsageCreatesAndInitializes() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        FlowActivityImpl flowActivity0 = newFlowActivity();
        final String notAutoCreated = "notAutocreated";
        final String autoCreated = "autocreated";
        FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider =  new AbstractFlowPropertyValueProvider<FlowPropertyProvider>() {

            @Override
            public <T> T get(FlowPropertyProvider flowPropertyProvider,
                    FlowPropertyDefinition flowPropertyDefinition) {
            	check(flowPropertyDefinition);
            	if(flowPropertyDefinition.isNamed(autoCreated)) {
            	} else {
            	    throw new FlowConfigurationException("but test should only get the "+autoCreated+" property");
            	}
                CharSequence value = "ME: "+flowPropertyDefinition.getName();
                return (T) value;
            }

            @Override
            public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
                return FlowPropertyProvider.class;
            }
        	@Override
        	public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
        		return flowPropertyExpectation.isNamed(autoCreated) || flowPropertyExpectation.isNamed(notAutoCreated);
        	}
        };
        FlowPropertyDefinitionImplementor flowNotAutoCreatedProperty = new FlowPropertyDefinitionBuilder(notAutoCreated, Object.class).initAccess(flowLocal,io).
        initFlowPropertyValueProvider(flowPropertyValueProvider).toFlowPropertyDefinition();
        FlowPropertyDefinitionImplementor flowLocalProperty = new FlowPropertyDefinitionBuilder(autoCreated, Object.class).returned()
            .initFlowPropertyValueProvider(flowPropertyValueProvider).toFlowPropertyDefinition();
        flowActivity0.addPropertyDefinitions(flowNotAutoCreatedProperty, flowLocalProperty );
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, null);
        assertNotNull(flowState);
        assertFalse(flowState.isPropertySet(autoCreated));
        assertFalse(flowState.isPropertySet(notAutoCreated));
        flowState.finishFlow();
        assertTrue(flowState.isPropertySet(autoCreated));
        assertFalse(flowState.isPropertySet(notAutoCreated));
        assertEquals(flowState.getProperty(autoCreated), "ME: "+autoCreated);
    }

    /**
     * There are tests around FlowTransitions in {@link TestFlowTransitions#testAvoidConflictsOnFlowTransitions()}
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyUsage() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String propertyName = "propertyName";

        FlowPropertyDefinitionImplementor flowLocalProperty = new FlowPropertyDefinitionBuilder(propertyName, String.class).initAccess(flowLocal,initialize).setInitial("true").toFlowPropertyDefinition();
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);

        FlowActivityImpl flowActivity1 = newFlowActivity();
        flowActivity1.setFlowPropertyProviderName("activity1");
        FlowPropertyDefinitionImplementor activityLocalProperty = new FlowPropertyDefinitionBuilder(propertyName, String.class).initAccess(activityLocal,internalState).toFlowPropertyDefinition();
        flowActivity1.addPropertyDefinitions(activityLocalProperty);

        FlowActivityImpl flowActivity2 = newFlowActivity();
        flowActivity2.setFlowPropertyProviderName("activity2");
        FlowPropertyDefinitionImplementor globalProperty = new FlowPropertyDefinitionBuilder(propertyName, String.class).initAccess(flowLocal,io).toFlowPropertyDefinition();
        flowActivity2.addPropertyDefinitions(globalProperty);

        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0, flowActivity1, flowActivity2);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(propertyName, "maybe");
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, initialFlowState);
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
     * @return
     */
    private FlowActivityImpl newFlowActivity() {
        return new FlowActivityImpl().initInvisible(false);
    }

    /**
     * Test to see that serialization/deserialization of enum's
     *
     * Also tests that {@link FlowPropertyDefinitionBuilder#setInitial(String)} works.
     */
    @Test(enabled=TEST_ENABLED)
    public void testEnumHandling() {
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState("foo", SampleEnum.EXTERNAL);
        FlowImplementor flow = new FlowImpl(FLOW_TYPE);
        FlowPropertyDefinitionImplementor definition = new FlowPropertyDefinitionBuilder("foo", SampleEnum.class).toFlowPropertyDefinition();
        flow.addPropertyDefinitions(definition);
        FlowActivityImpl fa1 = new FlowActivityImpl().initInvisible(false);
        definition = new FlowPropertyDefinitionBuilder("fa1fp", SampleEnum.class).setInitial(SampleEnum.EMAIL.name()).toFlowPropertyDefinition();
        fa1.addPropertyDefinitions(definition);
        flow.addActivity(fa1);
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
        flowTestingUtils.getFlowDefinitionsManager().addDefinition(flow);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowState flowState = flowManagement.startFlowState(FLOW_TYPE, true, initialFlowState);
        SampleEnum type =flowState.getCurrentActivity().getProperty("foo");
        assertEquals(type, SampleEnum.EXTERNAL, "(looking for property 'foo') FlowState="+flowState);
        type =flowState.getProperty("fa1fp", SampleEnum.class);
        assertEquals(type, SampleEnum.EMAIL);
    }

    private static enum SampleEnum {
        EXTERNAL, EMAIL

    }

    /**
     * Ensure that FlowPropertyValueProviders are only set on properties that they handle and only on properties that have no existing
     * {@link FlowPropertyValueProvider}
     */
    @Test(enabled=TEST_ENABLED)
    public void testDefaultProviderInitialization() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        final FlowPropertyDefinitionBuilder noProvider = new FlowPropertyDefinitionBuilder("noProvider", PropertyUsage.class);
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = new FlowPropertyDefinitionBuilder("hasdefault", Boolean.class).initDefaultObject(Boolean.TRUE);
        FixedFlowPropertyValueProvider originalProvider = (FixedFlowPropertyValueProvider) flowPropertyDefinitionBuilder.getFlowPropertyValueProvider();
        AbstractFlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider = new AbstractFlowPropertyValueProvider<FlowPropertyProvider>() {

        	@Override
        	public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        		super.defineFlowPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters);
				super.addPropertyDefinition(flowPropertyProvider, noProvider, additionalConfigurationParameters);
        	}
            @Override
            public <T> T get(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
                throw new UnsupportedOperationException("should not be called.");
            }
        };
        flowPropertyValueProvider.addFlowPropertyDefinitionImplementators(flowPropertyDefinitionBuilder);
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
        flowPropertyValueProvider.defineFlowPropertyDefinitions(flowActivity0);
        flowActivity0.addPropertyDefinitions(flowPropertyDefinitionBuilder);
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, null);
        assertEquals(flowState.getProperty("hasdefault"), Boolean.TRUE);
        FlowPropertyDefinition actualFixed = flowState.getFlowPropertyDefinition("hasdefault");
        assertTrue(actualFixed.getFlowPropertyValueProvider() instanceof FixedFlowPropertyValueProvider);
        assertSame(actualFixed.getFlowPropertyValueProvider(), originalProvider);

        FlowPropertyDefinition actualNoProvider = flowState.getFlowPropertyDefinition("noProvider");
        assertNull(actualNoProvider.getFlowPropertyValueProvider());
        assertEquals(actualNoProvider.getDataClass(), PropertyUsage.class);
    }

    /**
     * Make sure that a {@link FlowPropertyValueProvider} that cannot handle a FlowPropertyDefinition is not set as its default
     * Make sure the exception is thrown.
     */
    @Test(enabled=TEST_ENABLED, expectedExceptions= {FlowConfigurationException.class})
    public void testPreventAddingWrongFlowPropertyValueProvider() {
        FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider =  new AbstractFlowPropertyValueProvider<FlowPropertyProvider>() {

            @Override
            public <T> T get(FlowPropertyProvider flowPropertyProvider,  FlowPropertyDefinition flowPropertyDefinition) {
            	throw fail(flowPropertyDefinition);
            }

            @Override
            public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
                return FlowPropertyProvider.class;
            }
        	@Override
        	public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
        		return false;
        	}
        };
        new FlowPropertyDefinitionBuilder("foo", Object.class).initFlowPropertyValueProvider(flowPropertyValueProvider);
    }

    /**
     * Create a standard property and see if that standard property is access and used when the property comes up unexpectedly.
     */
    @Test(enabled=TEST_ENABLED)
    public void testStandardFlowPropertyExtensions() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        FactoryFlowPropertyDefinitionProvider factoryFlowPropertyDefinitionProvider = new FactoryFlowPropertyDefinitionProvider();
        factoryFlowPropertyDefinitionProvider.addStandardPropertyDefinition("user", UserObject.class);
        flowTestingUtils.getFlowDefinitionsManager().addFactoryFlowPropertyDefinitionProvider(factoryFlowPropertyDefinitionProvider);
        String propertyName = "propertyName";

        FlowPropertyDefinitionImplementor flowLocalProperty = new FlowPropertyDefinitionBuilder(propertyName, Boolean.class).initAccess(flowLocal,initialize)
            .initFlowPropertyValueProvider(new FlowPropertyValueProvider<FlowPropertyProviderWithValues>() {

            @SuppressWarnings("unchecked")
            @Override
            public <T> T get(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
                // return a non-String value to make sure initialization does not expect a string.
            	UserObject userObject = flowPropertyProvider.getProperty("user");
            	assertNotNull(userObject);
            	assertEquals(userObject.i, 1);
                return (T) Boolean.TRUE;
            }

            @Override
            public Class<FlowPropertyProviderWithValues> getFlowPropertyProviderClass() {
                return FlowPropertyProviderWithValues.class;
            }
        	@Override
        	@Deprecated // provide better definition
        	public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
        		// not checking so sure.
        		return true;
        	}
        }).toFlowPropertyDefinition();
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowStateImplementor flowState = flowManagement.startFlowState(flowTypeName, false, null);
        assertNotNull(flowState);
        flowState.setProperty("user", new UserObject(1));
        Boolean propertyValue = flowState.getProperty(propertyName, Boolean.class);
        assertTrue(propertyValue);
    }
    /**
     * Test to make sure property initialization is forced and that the initialization code does not expect a String.
     */
    @Test(enabled=TEST_ENABLED)
    public void testForcedInitializationWithFlowPropertyValueProvider() {
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        String propertyName = "propertyName";

        FlowPropertyDefinitionImplementor flowLocalProperty = new FlowPropertyDefinitionBuilder(propertyName, Boolean.class).initAccess(flowLocal,initialize)
        .initFlowPropertyValueProvider(new FlowPropertyValueProvider<FlowPropertyProvider>() {

            @SuppressWarnings("unchecked")
            @Override
            public <T> T get(FlowPropertyProvider flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
                // return a non-String value to make sure initialization does not expect a string.
                return (T) Boolean.TRUE;
            }

            @Override
            public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
                return FlowPropertyProvider.class;
            }
        	@Override
        	@Deprecated // provide better definition
        	public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
        		// not checking so sure.
        		return true;
        	}
        }).toFlowPropertyDefinition();
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(propertyName, "maybe");
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, initialFlowState);
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

    private static class Dummy implements FlowPropertyProvider {

        /**
         * @see org.amplafi.flow.flowproperty.FlowPropertyProvider#getFlowPropertyDefinition(java.lang.String)
         */
        @Override
        public <T extends FlowPropertyDefinition> T getFlowPropertyDefinition(String key) {
            throw new UnsupportedOperationException();
        }

        /**
         * @see org.amplafi.flow.flowproperty.FlowPropertyProvider#getFlowPropertyProviderFullName()
         */
        @Override
        public String getFlowPropertyProviderFullName() {
            throw new UnsupportedOperationException();
        }

        /**
         * @see org.amplafi.flow.flowproperty.FlowPropertyProvider#getFlowPropertyProviderName()
         */
        @Override
        public String getFlowPropertyProviderName() {
            throw new UnsupportedOperationException();
        }

        /**
         * @see org.amplafi.flow.flowproperty.FlowPropertyProvider#getPropertyDefinitions()
         */
        @Override
        public Map<String, FlowPropertyDefinition> getPropertyDefinitions() {
            throw new UnsupportedOperationException();
        }

    }
    private static class UserObject {

    	int i;

		public UserObject(int i) {
			this.i = i;
		}

    }

}
