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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.amplafi.flow.*;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JSONStringer;
import org.amplafi.json.JsonRenderer;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;

import com.sworddance.beans.MapByClass;


import static org.apache.commons.collections.CollectionUtils.*;

/**
 * Intended to be a stateless singleton service that will provide {@link FlowTranslator} to {@link org.amplafi.flow.FlowPropertyDefinition}
 * that do have their {@link org.amplafi.flow.FlowPropertyDefinition#getDataClassDefinition()}.{@link org.amplafi.flow.flowproperty.DataClassDefinitionImpl#isFlowTranslatorSet()} == false
 *
 */
public class BaseFlowTranslatorResolver implements FlowTranslatorResolver {

    private Map<Class<?>, FlowTranslator<?>> translators;
    private Map<Class<?>, JsonRenderer<?>> jsonRenderers;
    private Log log;
    private List<FlowTranslator<?>> flowTranslators  = new CopyOnWriteArrayList<FlowTranslator<?>>();
    /**
     * These are {@link org.amplafi.flow.FlowPropertyDefinition}s that are core to the functioning of the AmpFlow code.
     * These should not be altered.
     */
    private Map<String, FlowPropertyDefinition> coreFlowPropertyDefinitions;
    /**
     * These are the definitions that are standard across many different flowActivities.
     * Rather than be defined in each FlowActivity (which results in duplication and may result in
     * differing definitions), these standard definitions can be defined and injected.
     */
    private Map<String, FlowPropertyDefinition> commonFlowPropertyDefinitions;

    public BaseFlowTranslatorResolver() {
        // this instead of ConcurrentHashMap so that tests that are order dependent will still pass.
        this.coreFlowPropertyDefinitions = Collections.synchronizedMap(new LinkedHashMap<String, FlowPropertyDefinition>());
        this.commonFlowPropertyDefinitions = Collections.synchronizedMap(new LinkedHashMap<String, FlowPropertyDefinition>());

    }
    public void initializeService() {
        translators = new MapByClass<FlowTranslator<?>>();

        jsonRenderers = new MapByClass<JsonRenderer<?>>();
        for(FlowTranslator<?> flowTranslator: getFlowTranslators() ) {
            addFlowTranslator(flowTranslator);
        }
        initCoreFlowPropertyDefinitions();
        initCoreFlowActivityFlowPropertyDefinitions();
    }
    /**
     * TODO -- move to a {@link FlowPropertyProvider} implementation
     */
    protected void initCoreFlowPropertyDefinitions() {
        // too many problems at this point #2192 #2179
//        this.putCoreFlowPropertyDefinitions(
//            new FlowPropertyDefinition(FSTITLE_TEXT).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSCANCEL_TEXT).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSNO_CANCEL, boolean.class).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSFINISH_TEXT).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSFLOW_TRANSITIONS, FlowTransition.class, Map.class).initAutoCreate().initPropertyUsage(flowLocal)
//            .initFlowPropertyValueProvider(new AddToMapFlowPropertyValueProvider<String, FlowTransition>(
//                    new FlowTransition(null, DEFAULT_FSFINISH_TEXT, TransitionType.normal, null))),
//            // io -- for now because need to communicate the next page to be displayed
//            new FlowPropertyDefinition(FSPAGE_NAME).initPropertyUsage(io),
//            new FlowPropertyDefinition(FSAFTER_PAGE).initPropertyUsage(io),
//            new FlowPropertyDefinition(FSDEFAULT_AFTER_PAGE).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSDEFAULT_AFTER_CANCEL_PAGE).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSREDIRECT_URL, URI.class).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSHIDE_FLOW_CONTROL, boolean.class).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSACTIVATABLE, boolean.class).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSAUTO_COMPLETE, boolean.class).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSALT_FINISHED).initPropertyUsage(flowLocal),
//            new FlowPropertyDefinition(FSCONTINUE_WITH_FLOW).initPropertyUsage(io),
//            new FlowPropertyDefinition(FSNEXT_FLOW).initPropertyUsage(io),
//            new FlowPropertyDefinition(FSIMMEDIATE_SAVE, boolean.class).initPropertyUsage(flowLocal)
//            );
    }
    /**
     * TODO -- move to a {@link FlowPropertyProvider} implementation
     */
    protected void initCoreFlowActivityFlowPropertyDefinitions() {
        // too many problems at this point #2192 #2179
//        this.putCoreFlowPropertyDefinitions(
//            new FlowPropertyDefinition(FATITLE_TEXT).initPropertyUsage(activityLocal),
//            new FlowPropertyDefinition(FAUPDATE_TEXT).initPropertyUsage(activityLocal),
//            new FlowPropertyDefinition(FANEXT_TEXT).initPropertyUsage(activityLocal),
//            new FlowPropertyDefinition(FAPREV_TEXT).initPropertyUsage(activityLocal)
//        );
    }
    /**
     * TODO -- move to a {@link FlowPropertyProvider} implementation
     */
    public void putCoreFlowPropertyDefinitions(FlowPropertyDefinition... flowPropertyDefinitions) {
        for (FlowPropertyDefinition flowPropertyDefinition: flowPropertyDefinitions) {
            this.resolve("", flowPropertyDefinition);
            this.coreFlowPropertyDefinitions.put(flowPropertyDefinition.getName(), flowPropertyDefinition);
        }
    }
    public void putCommonFlowPropertyDefinitions(FlowPropertyDefinition... flowPropertyDefinitions) {
        for (FlowPropertyDefinition flowPropertyDefinition: flowPropertyDefinitions) {
            this.resolve("", flowPropertyDefinition);
            this.commonFlowPropertyDefinitions.put(flowPropertyDefinition.getName(), flowPropertyDefinition);
        }
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public void addStandardFlowTranslators() {
        getFlowTranslators().add(new ListFlowTranslator());
        getFlowTranslators().add(new SetFlowTranslator());
        getFlowTranslators().add(new MapFlowTranslator());
        getFlowTranslators().add(new IntegerFlowTranslator());
        getFlowTranslators().add(new LongFlowTranslator());
        getFlowTranslators().add(new BooleanFlowTranslator());
        getFlowTranslators().add(new CalendarFlowTranslator());
        getFlowTranslators().add(new JsonSelfRendererFlowTranslator());
        getFlowTranslators().add(new UriFlowTranslator());
        getFlowTranslators().add(new EnumFlowTranslator());
        for(FlowTranslator<?> flowTranslator: this.flowTranslators) {
            if ( flowTranslator instanceof AbstractFlowTranslator) {
                ((AbstractFlowTranslator<?>)flowTranslator).setFlowTranslatorResolver(this);
            }
        }
    }
    public void addFlowTranslator(FlowTranslator<?> flowTranslator) {
        List<Class<?>> deserializedFormClasses = flowTranslator.getDeserializedFormClasses();
        for(Class<?>clazz: deserializedFormClasses) {
            addFlowTranslator(flowTranslator, clazz);
        }
    }
    /**
     * @param flowTranslator
     * @param clazz
     * @return the previous FlowTranslator
     */
    public FlowTranslator<?> addFlowTranslator(FlowTranslator<?> flowTranslator, Class<?> clazz) {
        JsonRenderer<?> jsonRenderer = flowTranslator.getJsonRenderer();
        if ( jsonRenderer != null) {
            this.jsonRenderers.put(clazz, jsonRenderer);
        }
        return translators.put(clazz, flowTranslator);
    }
    /**
     * @see org.amplafi.flow.FlowTranslatorResolver#resolve(String, org.amplafi.flow.FlowPropertyDefinition)
     */
    @Override
    public void resolve(String context, FlowPropertyDefinition definition) {
        FlowPropertyDefinition standardFlowPropertyDefinition = getFlowPropertyDefinition(definition.getName());
        if ( standardFlowPropertyDefinition != null) {
            definition.merge(standardFlowPropertyDefinition);
        }
        if ( !resolve(context+definition.getName()+":", definition.getDataClassDefinition(), !definition.isCacheOnly())) {
            // TODO: anything special?
        }
        ((FlowPropertyDefinitionImplementor)definition).initialize();
    }
    public boolean resolve(String context, DataClassDefinition definition, boolean resolvedRequired) {
        if (definition == null || definition.isFlowTranslatorSet()) {
            return true;
        } else {
            FlowTranslator<?> flowTranslator = resolve(definition.getDataClass());
            if ( flowTranslator != null) {
                definition.setFlowTranslator(flowTranslator);
                boolean resolved = resolve(context+definition.getDataClass()+"(element):", definition.getElementDataClassDefinition(), resolvedRequired);
                resolved &= resolve(context+definition.getKeyClass()+"(key):", definition.getKeyDataClassDefinition(), resolvedRequired);
                return resolved;
            } else if (resolvedRequired) {
                getLog().warn(context+definition+ " was not able to determine the correct FlowTranslator.");
                return false;
            } else {
                getLog().debug(context+definition+ " was not able to determine the correct FlowTranslator. But resolving class was not required, so it may not matter. ( usually this means FlowProperty is cache only)");
                return false;
            }
        }
    }
    /**
     * @see org.amplafi.flow.FlowTranslatorResolver#resolve(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public FlowTranslator<?> resolve(Class<?> clazz) {
        FlowTranslator<?> flowTranslator;
        if ( clazz == null ) {
            flowTranslator = CharSequenceFlowTranslator.INSTANCE;
        } else {
            flowTranslator = this.translators.get(clazz);
        }
        if ( flowTranslator == null && CharSequence.class.isAssignableFrom(clazz)) {
            flowTranslator = new CharSequenceFlowTranslator();
        }
        if ( flowTranslator != null && flowTranslator instanceof InstanceSpecificFlowTranslator) {
            flowTranslator = ((InstanceSpecificFlowTranslator) flowTranslator).resolveFlowTranslator(clazz);
        }
        return flowTranslator;
    }

    /**
     * @see org.amplafi.flow.FlowTranslatorResolver#resolveFlow(org.amplafi.flow.Flow)
     */
    @Override
    public void resolveFlow(Flow flow) {
        Map<String, FlowPropertyDefinition> propertyDefinitions = flow.getPropertyDefinitions();
        if ( MapUtils.isNotEmpty(propertyDefinitions) ) {
            Collection<FlowPropertyDefinition> values = propertyDefinitions.values();
            initAndResolveCollection(flow.getFlowPropertyProviderName()+".", values);
        }
        List<FlowActivityImplementor> activities = flow.getActivities();
        if ( activities != null ) {
            for(FlowActivityImplementor flowActivity: activities) {
                resolve(flowActivity);
                // TODO ideally here...
//                if ( !flow.isInstance()) {
//                    flowActivity.processDefinitions();
//                }
            }
        }
    }

    /**
     * @param flowActivity
     */
    public void resolve(FlowActivity flowActivity) {
        if ( flowActivity != null) {
            Map<String, FlowPropertyDefinition> propertyDefinitions = flowActivity.getPropertyDefinitions();
            if ( MapUtils.isNotEmpty(propertyDefinitions)) {
                Collection<FlowPropertyDefinition> values = propertyDefinitions.values();
                initAndResolveCollection(flowActivity.getFlowPropertyProviderFullName()+"("+flowActivity.getClass().getName()+").", values);
            }
        }
    }
    private void initAndResolveCollection(String context, Collection<FlowPropertyDefinition> values) {
        for(FlowPropertyDefinition definition: values) {
            resolve(context, definition);
        }
    }
    public IJsonWriter getJsonWriter() {
        IJsonWriter writer = new JSONStringer(new MapByClass<JsonRenderer<?>>(this.jsonRenderers));
        return writer;
    }
    /**
     * @param flowTranslatorsList the flowTranslators to set
     */
    public void setFlowTranslators(List<FlowTranslator<?>> flowTranslatorsList) {
        this.flowTranslators.clear();
        if ( isNotEmpty(flowTranslatorsList)) {
            this.flowTranslators.addAll(flowTranslatorsList);
        }
    }
    /**
     * @return the flowTranslators
     */
    public List<FlowTranslator<?>> getFlowTranslators() {
        return flowTranslators;
    }

    /**
     * @param commonFlowPropertyDefinitions the commonFlowPropertyDefinitions to set
     */
    public void setCommonFlowPropertyDefinitions(Map<String, FlowPropertyDefinition> commonFlowPropertyDefinitions) {
        this.commonFlowPropertyDefinitions = commonFlowPropertyDefinitions;
    }
    /**
     * @return the commonFlowPropertyDefinitions
     */
    public Map<String, FlowPropertyDefinition> getCommonFlowPropertyDefinitions() {
        return commonFlowPropertyDefinitions;
    }

    public FlowPropertyDefinition getFlowPropertyDefinition(String key) {
        FlowPropertyDefinition flowPropertyDefinition = this.commonFlowPropertyDefinitions.get(key);
        if (flowPropertyDefinition == null) {
            flowPropertyDefinition = this.coreFlowPropertyDefinitions.get(key);
        }
        return flowPropertyDefinition;
    }
    /**
     * @param log the log to set
     */
    public void setLog(Log log) {
        this.log = log;
    }
    /**
     * @return the log
     */
    public Log getLog() {
        return log;
    }

}
