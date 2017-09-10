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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowDefinitionsManager;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowTranslatorResolver;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.flowproperty.Resolvable;
import org.amplafi.flow.json.IJsonWriter;
import org.amplafi.flow.json.JSONStringer;
import org.amplafi.flow.json.JsonRenderer;
import org.amplafi.flow.json.translator.FlowAwareJsonSelfRendererFlowTranslator;
import org.amplafi.flow.json.translator.JSONArrayFlowTranslator;
import org.amplafi.flow.json.translator.JSONObjectFlowTranslator;
import org.amplafi.flow.json.translator.JsonSelfRendererFlowTranslator;

import com.sworddance.beans.MapByClass;

import org.apache.commons.logging.Log;


/**
 * Intended to be a stateless singleton service that will provide {@link FlowTranslator} to {@link org.amplafi.flow.FlowPropertyDefinition}
 * that do have their {@link org.amplafi.flow.FlowPropertyDefinition#getDataClassDefinition()}.{@link org.amplafi.flow.flowproperty.DataClassDefinitionImpl#isFlowTranslatorSet()} == false
 *
 */
public class BaseFlowTranslatorResolver implements FlowTranslatorResolver {

    private FlowDefinitionsManager flowDefinitionsManager;
    private Map<Class<?>, FlowTranslator<?>> translators;
    private Log log;
    private List<FlowTranslator<?>> flowTranslators  = new CopyOnWriteArrayList<>();

    public BaseFlowTranslatorResolver() {

    }
    public void initializeService() {
        translators = new MapByClass<>();
        this.addStandardFlowTranslators();
        for(FlowTranslator<?> flowTranslator: getFlowTranslators() ) {
            addFlowTranslator(flowTranslator);
        }
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private void addStandardFlowTranslators() {
        getFlowTranslators().add(new UriFlowTranslator());
        getFlowTranslators().add(new EnumFlowTranslator());
        getFlowTranslators().add(new CalendarFlowTranslator());
        getFlowTranslators().add(new JsonSelfRendererFlowTranslator());
        getFlowTranslators().add(new FlowAwareJsonSelfRendererFlowTranslator());
        getFlowTranslators().add(new JSONObjectFlowTranslator());
        getFlowTranslators().add(new JSONArrayFlowTranslator());
        getFlowTranslators().add(new LongFlowTranslator());
        getFlowTranslators().add(new IntegerFlowTranslator());
        getFlowTranslators().add(new BooleanFlowTranslator());
        getFlowTranslators().add(new TimezoneFlowTranslator());
        getFlowTranslators().add(new ListFlowTranslator());
        getFlowTranslators().add(new SetFlowTranslator());
        getFlowTranslators().add(new MapFlowTranslator(java.util.Map.class, java.util.LinkedHashMap.class));
        getFlowTranslators().add(new MapFlowTranslator(java.util.NavigableMap.class, java.util.TreeMap.class));
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
     */
    public void addFlowTranslator(FlowTranslator<?> flowTranslator, Class<?> clazz) {
        translators.put(clazz, flowTranslator);
    }
    /**
     * @see org.amplafi.flow.FlowTranslatorResolver#resolve(String, org.amplafi.flow.FlowPropertyDefinition)
     */
    @Override
    public void resolve(String context, FlowPropertyDefinition definition) {
        FlowPropertyDefinitionBuilder standardFlowPropertyDefinitionBuilder = getFlowDefinitionsManager().getFactoryFlowPropertyDefinitionBuilder(definition.getName(), definition.getDataClass());
        if ( standardFlowPropertyDefinitionBuilder != null) {
            definition.merge(standardFlowPropertyDefinitionBuilder.toFlowPropertyDefinition());
        }
        // TODO: do we need to look at definition.isReadOnly() ?
        if ( !resolve(context+definition.getName()+":", definition.getDataClassDefinition(), !definition.isCacheOnly())) {
            // TODO: anything special?
        }
        ((FlowPropertyDefinitionImplementor)definition).initialize();
    }
    @Override
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
                getLog().warn(context+":"+definition+ " was not able to determine the correct FlowTranslator.");
                return false;
            } else {
                getLog().debug(context+":"+definition+ " was not able to determine the correct FlowTranslator. But resolving class was not required, so it may not matter. ( usually this means FlowProperty is cache only)");
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
        if (!(flow instanceof Resolvable) || !((Resolvable)flow).isResolved() ) {
            resolve(flow);
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
    }

    /**
     * @param flowPropertyProvider
     */
    @Override
    public void resolve(FlowPropertyProvider flowPropertyProvider) {
        if ( flowPropertyProvider != null && (!(flowPropertyProvider instanceof Resolvable) || !((Resolvable)flowPropertyProvider).isResolved() )) {
            Map<String, FlowPropertyDefinition> propertyDefinitions = flowPropertyProvider.getPropertyDefinitions();
            if ( propertyDefinitions != null && !propertyDefinitions.isEmpty()) {
                Collection<FlowPropertyDefinition> values = propertyDefinitions.values();
                initAndResolveCollection(flowPropertyProvider.getFlowPropertyProviderFullName()+"("+flowPropertyProvider.getClass().getName()+").", values);
            }
            if (flowPropertyProvider instanceof Resolvable) {
                ((Resolvable)flowPropertyProvider).setResolved(true);
            }
        }
    }
    private void initAndResolveCollection(String context, Collection<FlowPropertyDefinition> values) {
        for(FlowPropertyDefinition definition: values) {
            resolve(context, definition);
        }
    }
    @Override
    public IJsonWriter getJsonWriter() {
        MapByClass<JsonRenderer<?>>jsonRenderers = this.translators.values().stream()
                .filter(translator -> translator.getJsonRenderer() != null)
                .map(translator -> translator.getJsonRenderer())
                .collect(Collectors.toMap(jsonRenderer -> jsonRenderer.getClassToRender(),
                    jsonRenderer -> jsonRenderer,
                    (first, last) -> last, MapByClass::new)
                    );
        IJsonWriter writer = new JSONStringer(jsonRenderers);
        return writer;
    }
    /**
     * @param flowTranslatorsList the flowTranslators to set
     */
    public void setFlowTranslators(List<FlowTranslator<?>> flowTranslatorsList) {
        this.flowTranslators.clear();
        if ( flowTranslatorsList != null && !flowTranslatorsList.isEmpty()) {
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
    public void setFlowDefinitionsManager(FlowDefinitionsManager flowDefinitionsManager) {
        this.flowDefinitionsManager = flowDefinitionsManager;
    }
    public FlowDefinitionsManager getFlowDefinitionsManager() {
        return flowDefinitionsManager;
    }

}
