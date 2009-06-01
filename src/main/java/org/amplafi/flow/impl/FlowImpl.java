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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.amplafi.flow.flowproperty.CancelTextFlowPropertyValueProvider;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.MessageFlowPropertyValueProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import static org.amplafi.flow.FlowConstants.*;
import static org.amplafi.flow.PropertyUsage.*;
import org.amplafi.flow.*;


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
public class FlowImpl implements Serializable, Cloneable, Flow, Iterable<FlowActivityImplementor> {

    private static final long serialVersionUID = -985306244948511836L;
    /**
     * if false, this flow is a definition. It should be copied if it is to become an
     * instance.
     */
    private Flow definitionFlow;
    /**
     * the definition's lookup key -- will be referenced in database.
     */
    private String flowTypeName;

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

    private Map<String, FlowPropertyDefinition> propertyDefinitions;

    /**
     * Used to restore an existing definition or create an new definitions from XML
     */
    public FlowImpl() {
        // see #2179 #2192
        this.addPropertyDefinitions(
            new FlowPropertyDefinitionImpl(FSTITLE_TEXT).initPropertyUsage(flowLocal),
            new FlowPropertyDefinitionImpl(FSCANCEL_TEXT).initPropertyUsage(flowLocal).initFlowPropertyValueProvider(CancelTextFlowPropertyValueProvider.INSTANCE),
            new FlowPropertyDefinitionImpl(FSNO_CANCEL, boolean.class).initPropertyUsage(flowLocal),
            new FlowPropertyDefinitionImpl(FSFINISH_TEXT).initPropertyUsage(flowLocal).initFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
            new FlowPropertyDefinitionImpl(FSRETURN_TO_TEXT).initPropertyUsage(flowLocal).initFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
            new FlowPropertyDefinitionImpl(FSFLOW_TRANSITIONS, FlowTransition.class, Map.class).initAutoCreate().initPropertyUsage(flowLocal),
            new FlowPropertyDefinitionImpl(FSREADONLY, boolean.class).initPropertyUsage(flowLocal),
            // io -- for now because need to communicate the next page to be displayed
            new FlowPropertyDefinitionImpl(FSPAGE_NAME).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSAFTER_PAGE).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSDEFAULT_AFTER_PAGE).initPropertyUsage(flowLocal),
            new FlowPropertyDefinitionImpl(FSDEFAULT_AFTER_CANCEL_PAGE).initPropertyUsage(flowLocal),
            new FlowPropertyDefinitionImpl(FSHIDE_FLOW_CONTROL, boolean.class).initPropertyUsage(flowLocal),
            new FlowPropertyDefinitionImpl(FSACTIVATABLE, boolean.class).initPropertyUsage(flowLocal),
            new FlowPropertyDefinitionImpl(FSIMMEDIATE_SAVE, boolean.class).initPropertyUsage(flowLocal),

            new FlowPropertyDefinitionImpl(FSAPI_CALL, boolean.class).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSAUTO_COMPLETE, boolean.class).initPropertyUsage(flowLocal),
            new FlowPropertyDefinitionImpl(FSALT_FINISHED).initPropertyUsage(flowLocal),
            new FlowPropertyDefinitionImpl(FSREDIRECT_URL, URI.class).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSREFERING_URL, URI.class).initPropertyUsage(use),
            new FlowPropertyDefinitionImpl(FSCONTINUE_WITH_FLOW).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSRETURN_TO_FLOW).initPropertyUsage(io),
            new FlowPropertyDefinitionImpl(FSNEXT_FLOW).initPropertyUsage(io)

        );
    }

    /**
     * creates a instance Flow from a definition.
     * @param definition
     */
    public FlowImpl(Flow definition) {
        this.flowTypeName = definition.getFlowTypeName();
        this.definitionFlow = definition;
    }
    /**
     * Used to create a definition for testing.
     *
     * @param flowTypeName
     */
    public FlowImpl(String flowTypeName) {
        this();
        this.flowTypeName = flowTypeName;
    }

    public FlowImpl(String flowTypeName,
            FlowActivityImplementor... flowActivities) {
        this(flowTypeName);
        for(FlowActivityImplementor flowActivity : flowActivities) {
            addActivity(flowActivity);
        }
    }

    /**
     * @see org.amplafi.flow.Flow#createInstance()
     */

    public Flow createInstance() {
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

    /**
     * @see org.amplafi.flow.Flow#setActivities(java.util.List)
     */
    public void setActivities(List<FlowActivityImplementor> activities) {
        this.activities = null;
        for(FlowActivityImplementor activity: activities) {
            this.addActivity(activity);
        }
    }

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
                if (existing.isActivityNameSet() && activity.isActivityNameSet() && StringUtils.equalsIgnoreCase(existing.getActivityName(), activity.getActivityName())) {
                    throw new IllegalArgumentException(this.getFlowTypeName()+": A FlowActivity with the same name has already been added to this flow. existing="+existing+" new="+activity);
                }
            }
        }
        activity.setFlow(this);
        activities.add(activity);
        if ( !isInstance()) {
            activity.processDefinitions();
        }
    }

    /**
     * @see org.amplafi.flow.Flow#isInstance()
     */
    public boolean isInstance() {
        return this.definitionFlow != null;
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
     * @see org.amplafi.flow.Flow#setPropertyDefinitions(java.util.Map)
     */
    public void setPropertyDefinitions(Map<String, FlowPropertyDefinition> properties) {
        this.propertyDefinitions = properties;
    }
    /**
     * @see org.amplafi.flow.Flow#getPropertyDefinitions()
     */
    public Map<String, FlowPropertyDefinition> getPropertyDefinitions() {
        if ( propertyDefinitions == null && this.isInstance() ) {
            // as is usually the case for instance flow activities.
            return this.definitionFlow.getPropertyDefinitions();
        }
        return propertyDefinitions;
    }

    /**
     * @see org.amplafi.flow.Flow#getPropertyDefinition(java.lang.String)
     */
    public FlowPropertyDefinition getPropertyDefinition(String key) {
        Map<String, FlowPropertyDefinition> propDefs = this.getPropertyDefinitions();
        return propDefs == null? null : propDefs.get(key);
    }
    public void addPropertyDefinitions(FlowPropertyDefinition... definitions) {
        if ( definitions != null && definitions.length > 0) {
            for(FlowPropertyDefinition definition: definitions) {
                this.addPropertyDefinition(definition);
            }
        }
    }
    /**
     * @see org.amplafi.flow.Flow#addPropertyDefinition(org.amplafi.flow.FlowPropertyDefinition)
     */
    public void addPropertyDefinition(FlowPropertyDefinition definition) {
        if ( definition == null ) {
            return;
        }
        if ( this.propertyDefinitions == null ) {
            if ( isInstance()) {
                this.propertyDefinitions =
                    new LinkedHashMap<String, FlowPropertyDefinition>();
                if ( this.definitionFlow.getPropertyDefinitions() != null) {
                    this.propertyDefinitions.putAll(this.definitionFlow.getPropertyDefinitions());
                }
            } else {
                this.propertyDefinitions = new LinkedHashMap<String, FlowPropertyDefinition>();
            }
        }
        FlowPropertyDefinition current = this.propertyDefinitions.get(definition.getName());
        if ( current != null ) {
            if ( !definition.merge(current) ) {
                throw new IllegalArgumentException(definition+": cannot be merged with "+current);
            }
        }
        this.propertyDefinitions.put(definition.getName(), definition);
    }

    /**
     * @see org.amplafi.flow.Flow#setFlowTypeName(java.lang.String)
     */
    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }

    /**
     * @see org.amplafi.flow.Flow#getFlowTypeName()
     */
    public String getFlowTypeName() {
        if ( flowTypeName == null && isInstance()) {
            return definitionFlow.getFlowTypeName();
        } else {
            return flowTypeName;
        }
    }

    /**
     * @see org.amplafi.flow.Flow#getFlowTitle()
     */
    public String getFlowTitle() {
        if ( flowTitle == null && isInstance()) {
            return definitionFlow.getFlowTitle();
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
            return definitionFlow.getContinueFlowTitle();
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
            return definitionFlow.getLinkTitle();
        } else {
            return "message:" + "flow." + FlowUtils.INSTANCE.toLowerCase(this.getFlowTypeName())+".link-title";
        }
    }

    /**
     * @see org.amplafi.flow.Flow#getMouseoverEntryPointText()
     */
    public String getMouseoverEntryPointText() {
        if ( mouseoverEntryPointText == null && isInstance()) {
            return definitionFlow.getMouseoverEntryPointText();
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
            return definitionFlow.getFlowDescriptionText();
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
        return isInstance()&& pageName == null? definitionFlow.getPageName() : pageName;
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
        return isInstance() && defaultAfterPage ==null?definitionFlow.getDefaultAfterPage():defaultAfterPage;
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

    /**
     * @see org.amplafi.flow.Flow#setFlowState(org.amplafi.flow.FlowState)
     */
    public void setFlowState(FlowState state) {
        this.flowState = state;
    }

    /**
     * @see org.amplafi.flow.Flow#getFlowState()
     */
    public FlowState getFlowState() {
        return this.flowState;
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
        return getFlowTypeName()+ (isInstance()?"(instance)":"");
    }
}
