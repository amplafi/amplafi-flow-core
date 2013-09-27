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

import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.translator.FlowTranslator;
import org.amplafi.json.IJsonWriter;


/**
 * Implementers inject the services that the FlowPropertyDefinitions ( and their supporting {@link FlowPropertyValueProvider}, et.al. need )
 *
 * Implementers tend to be a shim between the dependency injection framework used and the flow code.
 *
 * TODO : can we do the service injection at a higher level?
 * current issue is that the injection happens when a flow is run.
 *
 * Implementations determine which {@link FlowTranslator} should be used
 * for a given {@link FlowPropertyDefinition}.
 *
 * FlowTranslatorResolvers set the default {@link FlowTranslator} for {@link DataClassDefinition}s ( used by {@link FlowPropertyDefinition}s ).
 * The resolution happens when the flow that the {@link FlowPropertyDefinition} is part of is assigned added to a {@link FlowDefinitionsManager}.
 * FlowTranslatorResolvers usually only set the FlowTranslator when there is no FlowTranslator. However, a FlowTranslatorResolver is permitted disregard this suggestion.
 */
public interface FlowTranslatorResolver {
    /**
     * Determines the FlowTranslators needed for the supplied {@link FlowPropertyDefinition}.
     * @param context TODO
     * @param flowPropertyDefinition
     */
    void resolve(String context, FlowPropertyDefinition flowPropertyDefinition);
    /**
    *
    * @param context TODO
     * @param dataClassDefinition
     * @param resolvedRequired TODO
     * @return true if resolve was successful in setting the FlowTranslator.
    */
    boolean resolve(String context, DataClassDefinition dataClassDefinition, boolean resolvedRequired);
    /**
     * all the other methods end up calling this method.
     * @param clazz
     * @return the FlowTranslator or null if none could be found.
     */
    FlowTranslator<?> resolve(Class<?> clazz);
    void resolveFlow(Flow flow);

    // needed only for case when flow translator is not given a outputWriter
    // this is a really rare case - MapFlowTranslator seems to be about the only good example
    // we should be able to get rid of this.
    @Deprecated
    IJsonWriter getJsonWriter();

    /**
     * @param flowPropertyProvider
     */
    void resolve(FlowPropertyProvider flowPropertyProvider);
}
