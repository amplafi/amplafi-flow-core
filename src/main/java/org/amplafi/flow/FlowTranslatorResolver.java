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

import org.amplafi.json.JSONWriter;


/**
 * Implementations determine which {@link FlowTranslator} should be used
 * for a given {@link FlowPropertyDefinition}.
 *
 */
public interface FlowTranslatorResolver {
    /**
     *
     * @param flowPropertyDefinition
     */
    public void resolve(FlowPropertyDefinition flowPropertyDefinition);
    /**
    *
    * @param dataClassDefinition
    */
   public void resolve(DataClassDefinition dataClassDefinition);
    /**
     * all the other methods end up calling this method.
     * @param clazz
     * @return the FlowTranslator or null if none could be found.
     */
    public FlowTranslator<?> resolve(Class<?> clazz);
    public void resolveFlow(Flow flow);

    JSONWriter getJsonWriter();

    /**
     * @param key
     * @return the {@link FlowPropertyDefinition} for this key.
     */
    public FlowPropertyDefinition getFlowPropertyDefinition(String key);
    public void putCommonFlowPropertyDefinitions(FlowPropertyDefinition... flowPropertyDefinitions);
    /**
     * @param flowActivity
     */
    public void resolve(FlowActivity flowActivity);
}
