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

package org.amplafi.flow.impl;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowTransition;
import org.amplafi.flow.FlowUtils;
import org.amplafi.flow.flowproperty.CancelTextFlowPropertyValueProvider;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.MessageFlowPropertyValueProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import static org.amplafi.flow.FlowConstants.*;
import static org.amplafi.flow.flowproperty.PropertyScope.*;
import static org.amplafi.flow.flowproperty.PropertyUsage.*;


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
public class FlowImpl extends BaseFlowPropertyProvider<FlowImplementor> implements Serializable, Cloneable, FlowImplementor, Iterable<FlowActivityImplementor> {

    private static final long serialVersionUID = -985306244948511836L;

    private List<FlowActivityImplementor> activities;

    private String flowTitle;
    private String continueFlowTitle;
    private String linkTitle;
    private String pageName;
    private String defaultAfterPage;

    private String mouseoverEntryPointText;

    private String flowDescriptionText;

    private FlowState flowState;

    private boolean activatable;

    /**
     * False means {@link FlowState}s don't need to be current. True means that if a FlowState of this
     * Flow type is no longer the current flow (after being the current flow), the FlowState
     * is dropped.
     */
    private boolean notCurrentAllowed;

    /**
     * Used to restore an existing definition or create an new definitions from XML
     */
    public FlowImpl() {
        // see #2179 #2192
        this.addPropertyDefinitions(
            new FlowPropertyDefinitionImpl(FSTITLE_TEXT).initAccess(flowLocal, use),
            new FlowPropertyDefinitionImpl(FSCANCEL_TEXT).initAccess(flowLocal, use).initFlowPropertyValueProvider(CancelTextFlowPropertyValueProvider.INSTANCE),
            new FlowPropertyDefinitionImpl(FSNO_CANCEL, boolean.class).initAccess(flowLocal, use),
            new FlowPropertyDefinitionImpl(FSFINISH_TEXT).initAccess(flowLocal, use).initFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
            new FlowPropertyDefinitionImpl(FSRETURN_TO_TEXT).initAccess(flowLocal, use).initFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
            // io -- for now because need to communicate the next page to be displayed
            // TODO think about PropertyScope/PropertyUsage
            new FlowPropertyDefinitionImpl(FSPAGE_NAME).initPropertyUsage(io),
            // TODO think about PropertyScope/PropertyUsage
            new FlowPropertyDefinitionImpl(FSAFTER_PAGE).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSDEFAULT_AFTER_PAGE).initAccess(flowLocal, internalState),
            new FlowPropertyDefinitionImpl(FSDEFAULT_AFTER_CANCEL_PAGE).initAccess(flowLocal, internalState),
            new FlowPropertyDefinitionImpl(FSHIDE_FLOW_CONTROL, boolean.class).initPropertyScope(flowLocal),
            new FlowPropertyDefinitionImpl(FSACTIVATABLE, boolean.class).initAccess(flowLocal, internalState),
            new FlowPropertyDefinitionImpl(FSIMMEDIATE_SAVE, boolean.class).initAccess(flowLocal, internalState),

            new FlowPropertyDefinitionImpl(FSAPI_CALL, boolean.class).initAccess(flowLocal, io),
            new FlowPropertyDefinitionImpl(FSAUTO_COMPLETE, boolean.class).initAccess(flowLocal, internalState),
            new FlowPropertyDefinitionImpl(FSALT_FINISHED).initAccess(flowLocal, use),
            new FlowPropertyDefinitionImpl(FSREDIRECT_URL, URI.class).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSREFERRING_URL, URI.class).initPropertyUsage(use),
            new FlowPropertyDefinitionImpl(FSCONTINUE_WITH_FLOW).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSFLOW_TRANSITIONS, FlowTransition.class, Map.class).initAutoCreate().initAccess(flowLocal, use),
            new FlowPropertyDefinitionImpl(FSFLOW_TRANSITION, FlowTransition.class).initAccess(flowLocal, initialize),

            new FlowPropertyDefinitionImpl(FSRETURN_TO_FLOW).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSSUGGESTED_NEXT_FLOW_TYPE, FlowTransition.class, Map.class).initAutoCreate().initAccess(flowLocal, use),
            // TODO think about PropertyScope/PropertyUsage
            new FlowPropertyDefinitionImpl(FSNEXT_FLOW).initPropertyUsage(io)

        );
    }

    /**
     * creates a instance Flow from a definition.
     * @param definition
     */
    public FlowImpl(FlowImplementor definition) {
        super(definition);
        this.flowPropertyProviderName = definition.getFlowPropertyProviderName();
    }
    /**
     * Used to create a definition for testing.
     *
     * @param flowTypeName
     */
    public FlowImpl(String flowTypeName) {
        this();
        this.flowPropertyProviderName = flowTypeName;
    }

    public FlowImpl(String flowTypeName, FlowActivityImplementor... flowActivities) {
        this(flowTypeName);
        for(FlowActivityImplementor flowActivity : flowActivities) {
            addActivity(flowActivity);
        }
    }

    public FlowImplementor createInstance() {
        FlowImpl inst = new FlowImpl(this);
        inst.activities = new ArrayList<FlowActivityImplementor>();

        if ( CollectionUtils.isNotEmpty(this.activities)) {
            for(FlowActivityImplementor activity: this.activities) {
                FlowActivityImplementor fa = activity.createInstance();
                if ( isActivatable() ) {
                    fa.setActivatable(true);
                }
                inst.addActivity(fa);
            }
            // need to always be able to start!
            inst.activities.get(0).setActivatable(true);
        }
        return inst;
    }

    public void setActivities(List<FlowActivityImplementor> activities) {
        this.activities = null;
        for(FlowActivityImplementor activity: activities) {
            this.addActivity(activity);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FlowActivityImplementor> getActivities() {
        return activities;
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public ListIterator<FlowActivityImplementor> iterator() {
        return this.activities.listIterator();
    }

    /**
     * @see org.amplafi.flow.Flow#getActivity(int)
     */
    @SuppressWarnings("unchecked")
    public <T extends FlowActivity> T getActivity(int activityIndex) {
        if ( activityIndex < 0 || activityIndex >= activities.size()) {
            // this may be case if done with the flow or we haven't started it yet.
            return null;
        }
        return (T) activities.get(activityIndex);
    }


    @Override
    public void addActivity(FlowActivityImplementor activity) {
        if ( activities == null ) {
            activities = new ArrayList<FlowActivityImplementor>();
        } else {
            for(FlowActivityImplementor existing: activities) {
                if (existing.isFlowPropertyProviderNameSet() && activity.isFlowPropertyProviderNameSet() && StringUtils.equalsIgnoreCase(existing.getFlowPropertyProviderName(), activity.getFlowPropertyProviderName())) {
                    throw new IllegalArgumentException(this.getFlowPropertyProviderName()+": A FlowActivity with the same name has already been added to this flow. existing="+existing+" new="+activity);
                }
            }
        }
        activity.setFlow(this);
        activities.add(activity);
        if ( !isInstance()) {
            activity.processDefinitions();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends FlowActivity> List<T> getVisibleActivities() {
        List<T> list = new ArrayList<T>();
        for(FlowActivity flowActivity: this.activities) {
            if (!flowActivity.isInvisible() && !StringUtils.isBlank(flowActivity.getComponentName())) {
                list.add((T)flowActivity);
            }
        }
        return list;
    }


    /**
     * @see org.amplafi.flow.Flow#getPropertyDefinition(java.lang.String)
     */
    public FlowPropertyDefinition getPropertyDefinition(String key) {
        Map<String, FlowPropertyDefinition> propDefs = this.getPropertyDefinitions();
        return propDefs == null? null : propDefs.get(key);
    }

    public FlowManagement getFlowManagement() {
        return this.getFlowState() == null ? null : this.getFlowState().getFlowManagement();
    }

//    /**
//     * TODO -- copied from FlowActivityImpl -- not certain this is good idea.
//     * Need somewhat to find statically defined properties - not enough to always be looking at the flowState.
//     */
//    @SuppressWarnings("unchecked")
//    public <T> T getProperty(String key) {
//        FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, null);
//        FlowStateImplementor flowStateImplementor =  getFlowState();
//        T result = null;//(T) flowStateImplementor.getPropertyWithDefinition(this, flowPropertyDefinition);
//        return result;
//    }

    /**
     * TODO -- copied from FlowActivityImpl -- not certain this is good idea.
     * Need somewhat to find statically defined properties - not enough to always be looking at the flowState.
     */
    protected <T> FlowPropertyDefinition getFlowPropertyDefinitionWithCreate(String key, Class<T> expected, T sampleValue) {
        FlowPropertyDefinition flowPropertyDefinition = getPropertyDefinition(key);
        if (flowPropertyDefinition == null) {
            flowPropertyDefinition = getFlowManagement().createFlowPropertyDefinition(this, key, expected, sampleValue);
        }
        return flowPropertyDefinition;
    }

    public void setFlowPropertyProviderName(String flowTypeName) {
        this.flowPropertyProviderName = flowTypeName;
    }

    public String getFlowPropertyProviderName() {
        if ( flowPropertyProviderName == null && isInstance()) {
            return getDefinition().getFlowPropertyProviderName();
        } else {
            return flowPropertyProviderName;
        }
    }

    /**
     * @see org.amplafi.flow.Flow#getFlowTitle()
     */
    public String getFlowTitle() {
        if ( flowTitle == null && isInstance()) {
            return getDefinition().getFlowTitle();
        } else {
            return this.flowTitle;
        }
    }

    /**
     * @see org.amplafi.flow.Flow#setFlowTitle(java.lang.String)
     */
    public void setFlowTitle(String flowTitle) {
        this.flowTitle = flowTitle;
    }

    /**
     * @see org.amplafi.flow.Flow#getContinueFlowTitle()
     */
    public String getContinueFlowTitle() {
        if ( continueFlowTitle == null && isInstance()) {
            return getDefinition().getContinueFlowTitle();
        } else {
            return this.continueFlowTitle;
        }
    }

    /**
     * @see org.amplafi.flow.Flow#setContinueFlowTitle(java.lang.String)
     */
    public void setContinueFlowTitle(String continueFlowTitle) {
        this.continueFlowTitle = continueFlowTitle;
    }

    /**
     * @see org.amplafi.flow.Flow#setLinkTitle(java.lang.String)
     */
    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    /**
     * @see org.amplafi.flow.Flow#getLinkTitle()
     */
    public String getLinkTitle() {
        if ( linkTitle != null ) {
            return this.linkTitle;
        } else if ( isInstance()) {
            return getDefinition().getLinkTitle();
        } else {
            return "message:" + "flow." + FlowUtils.INSTANCE.toLowerCase(this.getFlowPropertyProviderName())+".link-title";
        }
    }

    /**
     * @see org.amplafi.flow.Flow#getMouseoverEntryPointText()
     */
    public String getMouseoverEntryPointText() {
        if ( mouseoverEntryPointText == null && isInstance()) {
            return getDefinition().getMouseoverEntryPointText();
        } else {
            return this.mouseoverEntryPointText;
        }
    }
    /**
     * @see org.amplafi.flow.Flow#setMouseoverEntryPointText(java.lang.String)
     */
    public void setMouseoverEntryPointText(String mouseoverEntryPointText) {
        this.mouseoverEntryPointText = mouseoverEntryPointText;
    }

    /**
     * @see org.amplafi.flow.Flow#getFlowDescriptionText()
     */
    public String getFlowDescriptionText() {
        if ( flowDescriptionText == null && isInstance()) {
            return getDefinition().getFlowDescriptionText();
        } else {
            return this.flowDescriptionText;
        }
    }
    /**
     * @see org.amplafi.flow.Flow#setFlowDescriptionText(java.lang.String)
     */
    public void setFlowDescriptionText(String flowDescriptionText) {
        this.flowDescriptionText = flowDescriptionText;
    }

    /**
     * @see org.amplafi.flow.Flow#setPageName(java.lang.String)
     */
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    /**
     * @see org.amplafi.flow.Flow#getPageName()
     */
    public String getPageName() {
        return isInstance()&& pageName == null? getDefinition().getPageName() : pageName;
    }

    /**
     * @see org.amplafi.flow.Flow#setDefaultAfterPage(java.lang.String)
     */
    public void setDefaultAfterPage(String defaultAfterPage) {
        this.defaultAfterPage = defaultAfterPage;
    }

    /**
     * @see org.amplafi.flow.Flow#getDefaultAfterPage()
     */
    public String getDefaultAfterPage() {
        return isInstance() && defaultAfterPage ==null? getDefinition().getDefaultAfterPage():defaultAfterPage;
    }

    /**
     * @see org.amplafi.flow.Flow#refresh()
     */
    public void refresh() {
        int activityIndex = flowState.getCurrentActivityIndex();
        FlowActivity flowActivity = getActivity(activityIndex);
        if ( flowActivity != null ) {
            flowActivity.refresh();
        }
    }

    public String getFlowPropertyProviderFullName() {
        return getFlowPropertyProviderName();
    }

    /**
     * @see org.amplafi.flow.Flow#setFlowState(org.amplafi.flow.FlowState)
     */
    public void setFlowState(FlowState state) {
        this.flowState = state;
    }

    /**
     * @see org.amplafi.flow.Flow#getFlowState()
     */
    @SuppressWarnings("unchecked")
    public <FS extends FlowState> FS getFlowState() {
        return (FS) this.flowState;
    }

    /**
     * @see org.amplafi.flow.Flow#indexOf(org.amplafi.flow.FlowActivity)
     */
    public int indexOf(FlowActivity activity) {
        return this.activities.indexOf(activity);
    }

    /**
     * @see org.amplafi.flow.Flow#setActivatable(boolean)
     */
    public void setActivatable(boolean activatable) {
        this.activatable = activatable;
    }

    /**
     * @see org.amplafi.flow.Flow#isActivatable()
     */
    public boolean isActivatable() {
        return activatable;
    }

    /**
     * @see org.amplafi.flow.Flow#setNotCurrentAllowed(boolean)
     */
    public void setNotCurrentAllowed(boolean notCurrentAllowed) {
        this.notCurrentAllowed = notCurrentAllowed;
    }

    /**
     * @see org.amplafi.flow.Flow#isNotCurrentAllowed()
     */
    public boolean isNotCurrentAllowed() {
        return notCurrentAllowed;
    }

    /**
     * @see org.amplafi.flow.Flow#toString()
     */
    @Override
    public String toString() {
        return getFlowPropertyProviderName()+ (isInstance()?"(instance)":"");
    }
}
