/**
 * Copyright 2006-8 by Amplafi, Inc.
 */
package org.amplafi.flow.translator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowDefinitionsManager;
import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.json.JSONStringer;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;
import org.amplafi.json.MapByClass;
import org.apache.commons.collections.MapUtils;


import static org.apache.commons.collections.CollectionUtils.*;

/**
 * Intended to be a stateless singleton service that will provide {@link FlowTranslator} to {@link FlowPropertyDefinition}
 * that do have their {@link FlowPropertyDefinition#getDataClassDefinition()}.{@link DataClassDefinition#isFlowTranslatorSet()} == false
 *
 */
public class BaseFlowTranslatorResolver implements FlowTranslatorResolver {

    private Map<Class<?>, FlowTranslator<?>> translators;
    private Map<Class<?>, JsonRenderer<?>> jsonRenderers;
    private List<FlowTranslator<?>> flowTranslators  = new CopyOnWriteArrayList<FlowTranslator<?>>();
    private FlowDefinitionsManager flowDefinitionsManager;
    /**
     * These are {@link FlowPropertyDefinition}s that are core to the functioning of the AmpFlow code.
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
     *
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
//            new FlowPropertyDefinition(FSREADONLY, boolean.class).initPropertyUsage(flowLocal),
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
    protected void initCoreFlowActivityFlowPropertyDefinitions() {
        // too many problems at this point #2192 #2179
//        this.putCoreFlowPropertyDefinitions(
//            new FlowPropertyDefinition(FATITLE_TEXT).initPropertyUsage(activityLocal),
//            new FlowPropertyDefinition(FAUPDATE_TEXT).initPropertyUsage(activityLocal),
//            new FlowPropertyDefinition(FANEXT_TEXT).initPropertyUsage(activityLocal),
//            new FlowPropertyDefinition(FAPREV_TEXT).initPropertyUsage(activityLocal)
//        );
    }
    public void putCoreFlowPropertyDefinitions(FlowPropertyDefinition... flowPropertyDefinitions) {
        for (FlowPropertyDefinition flowPropertyDefinition: flowPropertyDefinitions) {
            this.resolve(flowPropertyDefinition);
            this.coreFlowPropertyDefinitions.put(flowPropertyDefinition.getName(), flowPropertyDefinition);
        }
    }
    public void putCommonFlowPropertyDefinitions(FlowPropertyDefinition... flowPropertyDefinitions) {
        for (FlowPropertyDefinition flowPropertyDefinition: flowPropertyDefinitions) {
            this.resolve(flowPropertyDefinition);
            this.commonFlowPropertyDefinitions.put(flowPropertyDefinition.getName(), flowPropertyDefinition);
        }
    }

    /**
     *
     */
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
     * @see org.amplafi.flow.translator.FlowTranslatorResolver#resolve(org.amplafi.flow.flowproperty.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void resolve(FlowPropertyDefinition definition) {
        FlowPropertyDefinition standardFlowPropertyDefinition = getFlowPropertyDefinition(definition.getName());
        if ( standardFlowPropertyDefinition != null) {
            definition.merge(standardFlowPropertyDefinition);
        }
        resolve(definition.getDataClassDefinition());
        definition.initialize();
    }
    public void resolve(DataClassDefinition definition) {
        if (definition == null || definition.isFlowTranslatorSet()) {
            return;
        }
        definition.setFlowTranslator(resolve(definition.getDataClass()));
        resolve(definition.getElementDataClassDefinition());
        resolve(definition.getKeyDataClassDefinition());
    }

    /**
     * @see org.amplafi.flow.translator.FlowTranslatorResolver#resolve(java.lang.Class)
     */
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
     * @see org.amplafi.flow.translator.FlowTranslatorResolver#resolveFlow(org.amplafi.flow.Flow)
     */
    @Override
    public void resolveFlow(Flow flow) {
        Map<String, FlowPropertyDefinition> propertyDefinitions = flow.getPropertyDefinitions();
        if ( MapUtils.isNotEmpty(propertyDefinitions) ) {
            Collection<FlowPropertyDefinition> values = propertyDefinitions.values();
            initAndResolveCollection(values);
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
        Map<String, FlowPropertyDefinition> propertyDefinitions;
        propertyDefinitions = flowActivity.getPropertyDefinitions();
        if ( MapUtils.isNotEmpty(propertyDefinitions)) {
            Collection<FlowPropertyDefinition> values = propertyDefinitions.values();
            initAndResolveCollection(values);
        }
    }
    private void initAndResolveCollection(Collection<FlowPropertyDefinition> values) {
        for(FlowPropertyDefinition definition: values) {
            resolve(definition);
        }
    }
    public JSONWriter getJsonWriter() {
        JSONWriter writer = new JSONStringer(new MapByClass<JsonRenderer<?>>(this.jsonRenderers));
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
     * @param flowDefinitionsManager the flowDefinitionsManager to set
     */
    public void setFlowDefinitionsManager(FlowDefinitionsManager flowDefinitionsManager) {
        this.flowDefinitionsManager = flowDefinitionsManager;
    }
    /**
     * @return the flowDefinitionsManager
     */
    public FlowDefinitionsManager getFlowDefinitionsManager() {
        return flowDefinitionsManager;
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

}
