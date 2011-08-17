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

import org.amplafi.flow.web.BaseFlowService;


/**
 *  TODO Convert this to {@link FlowPropertyDefinition}s
 */
public interface FlowConstants {
    public static final String FLOW_PROPERTY_PREFIX = "fprop:";
    /**
     * The Flow's title. Used to override the title as provided by the flow definition
     */
    public static final String FSTITLE_TEXT = "fsTitleText";
    /**
     * TODO: need to add and eliminate FlowImpl.linkTitle
     */
    public static final String FSLINK_TEXT = "fsLinkText";
    /**
     * An individual FlowActivity's title
     */
    public static final String FATITLE_TEXT = "faTitleText";
    public static final String FSCANCEL_TEXT = "fsCancelText";
    public static final String FSRETURN_TO_TEXT = "fsReturnToText";
    public static final String DEFAULT_FSCANCEL_TEXT = "message:flow.label-cancel";
    /**
     * if true then cancel is not an option.
     */
    public static final String FSNO_CANCEL = "fsNoCancel";

    public static final String FSFINISH_TEXT = "fsFinishText";
    public static final String DEFAULT_FSFINISH_TEXT = "message:flow.label-finish";

    /**
     * Used to submit the current FlowActivity data without advancing the flow.
     * Set on a per FlowActivity basis.
     */
    public static final String FAUPDATE_TEXT = "faUpdateText";
    /**
     * The text to be used for the "Next" Button.
     * Set on a per FlowActivity basis.
     */
    public static final String FANEXT_TEXT = "faNextText";
    /**
     * The text to be used for the "Previous" Button.
     * Set on a per FlowActivity basis.
     */
    public static final String FAPREV_TEXT = "faPrevText";
    /**
     * Invisible flowactivities are activities that do things like database updates,
     * but have no UI component to them.
     * Set on a per FlowActivity basis.
     */
    public static final String FAINVISIBLE = "faInvisible";
    /**
     * {@link #FSACTIVATABLE} for an activity.
     */
    public static final String FAACTIVATABLE = "faActivatable";


    /**
     * Used to override the default page name
     * TODO how to handle a flowActivity that supplies its own pageName when backing
     * up through a flow?
     */
    public static final String FSPAGE_NAME = "fsPageName";

    /**
     * always redirect to this page after a flow ends (except for a forced drop)
     * the  {@link #FSDEFAULT_AFTER_PAGE} and {@link #FSDEFAULT_AFTER_CANCEL_PAGE} values are ignored.
     */
    public static final String FSAFTER_PAGE= "fsAfterPage";
    /**
     * The default page (or uri?) to redirect to on success
     */
    public static final String FSDEFAULT_AFTER_PAGE= "fsDefaultAfterPage";
    /**
     * if the flow is canceled, this is page to redirect to.
     * if not set then a cancel uses the FSAFTER_PAGE
     */
    public static final String FSDEFAULT_AFTER_CANCEL_PAGE= "fsDefaultCancelAfterPage";
    /**
     * Used to store where an arbitrary uri to redirect to as soon as flow ends.
     */
    public static final String FSREDIRECT_URL = "fsRedirectUrl";
    /**
     * Used to store the external referring url as reported in the http request.
     * TODO -- 3 Jan 2009 most usages of this are bad -- need to indicate which flow will redirect to refering uri once completed.
     * TODO: Use "Referer" as string so it matches what the HttpRequest sends.
     */
    public static final String FSREFERRING_URL = "fsReferringUrl";

    /**
     * hide the tabbed flow controls.
     */
    public static final String FSHIDE_FLOW_CONTROL = "fsHideFlowControl";

    /**
     * The FlowActivity can be selected by the user if this is true.
     * If this property is set at the flow level, then all FlowActivities can be immediately
     * activated.
     */
    public static final String FSACTIVATABLE = "fsActivatable";

    /**
     * used to indicate that this FA should do what it can silently.
     */
    public static final String FSAUTO_COMPLETE = "fsAutoComplete";

    /**
     * For flow activities that should immediately have saveChanges() called.
     * 
     * @deprecated Kostya: seems to be unused, remove?
     */
    @Deprecated
    public static final String FSIMMEDIATE_SAVE = "fsImmediateSave";

    /**
     * Used to indicate that the flow was finished with an
     * alternative way / button.
     *
     * This property's value will be set to the FlowTransition key.
     * See {@link #FSFLOW_TRANSITIONS} and {@link #FSFLOW_TRANSITION}
     */
    public static final String FSALT_FINISHED = "fsAltFinished";
    /**
     * The id of an existing flow that should be continued after this flow ends. This takes precedence over
     * This represents a calling relationship ( without the requirement to the flow that is setting this
     * value on another flow -- tail-end recursion?)
     *
     * To start a new flow use {@link #FSNEXT_FLOW}.
     */
    @Deprecated // why not FSRETURN_TO_FLOW -- tail-end recursion?
    public static final String FSCONTINUE_WITH_FLOW = "fsContinueWithFlow";
    /**
     * Map<String,FlowTransition> - map for transitions.
     *
     * The key is the value of {@link #FSALT_FINISHED}
     * These transitions are looked at before any other
     * transitions.
     */
    public static final String FSFLOW_TRANSITIONS = "fsFlowTransitions";
    /**
     * The selected FlowTransition. Selected from {@link #FSFLOW_TRANSITIONS} using {@link #FSALT_FINISHED}.
     */
    public static final String FSFLOW_TRANSITION = "fsFlowTransition";
    /**
     * If there is no {@link #FSFLOW_TRANSITIONS} flow, then the flow when it finishes should return to the
     * FSRETURN_TO_FLOW flow.
     * FSRETURN_TO_FLOW value is passed to the FSCONTINUE_WITH_FLOW when the active flow finishes.
     *
     * For example,
     * Flow #1 starts Flow #2 but Flow #1 has not finished. Flow#2's FSRETURN_TO_FLOW value is Flow #1.
     * Flow #2 finishes but wants to continue to Flow#3. Flow #2 passes to Flow#3 Flow#2's FSRETURN_TO_FLOW (Flow#1).
     * When Flow #3 completes, it has no {@link #FSCONTINUE_WITH_FLOW} value set so it uses its FSRETURN_TO_FLOW value
     * to return to Flow #1.
     */
    public static final String FSRETURN_TO_FLOW = "fsReturnToFlow";
    /**
     * Used when the flow to return to has not been started.
     *
     * Primary use case is Flow A1 (flowType 'A')  starts another flow (Flow B1). Flow A1 does not want to stay active ( unnecessary state ). Flow
     */
    public static final String FSRETURN_TO_FLOW_TYPE = "fsReturnToFlowType";

    /**
     * Map<String,FlowTransition> - map for transitions. Checked after {@link #FSFLOW_TRANSITIONS}, {@link #FSRETURN_TO_FLOW}
     * intent is to suggest a flow transition that can be overridden by FSFLOW_TRANSITIONS
     *
     * TODO: FlowBorder does not display these choices. Is this a problem?
     */
    public static final String FSSUGGESTED_NEXT_FLOW_TYPE = "fsSuggestedNextFlowType";

    /**
     * ONLY WORKS IF {@link org.amplafi.flow.impl.TransitionFlowActivity} is in the flow!
     * TODO: suck this up into the nascent FlowTransitionFlowPropertyValueProvider
     * the new flow type that should be started.
     * Used by {@link org.amplafi.flow.impl.TransitionFlowActivity}, seems like should be combined with {@link #FSFLOW_TRANSITIONS}
     * But need to handle "default" specified via xml?
     * Also can be specified via url parameters.
     */
    @Deprecated
    public static final String FSNEXT_FLOW = "fsNextFlow";

    /**
     * MUST NOT BE included as a FlowPropertyDefinition -- will result in the flow template not wired up correctly to the FullFlowComponent
     */
    public static final String ATTACHED_FLOW = "attachedFlowState";
    /**
     * how to render the return result.
     * choices:
     * html (webpage)
     * json ( FlowState is rendered as json output )
     * handled (the calling code will handle this issue)
     */
    public static final String FSRENDER_RESULT = "fsRenderResult";
    
    /**
     * 
     * On calls to {@link BaseFlowService} these properties will be accessed via FPVP.get().
     * 
     */
    public static final String FS_PROPS_TO_INIT = "fsPropsToInit";
    
    public static final String HTML = "html";
    public static final String JSON = "json";
    public static final String HANDLED = "handled";
    public static final String JSON_DESCRIBE = "json/describe";

    /**
     * set this as a boolean flowState property when the flow is running as a api call.
     * use flowAppearance : {@link org.amplafi.flow.flowproperty.FlowAppearanceFlowPropertyDefinitionProvider}
     */
    @Deprecated // use FlowAppearanceFlowPropertyDefinitionProvider
    public static final String FSAPI_CALL = "fsApiCall";
}
