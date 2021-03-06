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

import org.amplafi.flow.flowproperty.FlowPropertyProviderImplementor;

/**
 * @author patmoore
 *
 * TODO: may not make sense to extend FlowStateProvider as definition FlowActivityImplementors will not have a FlowState.
 *
 */
public interface FlowActivityImplementor extends FlowActivity, FlowPropertyProviderImplementor, FlowStateProvider {
    /**
     * @return instance of this definition
     */
    FlowActivityImplementor createInstance();

    /**
     * @param nextFlow
     * @return the nextFlow after all property substitution has had a change to find the real name.
     */
    String resolveIndirectReference(String nextFlow);

    /**
     *
     */
    @Deprecated // only because FlowActivity can't handle properties in the ctor
    void processDefinitions();
    /**
     * If the property has no value stored in the flowState's keyvalueMap then
     * put the supplied value in it.
     *
     * @param key
     * @param value
     * @see #isPropertySet(String)
     */
    @Deprecated // use FlowPropertyValueProvider with PropertyRequired set
    void initPropertyIfNull(String key, Object value) ;

    @Deprecated // use FlowPropertyValueProvider with PropertyRequired set
    void initPropertyIfBlank(String key, Object value);

    /**
     *
     * @param key
     * @return true if {@link #getRawProperty(String)} returns null or blank
     */
    boolean isPropertyBlank(String key);

    void setFlow(FlowImplementor flow);

    /**
     * @param activatable true if this flowActivity can be selected from the UI.
     */
    void setActivatable(boolean activatable);

    /**
     * @param activityTitle The flowTitle to set.
     */
    void setActivityTitle(String activityTitle);
    /**
     * @param finishedActivity The user can finish the flow when this activity
     *        is current.
     */
    void setFinishingActivity(boolean finishedActivity);
    <T extends FlowActivityImplementor> T initInvisible(Boolean invisible);

    void setInvisible(Boolean invisible);


    /**
     * @param pageName The pageName to set.
     */
    void setPageName(String pageName);
    void setComponentName(String componentName);
}
