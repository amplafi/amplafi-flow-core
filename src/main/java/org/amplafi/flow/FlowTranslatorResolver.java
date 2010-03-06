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
import org.amplafi.json.IJsonWriter;


/**
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

    IJsonWriter getJsonWriter();

    /**
     * TODO: probably should be a copy so the definition could be altered.
     * @param key
     * @return the standard {@link FlowPropertyDefinition} for this key.
     */
    FlowPropertyDefinition getFlowPropertyDefinition(String key);
    void putCommonFlowPropertyDefinitions(FlowPropertyDefinition... flowPropertyDefinitions);
    /**
     * @param flowPropertyProvider
     */
    void resolve(FlowPropertyProvider flowPropertyProvider);
}
