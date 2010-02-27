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

import java.util.List;

import org.amplafi.flow.definitions.DefinitionSource;
import org.amplafi.flow.flowproperty.FlowPropertyProviderImplementor;

/**
 * @author patmoore
 *
 */
public interface FlowImplementor extends Flow, FlowPropertyProviderImplementor, DefinitionSource {

    /**
     * Create an instance of a Flow from Flow definition.
     * The {@link FlowActivity} array is copied but not the {@link FlowActivity}s themselves.
     * @return flow instance.
     */
    FlowImplementor createInstance();

    /**
     * @param activities
     *            The activities to set.
     */
    void setActivities(List<FlowActivityImplementor> activities);

    /**
     * add another {@link FlowActivityImplementor} to the end of this Flow. The {@link FlowActivityImplementor#getFlowPropertyProviderName()} must
     * not duplicate the name of any previously added FlowActivityImplementor. The check is case-insensitive.
     * @param activity
     */
    void addActivity(FlowActivityImplementor activity);

    void setFlowState(FlowState state);

    void setContinueFlowTitle(String continueFlowTitle);

    void setLinkTitle(String linkTitle);

    void setFlowTitle(String flowTitle);

    void setMouseoverEntryPointText(String mouseoverEntryPointText);

    void setFlowDescriptionText(String flowDescriptionText);

    void setPageName(String pageName);

    void setDefaultAfterPage(String defaultAfterPage);
}
