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
    public String begin();

    /**
     * If this flow is not completed then we resume this flow by calling the
     * {@link #getCurrentActivity()}'s {@link FlowActivity#activate(FlowStepDirection)}. If that
     * method returns true, we then advance through the flow. If the Flow has
     * not been started then resume() calls {@link #begin()}
     *
     * @return pageName of page to be displayed.
     */
    public String resume();

    public void initializeFlow(Iterable<FlowPropertyDefinition> flowPropertyDefinitions);

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
    public String morphFlow(String flowTypeName, Map<String, String> initialFlowState);

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
    public <T extends FlowActivity> T selectActivity(int newActivity, boolean verifyValues);

    /**
     * Move to the specified visible activity.
     * @param <T>
     *
     * @param visibleIndex The index of the activity among the visible ones.
     * @return the now current FlowActivity
     */
    public <T extends FlowActivity> T selectVisibleActivity(int visibleIndex);

    public void saveChanges();

    /**
     * Completes this flow successfully. Calls {@link #saveChanges()} and then
     * completes the flow.
     *
     * @return The name of the page that should be presented (or null).
     */
    public String finishFlow();

    /**
     *
     * @return The name of the page that should be presented (or null).
     */
    public String cancelFlow();

    /**
     * return {@link #getAfterPage()} if the flow is complete.
     *
     * @return the page name that should be displayed at this point
     */
    public String getCurrentPage();

    public void setActiveFlowLabel(String activeFlowLabel);

    public String getActiveFlowLabel();

    public <T extends FlowActivity> T getActivity(int activityIndex);

    public <T extends FlowActivity> T getActivity(String activityName);

    /**
     * @return Returns the flowId.
     */
    public String getLookupKey();

    /**
     *
     * @param key
     * @return true if key when converted to a string matches
     *         {@link #getLookupKey()}.
     */
    public boolean hasLookupKey(Object key);

    public <T extends FlowActivity> T getCurrentActivity();

    public void setCurrentActivityByName(String currentActivityByName);

    /**
     * @return the current activitiy's name.
     */
    public String getCurrentActivityByName();

    public int size();

    /**
     * @return Returns the currentActivity.
     */
    public int getCurrentActivityIndex();

    public String getRawProperty(String key);

    public String getProperty(String flowActivityName, String key);

    /**
     *
     * @param flowActivityName
     * @param key
     * @param value
     * @return true if the value has changed.
     */
    public boolean setProperty(String flowActivityName, String key, String value);

    /**
     *
     * @param flowActivity
     * @param propertyDefinition
     * @param value
     * @return true if the value has changed.
     */
    public boolean setRawProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition, String value);

    /**
     *
     * @param key
     * @param value
     * @return true if the value has changed.
     */
    public boolean setProperty(String key, String value);

    public boolean hasProperty(String key);

    public <T extends FlowActivity> List<T> getActivities();

    public <T extends FlowActivity> List<T> getVisibleActivities();

    /**
     * can this flow be ended.
     *
     * @return true if the flow can end normally.
     */
    public boolean isFinishable();

    /**
     * Called when this flowstate no longer represents the current flow. Assume
     * that this FlowState may be G.C.'ed
     */
    public void clearCache();

    /**
     *
     * @return the flow title or the flow link title.
     */
    public String getFlowTitle();

    public void setCached(String key, Object value);

    public <T> T getCached(String key);

    /**
     * @param flowManagement The flowManagement to set.
     */
    public void setFlowManagement(FlowManagement flowManagement);

    /**
     * @return Returns the flowManagement.
     */
    public FlowManagement getFlowManagement();

    public void setFlowTypeName(String flowTypeName);

    public String getFlowTypeName();

    public Flow getFlow();

    public String makeCurrent();

    public boolean isTrue(String key);

    public Boolean getBoolean(String key);

    public Long getLong(String key);

    /**
     * can the current activity be completed.
     *
     * @return result returned by the currentActivity's
     *         {@link FlowActivity#getFlowValidationResult()}
     */
    public FlowValidationResult getCurrentActivityFlowValidationResult();

    public boolean isCurrentActivityCompletable();

    public Map<String, FlowValidationResult> getFlowValidationResults();

    public void setAfterPage(String afterPage);

    public String getAfterPage();

    public boolean isUpdatePossible();

    public String getUpdateText();

    public boolean isCancelPossible();

    public String getCancelText();

    public void setCancelText(String cancelText);

    public String getFinishText();

    public void setFinishText(String finishText);

    /**
     * Set this to a not-null value to indicate that an alternative button has
     * triggered the finish of the flow.
     *
     * @param type
     */
    public void setFinishType(String type);

    /**
     * @return Null if this flow was finished by the normal button. Anything
     *         else means that an alternative button has triggered the finish.
     */
    public String getFinishType();

    public void setFlowLifecycleState(FlowLifecycleState flowLifecycleState);

    public FlowLifecycleState getFlowLifecycleState();

    public boolean isCompleted();

    /**
     *
     * @param <T>
     * @param key
     * @param expected class maybe null if not known.
     * @return the property value
     */
    public <T> T getPropertyAsObject(String key, Class<T> expected);
    public String getPropertyAsObject(String key);
    public <T> void setPropertyAsObject(String key, T value);

    public boolean isActive();

    public void setDefaultAfterPage(String pageName);

    public String getDefaultAfterPage();

    public boolean isNotCurrentAllowed();

    public FlowValuesMap getFlowValuesMap();

    public void setFlowValuesMap(FlowValuesMap flowValuesMap);

    public Log getLog();

    public String getRawProperty(FlowActivity flowActivity, String key);

    public <T> void setProperty(FlowActivity flowActivity,
            FlowPropertyDefinition propertyDefinition, T value);

    public <T> T getProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition);

    public Long getRawLong(FlowActivity flowActivity, String key);

    public <T> FlowPropertyDefinition createFlowPropertyDefinition(String key,
            Class<T> expected, T sampleValue);

    public boolean hasVisibleNext();

    public boolean hasVisiblePrevious();

    /**
     * Passivates the current flow activity.
     *
     * @param verifyValues if true, verifies the values that the current flow activity
     * is interested in.
     * @param flowStepDirection TODO
     * @return result from {@link FlowActivity#passivate(boolean, FlowStepDirection)}
     */
    public FlowValidationResult passivate(boolean verifyValues, FlowStepDirection flowStepDirection);

    public <T> FlowPropertyDefinition getFlowPropertyDefinition(String key);

    /**
     * @return a FlowValuesMap with all the flowLocal, activityLocal values cleared.
     */
    public FlowValuesMap getClearFlowValuesMap();

    /**
     * @param possibleReferencedState
     * @return true if this flowState references possibleReferencedState
     */
    public boolean isReferencing(FlowState possibleReferencedState);

}