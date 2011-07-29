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

import java.io.Serializable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.FlowValidationResult;



/**
 * Implementations hold the state of an active Flow. Implementers should expect
 * to be stored in a Session and should be serializable.
 */
public interface FlowState extends ListIterator<FlowActivity>, Serializable, Iterable<FlowActivity>, FlowPropertyProviderWithValues, FlowProvider, FlowStateProvider {

    /**
     * Copy all Flow-level {@link org.amplafi.flow.FlowPropertyDefinition}'s initial values to the flowState's key value map.
     * Call each {@link FlowActivity#initializeFlow()}.
     *
     * used when the keys of the map may need to have a namespace assigned.
     * For example, a key is supplied that represents a flowLocal property as defined by the FlowPropertyDefinition.
     *
     * TODO the equivalent for activityLocal properties.
     */
    public void initializeFlow();
    /**
     * Start the FlowState. Call each FlowActivity's initializeFlow(). Then
     * starting with the first FlowActivity, activate the FlowActivity by
     * calling {@link FlowActivity#activate(FlowStepDirection)} until reaching a
     * {@link FlowActivity} that returns false or the flow has been completed.
     *
     * @return the page to be displayed after this flow has completed its
     *         initialization.
     */
    String begin();

    /**
     * If this flow is not completed then we resume this flow by calling the
     * {@link #getCurrentActivity()}'s {@link FlowActivity#activate(FlowStepDirection)}. If that
     * method returns true, we then advance through the flow. If the Flow has
     * not been started then resume() calls {@link #begin()}
     *
     * @return pageName of page to be displayed.
     */
    String resume();

    /**
     * Morphs the flowState to the new flow. In Morphing the FlowState has a new {@link Flow} definition.
     * The current flowState values are preserved and the flow definition attached to the flowState is changed to the new definition.
     * A FlowActivity with the same name as the current FlowActivity is looked for. If such a flow activity is found then that {@link FlowActivity}
     * will be the current activity of the morphed flow. Otherwise the morphing code searches for all previously executed
     * FlowActivities of the current flow and the morphed flow will be attempted to be at the first FlowActivity of the new Flow that is
     * "after" all "previous" FlowActivies with the same names as in the original Flow.
     *
     * The {@link FlowActivity}s in first flow and
     * second flow must be in order with regards to the FlowActivity names.
     *
     *
     * @param flowTypeName the flow to which flowstate should morph
     * @param initialFlowState
     * @return The name of the page that should be presented (or null).
     */
    String morphFlow(String flowTypeName, Map<String, String> initialFlowState);

    /**
     * Move to the specified activity.
     * <p/>
     * Implementations should activate the given activity and then should
     * keep on advancing to the activities that follow so long as the current one
     * returns true from beginActivity().
     * <p/>
     * This method can also be used to move to previous activities.
     * @param <T>
     *
     * @param newActivity the index of the activity to move to.
     * @param verifyValues if true, verifies the values for each {@link FlowActivity} examined.
     *
     * @return the now current {@link FlowActivity}
     * @throws FlowValidationException validation of the flow state data failed
     */
    <T extends FlowActivity> T selectActivity(int newActivity, boolean verifyValues) throws FlowValidationException;

    /**
     * Move to the specified visible activity.
     * @param <T>
     *
     * @param visibleIndex The index of the activity among the visible ones.
     * @return the now current FlowActivity
     * @throws FlowValidationException validation of the flow state data failed
     */
    <T extends FlowActivity> T selectVisibleActivity(int visibleIndex)  throws FlowValidationException;

    /**
     * Usually called as part of the {@link #finishFlow()} processing. Maybe called earlier if the business logic decides that some changes need to be committed immediately.
     */
    void saveChanges();

    /**
     * Completes this flow successfully. Calls {@link #saveChanges()} and then
     * completes the flow.
     *
     * @return The name of the page that should be presented (or null).
     * @throws FlowValidationException validation of the flow state data failed
     */
    String finishFlow() throws FlowValidationException;

    /**
     *
     * @return The name of the page that should be presented (or null).
     */
    String cancelFlow();

    /**
     * return {@link #getAfterPage()} if the flow is complete.
     *
     * @return the page name that should be displayed at this point
     */
    String getCurrentPage();

    String getActiveFlowLabel();

    /**
     * get FlowActivity by the {@link FlowActivity#getFlowPropertyProviderName()}
     * @param <T>
     * @param activityName
     * @return flowActivity
     */
    <T extends FlowActivity> T getActivity(String activityName);

    /**
     * @return Returns the flowId.
     */
    String getLookupKey();

    /**
     *
     * @param key
     * @return true if key when converted to a string matches
     *         {@link #getLookupKey()}.
     */
    boolean hasLookupKey(Object key);

    <T extends FlowActivity> T getCurrentActivity();

    void setCurrentActivityByName(String currentActivityByName);

    /**
     * @return the current activitiy's name.
     */
    String getCurrentActivityByName();

    int size();

    /**
     * @return Returns the currentActivity.
     */
    int getCurrentActivityIndex();

    boolean hasProperty(String key);

    <T extends FlowActivity> List<T> getActivities();

    <T extends FlowActivity> List<T> getVisibleActivities();

    /**
     * can this flow be ended.
     *
     * @return true if the flow can end normally.
     */
    boolean isFinishable();

    /**
     *
     * @return the flow title or the flow link title.
     */
    String getFlowTitle();

    /**
     * @return the flowManagement.
     */
    FlowManagement getFlowManagement();

    String getFlowTypeName();

    boolean isTrue(String key);

    Boolean getBoolean(String key);

    Long getLong(String key);

    /**
     * can the current activity be passivated.
     *
     * @return result returned by the currentActivity's
     *         {@link FlowActivity#getFlowValidationResult()}
     */
    FlowValidationResult getCurrentActivityFlowValidationResult();

    /**
     * @param flowActivityPhase
     * @param flowStepDirection
     * @return result returned by the currentActivity's
     *         {@link FlowActivity#getFlowValidationResult(FlowActivityPhase, FlowStepDirection)}
     */
    FlowValidationResult getCurrentActivityFlowValidationResult(FlowActivityPhase flowActivityPhase, FlowStepDirection flowStepDirection);

    Map<String, FlowValidationResult> getFlowValidationResults(FlowActivityPhase flowActivityPhase, FlowStepDirection flowStepDirection);


    /**
     * @return the combination of {@link #getCurrentActivityFlowValidationResult()} and
     *  {@link #getFullFlowValidationResult(FlowActivityPhase, FlowStepDirection)}({@link FlowActivityPhase#finish}, {@link FlowStepDirection#forward})
     */
    FlowValidationResult getFinishFlowValidationResult();
    FlowValidationResult getFullFlowValidationResult(FlowActivityPhase flowActivityPhase, FlowStepDirection flowStepDirection);

    /**
     *
     * @return the page the UI should be on after this flow completes
     */
    String getAfterPage();

    boolean isUpdatePossible();

    String getUpdateText();

    boolean isCancelPossible();

    String getCancelText();

    String getFinishText();


    /**
     * Set this to a not-null value to indicate that an alternative button has
     * triggered the finish of the flow.
     *
     * @param type
     */
    void setFinishKey(String type);

    /**
     * @return Null if this flow was finished by the normal button. Anything
     *         else means that an alternative button has triggered the finish.
     */
    String getFinishKey();

    FlowStateLifecycle getFlowStateLifecycle();

    boolean isCompleted();

    boolean isActive();

    void setDefaultAfterPage(String pageName);

    String getDefaultAfterPage();

    boolean isNotCurrentAllowed();

    FlowValuesMap getFlowValuesMap();

    boolean hasVisibleNext();

    boolean hasVisiblePrevious();

    /**
     * Passivates the current flow activity.
     *
     * @param verifyValues if true, verifies the values that the current flow activity
     * is interested in.
     * @param flowStepDirection TODO
     * @return result from {@link FlowActivity#passivate(boolean, FlowStepDirection)}
     */
    FlowValidationResult passivate(boolean verifyValues, FlowStepDirection flowStepDirection);

    /**
     * Used when continuing on to another FlowState. Clear out all the properties that should not be seen by downstream flows.
     * @return a FlowValuesMap with all the flowLocal, activityLocal values cleared.
     */
    FlowValuesMap getExportedValuesMap();


    /**
     * @param possibleReferencedState
     * @return true if this flowState references possibleReferencedState
     */
    boolean isReferencing(FlowState possibleReferencedState);
    /**
     * Set a map of values. This allows proper handling of namespace issues and cache invalidation that does not happen
     * if the FlowState's map is modified directly.
     * @param map
     */
    public void setAllProperties(Map<?,?> map);
    /**
     * TO_TIRIS : comment this please
     * @return
     */
	public boolean isPersisted();
}