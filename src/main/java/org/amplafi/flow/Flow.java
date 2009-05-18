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
import java.util.Map;

/**
 * defines a definition of a flow or a specific flow.
 *
 * <p>
 * Flows consist of FlowActivities. FlowActivities can be shared across
 * instances of Flows.
 * <p>
 * FlowActivities can create new objects. However it is the responsibility of
 * the Flow to determine if the object created by FlowActivities should be
 * actually persisted. (i.e. committed to the database.)
 * <p>
 * Flows are also responsible for connecting relationships. A FlowActivity may
 * create a relationship object but it will not be aware of the endpoints of
 * that relationship (a FlowActivity is not aware of the Flow nor its
 * history/state.)
 * <p>
 * Flows should not keep references to objects that FlowActivities create or
 * retrieve. The FlowActivity is responsible for that. This is important if a
 * FlowActivity is shared amongst Flow instances.
 * </p>
 */
public interface Flow {

    /**
     * Create an instance of a Flow from Flow definition.
     * The {@link FlowActivity} array is copied but not the {@link FlowActivity}s themselves.
     * @return flow instance.
     */
    public Flow createInstance();

    /**
     * @param activities
     *            The activities to set.
     */
    public void setActivities(List<FlowActivityImplementor> activities);

    /**
     * @param <T>
     * @return the activities.
     */
    public <T extends FlowActivity> List<T> getActivities();

    public <T extends FlowActivity> T getActivity(int activityIndex);

    /**
     * add another {@link FlowActivityImplementor} to the end of this Flow. The {@link FlowActivityImplementor#getActivityName()} must
     * not duplicate the name of any previously added FlowActivityImplementor. The check is case-insensitive.
     * @param activity
     */
    public void addActivity(FlowActivityImplementor activity);

    /**
     * @return Returns the definition.
     */
    public boolean isInstance();

    public <T extends FlowActivity> List<T> getVisibleActivities();

    public void setPropertyDefinitions(Map<String, FlowPropertyDefinition> properties);

    public Map<String, FlowPropertyDefinition> getPropertyDefinitions();

    public FlowPropertyDefinition getPropertyDefinition(String key);

    public void addPropertyDefinition(FlowPropertyDefinition definition);

    public void setFlowTypeName(String flowTypeName);

    public String getFlowTypeName();

    /**
     * @return get the flow name as it should appear in the flowentry and the
     *         titlebar.
     */
    public String getFlowTitle();

    public void setFlowTitle(String flowTitle);

    /**
     * @return Used if this is a secondary flow that will be started as the next
     *         flow.
     */
    public String getContinueFlowTitle();

    public void setContinueFlowTitle(String continueFlowTitle);

    public void setLinkTitle(String linkTitle);

    public String getLinkTitle();

    /**
     *
     * @return display this text on a mouseover hover on the entry point.
     */
    public String getMouseoverEntryPointText();

    public void setMouseoverEntryPointText(String mouseoverEntryPointText);

    /**
     * @return Explanatory text about what the purpose of this flow is.
     */
    public String getFlowDescriptionText();

    public void setFlowDescriptionText(String flowDescriptionText);

    public void setPageName(String pageName);

    public String getPageName();

    public void setDefaultAfterPage(String defaultAfterPage);

    public String getDefaultAfterPage();

    /**
     * retrieve the activity, and execute it's {@link FlowActivity#refresh()} method.
     */
    public void refresh();

    public void setFlowState(FlowState state);

    public FlowState getFlowState();

    public int indexOf(FlowActivity activity);

    public void setActivatable(boolean activatable);

    public boolean isActivatable();

    public void setNotCurrentAllowed(boolean notCurrentAllowed);

    /**
     * This flow doesn't have to be the current flow in order to be active.
     * @return false means this flowStates of this type should be dropped if they are
     * no longer the current flow.
     */
    public boolean isNotCurrentAllowed();

    public String toString();

}