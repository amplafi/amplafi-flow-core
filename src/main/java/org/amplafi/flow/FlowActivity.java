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

import java.util.Map;

/*
 * @author patmoore
 */
public interface FlowActivity {
    /**
     * Used to control which FlowActivities a user may select. This is used to
     * prevent the user from jumping ahead in a flow.
     *
     * @return true if this FlowActivity can be activated by the user.
     */
    public boolean isActivatable();
    /**
     * called when a flow is started. Subclasses should override this method to
     * initialize the FlowState with default values if needed. <p/> The
     * FlowState's initialState has been set. So all properties should be
     * checked first to see if they have already been set. <p/> No database
     * modifications should occur in this method.
     */
    public void initializeFlow();

    /**
     * subclass should override this to perform some action when the
     * FlowActivity is activated. But should still call this method.
     *
     * this method checks to see if the flowActivity is invisible or if
     * {@link FlowConstants#FSAUTO_COMPLETE} = true and
     * {@link #getFlowValidationResult()} {@link FlowValidationResult#isValid()}
     * = true.
     * @param flowStepDirection TODO
     *
     * @return if true, immediately complete this FlowActivity.
     */
    public boolean activate(FlowStepDirection flowStepDirection);

    /**
     * Passivate this flow activity. This method is invoked whenever the flow
     * activity is submitted (i.e. next, previous or finish is clicked on the
     * flow). Override this method to create the objects, or update objects as
     * needed when leaving this flow activity. TODO: I don't see this getting
     * called on PREVIOUS, shouldn't it?
     *
     * @param verifyValues verify the current values that the FlowActivity is
     *        concerned about.
     * @param flowStepDirection TODO
     * @return the {@link FlowValidationResult} if verifyValues is true otherwise an empty {@link FlowValidationResult} object.
     */
    public FlowValidationResult passivate(boolean verifyValues, FlowStepDirection flowStepDirection);

    /**
     * called when changes accumulated to flow properties should be saved
     * permanently. Override this method to perform database updates.
     */
    public void saveChanges();

    /**
     * called when the FlowActivity is marked as a Flow's finishingActivity.
     * <p/> called after all FlowActivities' {@link #saveChanges()} have been
     * called.
     * @param currentNextFlowState TODO
     *
     * @return the next FlowState that is now the current FlowState.
     */
    public FlowState finishFlow(FlowState currentNextFlowState);

    public Flow getFlow();

    /**
     * @param pageName The pageName to set.
     */
    public void setPageName(String pageName);

    /**
     * @return the pageName.
     */
    public String getPageName();

    public String getComponentName();

    public void setComponentName(String componentName);

    /**
     * can the current FlowActivity's completeActivity() method be called.
     *
     * @return result of validation
     */
    @Deprecated
    public FlowValidationResult getFlowValidationResult();

    /**
     * Determines if the flow passes validation for a specific level of required properties.
     * @param required
     * @param flowStepDirection
     *
     * @return result of validation
     */
    public FlowValidationResult getFlowValidationResult(PropertyRequired required, FlowStepDirection flowStepDirection);

    /**
     * @param activityName The activityName to set.
     */
    public void setActivityName(String activityName);

    /**
     * @return Returns the activityName.
     */
    public String getActivityName();

    /**
     * @param activityTitle The flowTitle to set.
     */
    public void setActivityTitle(String activityTitle);

    /**
     * @return Returns the flowTitle.
     */
    public String getActivityTitle();

    /**
     * "flowName.activityName"
     *
     * @return full flow activity name.
     */
    public String getFullActivityName();

    /**
     * @param activatable true if this flowActivity can be selected from the UI.
     */
    public void setActivatable(boolean activatable);

    /**
     * @param finishedActivity The user can finish the flow when this activity
     *        is current.
     */
    public void setFinishingActivity(boolean finishedActivity);

    public FlowManagement getFlowManagement();

    public FlowState getFlowState();

    public Map<String, FlowPropertyDefinition> getPropertyDefinitions();

    public boolean isInstance();

    public FlowActivity initInvisible();

    public void setInvisible(boolean invisible);

    public boolean isInvisible();

    public void setPersistFlow(boolean persistFlow);

    public boolean isPersistFlow();

    public FlowPropertyDefinition getPropertyDefinition(String key);

    public void setFlow(Flow flow);

    public int getIndex();
    public String getString(String key);
    /**
     * override to treat some properties as special. This method is called by
     * FlowPropertyBinding.
     *
     * @param key
     * @param <T> type of property.
     * @return property
     */
    public <T> T getProperty(String key);

    /**
     * Convert dataClass to a string using
     * {@link FlowUtils#toPropertyName(Class)} and use that string to look up
     * the property.
     *
     * @param <T> type of property
     * @param dataClass type of property
     * @return the value converted to dataClass.
     */
    public <T> T getProperty(Class<T> dataClass);

    /**
     * override to treat some properties as special. This method is called by
     * FlowPropertyBinding. Default behavior caches value and sets the property
     * to value.
     *
     * @param key
     * @param value
     * @param <T> value's type
     */
    public <T> void setProperty(String key, T value);

    /**
     * Convert value.getClass() to string using
     * {@link FlowUtils#toPropertyName(Class)} and use that string as the
     * property name for the value being set.
     *
     * @param <T>
     * @param value must not be null
     */
    public <T> void setProperty(T value);

    /**
     * Convert value.getClass() to string using
     * {@link FlowUtils#toPropertyName(Class)} and use that string as the
     * property name for the value being set.
     *
     * @param <T> value's type
     * @param value may be null
     * @param dataClass
     */
    public <T> void setProperty(Class<? extends T> dataClass, T value);

    /**
     * Called before a flow is {@link #passivate(boolean, FlowStepDirection)} also called if the flow is not
     * advancing but wants to inform the current FlowActivity that all the
     * values from the UI have been refreshed so the FlowActivity can do any
     * in-place updates. This can be used for the rare case when it is desireable
     * to immediately commit a change to the database.
     */
    public void refresh();
    public boolean isFinishingActivity();
    public boolean isPropertyNotSet(String key);

    public boolean isPropertySet(String key);

    public boolean isPropertyNotBlank(String key);

    public boolean isPropertyBlank(String key);

}