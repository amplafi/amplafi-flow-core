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

import org.apache.commons.logging.Log;


/**
 * Implementations hold the state of an active Flow. Implementers should expect
 * to be stored in a Session and should be serializable.
 */
public interface FlowState extends ListIterator<FlowActivity>, Serializable, Iterable<FlowActivity> {

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

    void initializeFlow(Iterable<FlowPropertyDefinition> flowPropertyDefinitions);

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
     */
    <T extends FlowActivity> T selectActivity(int newActivity, boolean verifyValues);

    /**
     * Move to the specified visible activity.
     * @param <T>
     *
     * @param visibleIndex The index of the activity among the visible ones.
     * @return the now current FlowActivity
     */
    <T extends FlowActivity> T selectVisibleActivity(int visibleIndex);

    void saveChanges();

    /**
     * Completes this flow successfully. Calls {@link #saveChanges()} and then
     * completes the flow.
     *
     * @return The name of the page that should be presented (or null).
     */
    String finishFlow();

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

    void setActiveFlowLabel(String activeFlowLabel);

    String getActiveFlowLabel();

    <T extends FlowActivity> T getActivity(int activityIndex);

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

    String getRawProperty(String key);

    String getProperty(String flowActivityName, String key);

    /**
     *
     * @param flowActivityName
     * @param key
     * @param value
     * @return true if the value has changed.
     */
    boolean setProperty(String flowActivityName, String key, String value);

    /**
     *
     * @param flowActivity
     * @param propertyDefinition
     * @param value
     * @return true if the value has changed.
     */
    boolean setRawProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition, String value);

    /**
     *
     * @param key
     * @param value
     * @return true if the value has changed.
     */
    boolean setProperty(String key, String value);

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
     * Called when this flowstate no longer represents the current flow. Assume
     * that this FlowState may be G.C.'ed
     */
    void clearCache();

    /**
     *
     * @return the flow title or the flow link title.
     */
    String getFlowTitle();

    void setCached(String key, Object value);

    <T> T getCached(String key);

    /**
     * @param flowManagement The flowManagement to set.
     */
    void setFlowManagement(FlowManagement flowManagement);

    /**
     * @return Returns the flowManagement.
     */
    FlowManagement getFlowManagement();

    void setFlowTypeName(String flowTypeName);

    String getFlowTypeName();

    Flow getFlow();

    String makeCurrent();

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
     * @param propertyRequired
     * @return result returned by the currentActivity's
     *         {@link FlowActivity#getFlowValidationResult(PropertyRequired, FlowStepDirection)}
     */
    FlowValidationResult getCurrentActivityFlowValidationResult(PropertyRequired propertyRequired, FlowStepDirection flowStepDirection);

    boolean isCurrentActivityCompletable();

    Map<String, FlowValidationResult> getFlowValidationResults(PropertyRequired propertyRequired, FlowStepDirection flowStepDirection);


    /**
     * @return the combination of {@link #getCurrentActivityFlowValidationResult()} and
     *  {@link #getFullFlowValidationResult(PropertyRequired, FlowStepDirection)}({@link PropertyRequired#finish}, {@link FlowStepDirection#forward})
     */
    FlowValidationResult getFinishFlowValidationResult();
    FlowValidationResult getFullFlowValidationResult(PropertyRequired propertyRequired, FlowStepDirection flowStepDirection);
    void setAfterPage(String afterPage);

    String getAfterPage();

    boolean isUpdatePossible();

    String getUpdateText();

    boolean isCancelPossible();

    String getCancelText();

    void setCancelText(String cancelText);

    String getFinishText();

    void setFinishText(String finishText);

    /**
     * Set this to a not-null value to indicate that an alternative button has
     * triggered the finish of the flow.
     *
     * @param type
     */
    void setFinishType(String type);

    /**
     * @return Null if this flow was finished by the normal button. Anything
     *         else means that an alternative button has triggered the finish.
     */
    String getFinishType();

    void setFlowLifecycleState(FlowLifecycleState flowLifecycleState);

    FlowLifecycleState getFlowLifecycleState();

    boolean isCompleted();

    /**
     *
     * @param <T>
     * @param key
     * @param expected class maybe null if not known.
     * @return the property value
     */
    <T> T getPropertyAsObject(String key, Class<T> expected);
    String getPropertyAsObject(String key);
    <T> void setPropertyAsObject(String key, T value);

    boolean isActive();

    void setDefaultAfterPage(String pageName);

    String getDefaultAfterPage();

    boolean isNotCurrentAllowed();

    FlowValuesMap getFlowValuesMap();

    void setFlowValuesMap(FlowValuesMap flowValuesMap);

    Log getLog();

    String getRawProperty(FlowActivity flowActivity, String key);

    <T> void setProperty(FlowActivity flowActivity,
            FlowPropertyDefinition propertyDefinition, T value);

    <T> T getProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition);

    Long getRawLong(FlowActivity flowActivity, String key);

    <T> FlowPropertyDefinition createFlowPropertyDefinition(String key,
            Class<T> expected, T sampleValue);

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

    <T> FlowPropertyDefinition getFlowPropertyDefinition(String key);

    /**
     * @return a FlowValuesMap with all the flowLocal, activityLocal values cleared.
     */
    FlowValuesMap getClearFlowValuesMap();

    /**
     * @param possibleReferencedState
     * @return true if this flowState references possibleReferencedState
     */
    boolean isReferencing(FlowState possibleReferencedState);
}