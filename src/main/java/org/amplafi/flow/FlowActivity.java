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

import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;
import org.amplafi.flow.validation.FlowValidationResult;

/**
 * defines one activity in the flow. This can be a definition of an activity or
 * the actual activity depending on the state of the Flow parent object.
 * FlowActivity objects may be part of multiple definitions or multiple
 * instances (but not both instances and definitions).
 *
 * Used when multiple properties need to interact with each other.
 *
 * typical use case is when combining multiple properties to create a database object.
 *
 * Every {@link Flow} must have at least 1 {@link FlowActivity}
 *
 * FlowActivities must be stateless. FlowActivity instances can be reused
 * between different users and different {@link org.amplafi.flow.Flow}s.
 *
 * <p> Lifecycle methods:</p>
 * <ol>
 * <li>{@link #initializeFlow()} - used to initialize the FlowState with any
 * defaults for missing values. <b>No</b> modifications should occur in this
 * method.</li>
 * <li>{@link #activate(FlowStepDirection)} - called each time the FlowActivity is made the
 * current FlowActivity. Returns true if the Flow should immediately advance to
 * the next FlowActivity. If this is the last FlowActivity, then the Flow
 * completes.
 *
 * <b>No</b> database modifications should occur in this method.</li>
 * <li>{@link #passivate(boolean, FlowStepDirection)} - called each time the FlowActivity was the
 * current FlowActivity and is now no longer the current FlowActivity. Used to
 * validate input as needed. <b>No</b> modifications should occur in this
 * method.</li>
 * <li>{@link #saveChanges()} - called when the flow is completing. <i>Only
 * place where db modifications can be made.</i> This allows canceling the flow
 * to meaningfully revert all changes.</li>
 * <li>{@link #finishFlow(org.amplafi.flow.FlowState)} - called when the flow is finishing.</li>
 * </ol>
 * <p> This structure is in place so that FlowActivities that create
 * relationships are not put into the position of having to be aware of the
 * surrounding Flow and previously created objects. Nor are they aware of the
 * state of the flow.</p>
 * <p>
 * By convention, FlowActivies are expected to be in a
 * 'flows' package and the FlowActivity subclass' name ends with 'FlowActivity'.
 * </p><p>
 * If a FlowActivity is a visible step then the FlowActivity needs a
 * component. The default component type is the grandparent package + the
 * FlowActivity class name with 'FlowActivity' stripped off. For example,
 * fuzzy.flows.FooBarFlowActivity would have a default component of
 * 'fuzzy/FooBar'.
 *
 * TODO handle return to previous flow issues.
 *
 *
 * @author patmoore
 */
public interface FlowActivity extends FlowPropertyProviderWithValues, FlowProvider {
    /**
     * Used to control which FlowActivities a user may select. This is used to
     * prevent the user from jumping ahead in a flow.
     *
     * @return true if this FlowActivity can be activated by the user.
     */
    boolean isActivatable();
    /**
     * called when a flow is started. Subclasses should override this method to
     * initialize the FlowState with default values if needed. <p/> The
     * FlowState's initialState has been set. So all properties should be
     * checked first to see if they have already been set. <p/> No database
     * modifications should occur in this method.
     */
    void initializeFlow();

    /**
     * subclass should override this to perform some action when the
     * FlowActivity is activated. But should still call this method.
     *
     * this method checks to see if the flowActivity is invisible or if
     * {@link FlowConstants#FSAUTO_COMPLETE} = true and
     * {@link #getFlowValidationResult()} {@link FlowValidationResult#isValid()}
     * = true.
     * @param flowStepDirection the direction the flow is be advanced ( forward, backward, or in place refresh )
     *
     * @return if true, this FlowActivity indicates that the 'next' activity in the flowStepDirection
     * should be activated. One example are invisible activities. false should only be returned for FlowActivities that have a UI component
     */
    boolean activate(FlowStepDirection flowStepDirection);
    /**
     * Called before a flow is {@link #passivate(boolean, FlowStepDirection)} also called if the flow is not
     * advancing but wants to inform the current FlowActivity that all the
     * values from the UI have been refreshed so the FlowActivity can do any
     * in-place updates. This can be used for the rare case when it is desirable
     * to immediately commit a change to the database.
     */
    void refresh();
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
    FlowValidationResult passivate(boolean verifyValues, FlowStepDirection flowStepDirection);

    /**
     * called when changes accumulated to flow properties should be saved
     * permanently. Override this method to perform database updates.
     */
    void saveChanges();

    /**
     * called when the FlowActivity is marked as a Flow's finishingActivity.
     * <p/> called after all FlowActivities' {@link #saveChanges()} have been
     * called.
     * @param currentNextFlowState TODO
     *
     * @return the next FlowState that is now the current FlowState.
     */
    FlowState finishFlow(FlowState currentNextFlowState);


    FlowValidationResult getFlowValidationResult();
    /**
     * Determines if the flow passes validation for a specific level of required properties.
     * @param required
     * @param flowStepDirection
     *
     * @return result of validation
     */
    FlowValidationResult getFlowValidationResult(FlowActivityPhase required, FlowStepDirection flowStepDirection);

    // above is workflow-ish
    // below is less-so or not at all.

    /**
     * @return the pageName.
     */
    @Deprecated
    String getPageName();

    @Deprecated
    String getComponentName();
    /**
     *
     * @return true if the activity name has been explicitly set.
     */
    boolean isFlowPropertyProviderNameSet();

    /**
     * @return Returns the flowTitle.
     */
    String getActivityTitle();

    /**
     * Namespace used to store activityLocal properties when running a flow.
     * Currently "flowState.lookupKey"."activityName"
     * @return namespace for activity.
     */
    String getFullActivityInstanceNamespace();

    boolean isInstance();

    /**
     * Checks the current state before using {@link #isPossiblyVisible()} as a default.
     * @return true if the FlowActivity is currently invisible.
     */
    boolean isInvisible();
    /**
     * static check to see if the FlowActivity could be visible ( has a componentName defined )
     * @return true if the FlowActivity could be visible (
     */
    boolean isPossiblyVisible();

    int getIndex();
    String getString(String key);

    /**
     * TODO: see about using this more -- allows disconnecting names from properties ..
     * unintended side-effects?
     * Convert dataClass to a string using
     * {@link org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder#toPropertyName(Class)} and use that string to look up
     * the property.
     *
     * @param <T> type of property
     * @param dataClass type of property
     * @return the value converted to dataClass.
     */
    @Override
    <T> T getProperty(Class<? extends T> dataClass);

    boolean isFinishingActivity();

    /**
     * Handles all the various namespaces that FlowActivity may be referenced by.
     * @param possibleName
     * @return true if the activity can "go by" the supplied name
     */
    boolean isNamed(String possibleName);
}