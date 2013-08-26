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
package org.amplafi.flow.definitions;

import java.util.Map;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider;

/**
 * Implementers will provide Flow definitions.
 * 
 * A definition source is a String key mapped collection of F that extends {@link FlowDefinition}. They allow you to package
 * flow definitions in a convenient way and then handle the results as a whole. Example usages are, creating a bulk converter of 
 * {@link FlowPropertyDefinitionProvider}, {@link FlowActivity} or XMLs into flows. follow the type hierarchy with ctrl+t for 
 * DefinitionSource to learn the types of converters that are available
 *
 * TODO(pat) should be namespace owner.
 * @author patmoore
 * @param <F>
 *
 */
public interface DefinitionSource<F extends FlowDefinition> {

    /**
     * Returns the flow having the specified name.
     * @param flowTypeName the name of the flow.
     * @return the Flow definition. null if {@link #isFlowDefined(String)} returns false.
     */
    F getFlowDefinition(String flowTypeName);
    /**
     * does this DefinitionSource have a definition for a flow with supplied name.
     * @param flowTypeName the name of the flow
     * @return true if definition exists.
     */
    boolean isFlowDefined(String flowTypeName);

    /**
     * Returns all defined flows, keyed by their name.
     * @return the map with all the currently defined flows indexed by (usually) the
     *                 {@link org.amplafi.flow.Flow#getFlowPropertyProviderName()}.
     */
    Map<String, F> getFlowDefinitions();
}
