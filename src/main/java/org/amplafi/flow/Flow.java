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

import org.amplafi.flow.flowproperty.FlowPropertyProvider;

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
public interface Flow extends FlowPropertyProvider, FlowProvider {

    /**
     * @param <T>
     * @return the activities.
     */
    <T extends FlowActivity> List<T> getActivities();

    <T extends FlowActivity> T getActivity(int activityIndex);

    /**
     * @return Returns the definition.
     */
    boolean isInstance();

    <T extends FlowActivity> List<T> getVisibleActivities();

    /**
     * @return get the flow name as it should appear in the flowentry and the
     *         titlebar.
     */
    String getFlowTitle();

    /**
     * @return Used if this is a secondary flow that will be started as the next
     *         flow.
     */
    String getContinueFlowTitle();

    String getLinkTitle();

    /**
     *
     * @return display this text on a mouseover hover on the entry point.
     */
    String getMouseoverEntryPointText();

    /**
     * @return Explanatory text about what the purpose of this flow is.
     */
    String getFlowDescriptionText();

    String getPageName();

    String getDefaultAfterPage();

    /**
     * retrieve the activity, and execute it's {@link FlowActivity#refresh()} method.
     */
    void refresh();

    int indexOf(FlowActivity activity);

    boolean isActivatable();

}