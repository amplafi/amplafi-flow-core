/*
 * Created on Jul 31, 2007
 * Copyright 2006 by Patrick Moore
 */
package org.amplafi.flow;

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
     * An individual FlowActivity's title
     */
    public static final String FATITLE_TEXT = "faTitleText";
    public static final String FSCANCEL_TEXT = "fsCancelText";
    public static final String DEFAULT_FSCANCEL_TEXT = "message:flow.label-cancel";
    /**
     * if true then cancel is not an option.
     */
    public static final String FSNO_CANCEL = "fsNoCancel";

    public static final String FSFINISH_TEXT = "fsFinishText";
    public static final String DEFAULT_FSFINISH_TEXT = "message:flow.label-finish";

    public static final String FSFLOW_TRANSITIONS = "fsFlowTransitions";

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
     * Flow state is readonly. The FlowActivities are not allowed to change the
     * state of any object.
     *
     * TODO: check that this is enforced (by no calling saveChanges() )
     */
    public static final String FSREADONLY = "fsReadonly";

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
     * Used to indicate that the flow was finished with an
     * alternative way / button.
     */
    public static final String FSALT_FINISHED = "fsAltFinished";
    /**
     * The id of an existing flow that should be continued after this flow ends.
     * This represents a calling relationship ( without the requirement to the flow that is setting this
     * value on another flow.)
     *
     * To start a new flow use {@link #FSNEXT_FLOW}.
     */
    public static final String FSCONTINUE_WITH_FLOW = "fsContinueWithFlow";
    /**
     * If there is no {@link #FSCONTINUE_WITH_FLOW} flow, then the flow when it finishes should return to the
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
     * the new flow type that should be started.
     */
    public static final String FSNEXT_FLOW = "fsNextFlow";

    /**
     * For flow activities that should immediately have saveChanges() called.
     */
    public static final String FSIMMEDIATE_SAVE = "fsImmediateSave";

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
    public static final String RENDER_RESULT = "fsRenderResult";
    public static final String HTML = "html";
    public static final String JSON = "json";
    /**
     *
     */
    public static final String HANDLED = "handled";
}
