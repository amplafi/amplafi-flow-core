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

import java.net.URI;


/**
 * FlowManager is a factory for creating and managing the standard {@link FlowManagement} implementations.
 *
 * @author Patrick Moore
 */
public interface FlowManager {

    public Flow getInstanceFromDefinition(String flowTypeName);

    /**
     * Returns the flow having the specified name.
     * @param flowTypeName
     * @return the Flow definition.
     */
    public Flow getFlowDefinition(String flowTypeName);
    public boolean isFlowDefined(String flowTypeName);

    public FlowManagement getFlowManagement();

    /**
     * @param flowTranslatorResolver
     */
    public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver);

    public FlowTranslatorResolver getFlowTranslatorResolver();

    /**
     * If the URI is relative, then the URI is to refer to a local page. This is usually a static setting.
     * @see FlowManagement#getDefaultHomePage()
     * @return the default home to use when a flow ends and there is no other place to return.
     */
    public URI getDefaultHomePage();
}