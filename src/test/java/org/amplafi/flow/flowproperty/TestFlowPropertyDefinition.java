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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowActivityPhase;
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
import org.amplafi.flow.TestFlowTransitions;
import org.amplafi.flow.impl.FactoryFlowPropertyDefinitionProvider;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowImpl;
import org.amplafi.flow.impl.FlowStateImpl;
import org.amplafi.flow.impl.FlowStateImplementor;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sworddance.util.ApplicationIllegalArgumentException;

import static org.amplafi.flow.flowproperty.PropertyScope.*;
import static org.amplafi.flow.flowproperty.PropertyUsage.*;
import static org.testng.Assert.*;

/**
 * Tests {@link FlowPropertyDefinitionImpl}.
 */
public class TestFlowPropertyDefinition {

    private static final String FLOW_TYPE = "ftype1";
    private static final boolean TEST_ENABLED = true;
//    @Test(enabled=TEST_ENABLED)
//    public void testValidateWith_empty() {
//        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl();
//        definition.validateWith("a", "b");
//
//        assertFalse(definition.isRequired());
//
//        definition.addValidator("required");
//        assertTrue(definition.isRequired());
//        assertEquals(definition.getValidators(), "flowField=a-b,required");
//    }
//
//    @Test(enabled=TEST_ENABLED)
//    public void testValidateWith_required() {
//        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Boolean.class, FlowActivityPhase.advance);
//        definition.validateWith("pass");
//
//        assertTrue(definition.isRequired());
//        assertEquals(definition.getValidators(), "required,flowField=pass");
//    }

//    @Test(enabled=TEST_ENABLED)
//    public void testValidate_required_and_extra_validator() {
//        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", Boolean.class, FlowActivityPhase.advance);
//        definition.addValidator("email").initPropertyUsage(PropertyUsage.io);
//
//        assertTrue(definition.isRequired());
//        assertEquals(definition.getValidators(), "required,email");
//    }

    @Test(enabled=TEST_ENABLED)
    public void testUriProperty() {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("uri", URI.class, FlowActivityPhase.advance);
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

        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo");
        new FlowTestingUtils().resolveAndInit(definition);
        assertFalse(definition.isAutoCreate());
        definition.initDefaultObject(Boolean.TRUE);
        assertEquals(definition.getDataClass(), Boolean.class);

        new FlowTestingUtils().resolveAndInit(definition);
        Boolean t = (Boolean) definition.parse(flowPropertyProvider, null);
        assertNull(t);
        t = (Boolean) definition.getDefaultObject(new Dummy());
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());

        // check behavior if everything is not defined in the Ctor call
        // (i.e. like the definition is being defined in the hivemind.xml)
        definition = new FlowPropertyDefinitionImpl("foo");
        definition.initDefaultObject("true");
        definition.setDataClass(Boolean.class);
        new FlowTestingUtils().resolveAndInit(definition);
        t = (Boolean) definition.parse(flowPropertyProvider, null);
        assertNull(t);
        t = (Boolean) definition.getDefaultObject(new Dummy());
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());

        final FlowPropertyDefinitionImpl definition1 = new FlowPropertyDefinitionImpl("foo");
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
        	public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
        		// not checking so sure.
        		return true;
        	}
        });
        t = (Boolean) definition.getDefaultObject(new Dummy());
        assertEquals(t, Boolean.TRUE);
        assertTrue(definition.isAutoCreate());
    }

    private void assertDefaultObject(Class<?> clazz, Object value) {
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("u", clazz, FlowActivityPhase.advance);
        new FlowTestingUtils().resolveAndInit(definition);
        assertEquals(definition.getDefaultObject(new Dummy()), value);
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
        assertTrue((Boolean)definition.getDefaultObject(new Dummy()));
        assertEquals(definition.getDataClass(), definition1.getDataClass());
    }

    /**
     * Try to merge definitions with a collection of unknown types.
     */
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

    /**
     * Make sure FlowPropertyDefinitionImpl cloning works.
     */
    @Test(enabled=TEST_ENABLED)
    public void testFlowPropertyDefinitionCloning() {
        FlowPropertyDefinitionImpl original = new FlowPropertyDefinitionImpl("foo", Boolean.class, FlowActivityPhase.advance, Set.class, List.class).initAccess(PropertyScope.activityLocal, PropertyUsage.consume, ExternalPropertyAccessRestriction.noAccess);
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
        FlowPropertyProvider flowPropertyProvider = null;

        String result = def.parse(flowPropertyProvider, def.serialize(original));
        assertEquals(result, original);
    }

    private static final String URI = "uri";

    /**
     * Tests to make sure that {@link FlowPropertyDefinitionImpl} can handle serializing/deserializing lists.
     * @throws Exception
     */
    @Test(enabled=TEST_ENABLED)
    @SuppressWarnings("unchecked")
    public void testListSerializing() throws Exception {
        FlowPropertyProvider flowPropertyProvider = null;

        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl(URI, URI.class, FlowActivityPhase.advance, List.class);
        new FlowTestingUtils().resolveAndInit(definition);
        List<URI> list = Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov"));
        String strV = definition.serialize(list);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");
        List<URI> result = (List<URI>) definition.parse(flowPropertyProvider, strV);
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
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl(URI, URI.class, FlowActivityPhase.advance, Set.class);
        new FlowTestingUtils().resolveAndInit(definition);
        Set<URI> set = new LinkedHashSet<URI>(Arrays.asList(new URI("http://foo.com"), new URI("http://gg.gov")));
        String strV = definition.serialize(set);
        assertEquals(strV, "[\"http://foo.com\",\"http://gg.gov\"]");
        Set<URI> result = (Set<URI>) definition.parse(flowPropertyProvider, strV);
        assertTrue(set.containsAll(result));
        assertTrue(set.containsAll(set));
    }

    @Test(enabled=TEST_ENABLED)
    @SuppressWarnings("unchecked")
    public void testMapSerializing() throws Exception {
        FlowPropertyProvider flowPropertyProvider = null;
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl(URI, URI.class, FlowActivityPhase.advance, Map.class);
        new FlowTestingUtils().resolveAndInit(definition);
        Map<String, URI> map = new LinkedHashMap<String, URI>();
        map.put("first", new URI("http://foo.com"));
        map.put("second", new URI("http://gg.gov"));
        String strV = definition.serialize(map);
        assertEquals(strV, "{\"first\":\"http://foo.com\",\"second\":\"http://gg.gov\"}");
        Map<String, URI> result = (Map<String, URI>) definition.parse(flowPropertyProvider, strV);
        assertTrue(result.equals(map));
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
            FlowActivityImpl flowActivity = newFlowActivity();
            flowActivity.setFlowPropertyProviderName(activityName);
            for (PropertyScope propertyScope: PropertyScope.values()) {
                for(PropertyUsage propertyUsage: PropertyUsage.values()) {
                    String name= propertyScope+"_"+propertyUsage;
                    String externalInitial = "ext_"+name;
                    FlowPropertyDefinitionImplementor flowPropertyDefinition = new FlowPropertyDefinitionImpl(name).initAccess(propertyScope, propertyUsage);
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
                    // HACK: This needs to be re-enabled
//                    assertNull(actual, "PropertyUsage="+propertyUsage+" flowState="+flowState);
                }
                String changed = "chg_"+name;
                flowState.setProperty(name, changed);
            }
        }
        flowState.finishFlow();
        FlowValuesMap<FlowValueMapKey, CharSequence> finalMap = flowState.getExportedValuesMap();
        // now make sure that only the properties allowed to be set externally are set.
        for (PropertyScope propertyScope: PropertyScope.values()) {
            for(PropertyUsage propertyUsage: PropertyUsage.values()) {
                String name= propertyScope+"_"+propertyUsage;
                String externalInitial = "ext_"+name;
                String changed = "chg_"+name;
                CharSequence actual = finalMap.get(name);
                if ( !propertyScope.isCacheOnly() && propertyUsage.isOutputedProperty() || propertyScope == global) {
                    assertEquals(actual, changed, "name="+name+" PropertyUsage="+propertyUsage+" finalMap="+finalMap);
                } else if ( !propertyScope.isCacheOnly() && propertyUsage.isCleanOnInitialization()) {
                    assertNull(actual, "name="+name+" PropertyUsage="+propertyUsage+" finalMap="+finalMap);
                } else {
                    assertEquals(actual, externalInitial, "name="+name+" PropertyUsage="+propertyUsage+" finalMap="+finalMap);
                }
            }
        }

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
        FlowPropertyDefinitionImplementor flowPropertyDefinition = new FlowPropertyDefinitionImpl(key, boolean.class).initAccess(propertyScope, propertyUsage);
        FlowActivityImpl flowActivity =newFlowActivity();
        flowActivity.addPropertyDefinitions(flowPropertyDefinition);
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity);
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(
            "outside-property", "out-1",
            key, "false");
        FlowState flowState = flowTestingUtils.getFlowManagement().startFlowState(flowTypeName, false, initialFlowState, null);
        // TODO : decide how to handle undeclared properties - use case is when one flow is an intermediary between 2 flows.
        flowState.setProperty("not-a-property", true);
        flowState.setProperty(key, true);
        flowState.finishFlow();
        FlowValuesMap exportedMap = flowState.getExportedValuesMap();
        assertTrue(exportedMap.containsKey("not-a-property"), exportedMap+" should contain ");
        if ( flowPropertyDefinition.isCopyBackOnFlowSuccess()) {
            assertEquals(exportedMap.get(key), "true");
            assertEquals(exportedMap.size(), 3, "wrong size "+exportedMap);
        } else if ( propertyUsage == consume) {
            assertNull(exportedMap.get(key), "exportedMap="+exportedMap+" key="+key+ " flowState="+flowState);
            assertEquals(exportedMap.size(), 2, "wrong size "+exportedMap);
        } else {
            assertEquals(exportedMap.get(key), "false", "exportedMap="+exportedMap+" key="+key+ " flowState="+flowState);
            assertEquals(exportedMap.size(), 3, "wrong size "+exportedMap);
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
        FlowPropertyDefinitionImpl flowLocalProperty = new FlowPropertyDefinitionImpl("foo", Boolean.class).initPropertyScope(flowLocal);
        FlowPropertyDefinitionImpl activityLocalProperty = new FlowPropertyDefinitionImpl("foo", Boolean.class).initPropertyScope(activityLocal);
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
        FlowStateImpl flowStateImpl = flowManagement.startFlowState(flowTypeName, true, null, null);
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
        	public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
        		// not checking so sure.
        		return true;
        	}
        };
        FlowPropertyDefinitionImpl flowLocalProperty = new FlowPropertyDefinitionBuilder().createNonconfigurableFlowPropertyDefinition(propertyName, Boolean.class, flowPropertyValueProvider).toFlowPropertyDefinition();
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);

        FlowActivityImpl flowActivity1 = newFlowActivity();
        flowActivity1.setFlowPropertyProviderName("activity1");
        FlowPropertyDefinitionImpl activityLocalProperty = new FlowPropertyDefinitionImpl(propertyName, Map.class).initAccess(activityLocal,internalState);
        flowActivity1.addPropertyDefinitions(activityLocalProperty);

        FlowActivityImpl flowActivity2 = newFlowActivity();
        flowActivity2.setFlowPropertyProviderName("activity2");
        FlowPropertyDefinitionImpl globalProperty = new FlowPropertyDefinitionImpl(propertyName, Boolean.class).initAccess(flowLocal,io);
        flowActivity2.addPropertyDefinitions(globalProperty);

        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0, flowActivity1, flowActivity2);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(propertyName, "true");
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, initialFlowState , null);
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
        FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider =  new FlowPropertyValueProvider<FlowPropertyProvider>() {

            @Override
            public <T> T get(FlowPropertyProvider flowPropertyProvider,
                    FlowPropertyDefinition flowPropertyDefinition) {
            	ApplicationIllegalArgumentException.valid(isHandling(flowPropertyDefinition));
            	ApplicationIllegalArgumentException.valid(flowPropertyDefinition.isNamed(autoCreated), "but test should only get the ",autoCreated," property");
                CharSequence value = "ME: "+flowPropertyDefinition.getName();
                return (T) value;
            }

            @Override
            public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
                return FlowPropertyProvider.class;
            }
        	@Override
        	public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
        		return flowPropertyDefinition.isNamed(autoCreated) || flowPropertyDefinition.isNamed(notAutoCreated);
        	}
        };
        FlowPropertyDefinitionImpl flowNotAutoCreatedProperty = new FlowPropertyDefinitionImpl(notAutoCreated, Object.class).initAccess(flowLocal,io).
        initFlowPropertyValueProvider(flowPropertyValueProvider);
        FlowPropertyDefinitionImplementor flowLocalProperty = new FlowPropertyDefinitionBuilder().createApiReturnValueFlowPropertyDefinition(autoCreated, Object.class).
            initFlowPropertyValueProvider(flowPropertyValueProvider).toFlowPropertyDefinition();
        flowActivity0.addPropertyDefinitions(flowNotAutoCreatedProperty, flowLocalProperty );
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, null , null);
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

        FlowPropertyDefinitionImpl flowLocalProperty = new FlowPropertyDefinitionImpl(propertyName, String.class).initAccess(flowLocal,initialize).initInitial("true");
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);

        FlowActivityImpl flowActivity1 = newFlowActivity();
        flowActivity1.setFlowPropertyProviderName("activity1");
        FlowPropertyDefinitionImpl activityLocalProperty = new FlowPropertyDefinitionImpl(propertyName, String.class).initAccess(activityLocal,internalState);
        flowActivity1.addPropertyDefinitions(activityLocalProperty);

        FlowActivityImpl flowActivity2 = newFlowActivity();
        flowActivity2.setFlowPropertyProviderName("activity2");
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
     * @return
     */
    private FlowActivityImpl newFlowActivity() {
        return new FlowActivityImpl().initInvisible(false);
    }

    /**
     * Test to see that serialization/deserialization of enum's
     *
     * Also tests that {@link FlowPropertyDefinitionImpl#initInitial(String)} works.
     */
    @Test(enabled=TEST_ENABLED)
    public void testEnumHandling() {
        Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState("foo", SampleEnum.EXTERNAL);
        FlowImplementor flow = new FlowImpl(FLOW_TYPE);
        FlowPropertyDefinitionImpl definition = new FlowPropertyDefinitionImpl("foo", SampleEnum.class);
        flow.addPropertyDefinitions(definition);
        FlowActivityImpl fa1 = new FlowActivityImpl().initInvisible(false);
        definition = new FlowPropertyDefinitionImpl("fa1fp", SampleEnum.class).initInitial(SampleEnum.EMAIL.name());
        fa1.addPropertyDefinitions(definition);
        flow.addActivity(fa1);
        FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
        flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
        flowTestingUtils.getFlowDefinitionsManager().addDefinition(flow);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        String returnToFlowLookupKey = null;
        FlowState flowState = flowManagement.startFlowState(FLOW_TYPE, true, initialFlowState, returnToFlowLookupKey);
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
        final FlowPropertyDefinitionImpl noProvider = new FlowPropertyDefinitionImpl("noProvider", PropertyUsage.class);
        final FlowPropertyDefinitionImpl flowPropertyDefinition = new FlowPropertyDefinitionImpl("hasdefault", Boolean.class).initDefaultObject(Boolean.TRUE);
        FixedFlowPropertyValueProvider originalProvider = (FixedFlowPropertyValueProvider) flowPropertyDefinition.getFlowPropertyValueProvider();
        AbstractFlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider = new AbstractFlowPropertyValueProvider<FlowPropertyProvider>(flowPropertyDefinition) {

        	@Override
        	public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        		super.defineFlowPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters);
				Collection<FlowPropertyDefinitionImplementor> flowPropertyDefinitions = Arrays.<FlowPropertyDefinitionImplementor>asList(noProvider);
				super.addPropertyDefinitions(flowPropertyProvider, flowPropertyDefinitions, additionalConfigurationParameters);
        	}
            @Override
            public <T> T get(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
                throw new UnsupportedOperationException("should not be called.");
            }
        };
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
        flowPropertyValueProvider.defineFlowPropertyDefinitions(flowActivity0);
        flowActivity0.addPropertyDefinitions(flowPropertyDefinition);
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowState flowState = flowManagement.startFlowState(flowTypeName, false, null , null);
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
    @Test(enabled=TEST_ENABLED, expectedExceptions= {ApplicationIllegalArgumentException.class})
    public void testPreventAddingWrongFlowPropertyValueProvider() {
        FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider =  new FlowPropertyValueProvider<FlowPropertyProvider>() {

            @Override
            public <T> T get(FlowPropertyProvider flowPropertyProvider,  FlowPropertyDefinition flowPropertyDefinition) {
            	throw ApplicationIllegalArgumentException.fail();
            }

            @Override
            public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
                return FlowPropertyProvider.class;
            }
        	@Override
        	public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
        		return false;
        	}
        };
        new FlowPropertyDefinitionBuilder().createFlowPropertyDefinition("foo", Object.class).initFlowPropertyValueProvider(flowPropertyValueProvider);
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

        FlowPropertyDefinitionImpl flowLocalProperty = new FlowPropertyDefinitionImpl(propertyName, Boolean.class).initAccess(flowLocal,initialize);
        flowLocalProperty.initFlowPropertyValueProvider(new FlowPropertyValueProvider<FlowPropertyProviderWithValues>() {

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
        	public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
        		// not checking so sure.
        		return true;
        	}
        });
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
        flowActivity0.addPropertyDefinitions(flowLocalProperty);
        String flowTypeName = flowTestingUtils.addFlowDefinition(flowActivity0);
        FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
        FlowStateImplementor flowState = flowManagement.startFlowState(flowTypeName, false, null, null);
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

        FlowPropertyDefinitionImpl flowLocalProperty = new FlowPropertyDefinitionImpl(propertyName, Boolean.class).initAccess(flowLocal,initialize);
        flowLocalProperty.initFlowPropertyValueProvider(new FlowPropertyValueProvider<FlowPropertyProvider>() {

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
        	public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
        		// not checking so sure.
        		return true;
        	}
        });
        FlowActivityImpl flowActivity0 = newFlowActivity();
        flowActivity0.setFlowPropertyProviderName("activity0");
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
