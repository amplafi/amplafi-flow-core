package org.amplafi.flow.web;

import static com.sworddance.util.CUtilities.isNotEmpty;
import static com.sworddance.util.CUtilities.put;
import static org.amplafi.flow.FlowConstants.FSREFERRING_URL;
import static org.amplafi.flow.launcher.FlowLauncher.ADVANCE_TO_END;
import static org.amplafi.flow.launcher.FlowLauncher.AS_FAR_AS_POSSIBLE;
import static org.amplafi.flow.launcher.FlowLauncher.FLOW_ID;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.amplafi.flow.FlowConstants;
import org.amplafi.flow.FlowDefinitionsManager;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowManager;
import org.amplafi.flow.FlowRenderer;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowStateLifecycle;
import org.amplafi.flow.FlowUtils;
import org.amplafi.flow.ServicesConstants;
import org.amplafi.flow.impl.JsonFlowRenderer;
import org.apache.commons.logging.Log;
import org.apache.http.HttpStatus;

import com.sworddance.util.NotNullIterator;

public class BaseFlowService implements FlowService {

    public static final String USE_CURRENT = "current";

    protected static final String SCRIPT_CONTENT_TYPE = "text/javascript";

    private FlowManager flowManager;

    private Log log;

    /**
     * if {@link org.amplafi.flow.launcher.FlowLauncher#COMPLETE_FLOW} is not supplied - this is the
     * default value to use.
     */
    protected String defaultComplete;

    /**
     * the default way the results should be rendered. "html", "json", etc.
     */
    protected String renderResultDefault;

    protected boolean discardSessionOnExit;

    private boolean assumeApiCall;

    protected FlowDefinitionsManager flowDefinitionsManager;

	private List<FlowRenderer> flowRenderers;

    public BaseFlowService() {
    }

    public BaseFlowService(FlowDefinitionsManager flowDefinitionsManager, FlowManager flowManager) {
        this.setFlowDefinitionsManager(flowDefinitionsManager);
        this.setFlowManager(flowManager);
        addFlowRenderer(new JsonFlowRenderer(flowDefinitionsManager));
    }

    public void addFlowRenderer(FlowRenderer flowRenderer) {
    	if (flowRenderers == null) {
    		flowRenderers = new ArrayList<FlowRenderer>();
    	}
    	flowRenderers.add(flowRenderer);
	}

	/**
     * @param flowDefinitionsManager the flowDefinitionsManager to set
     */
    public void setFlowDefinitionsManager(FlowDefinitionsManager flowDefinitionsManager) {
        this.flowDefinitionsManager = flowDefinitionsManager;
    }

    /**
     * @return the flowDefinitionsManager
     */
    public FlowDefinitionsManager getFlowDefinitionsManager() {
        return flowDefinitionsManager;
    }

    public void service(FlowRequest flowRequest) throws Exception {
        if (flowRequest.isDescribeRequest()) {
        	getRenderer(flowRequest.getRenderResultType()).describe(flowRequest);
        } else {
	        Map<String, String> initial = FlowUtils.INSTANCE.createState(FlowConstants.FSAPI_CALL, isAssumeApiCall());
	        // TODO map cookie to the json flow state.
	        String cookieString = flowRequest.getParameter(ServicesConstants.COOKIE_OBJECT);
	        if (isNotBlank(cookieString)) {
	            initial.put(ServicesConstants.COOKIE_OBJECT, cookieString);
	        }

	        List<String> keyList = flowRequest.getParameterNames();
	        if (isNotEmpty(keyList)) {
	            for (String key : keyList) {
	                String value = flowRequest.getParameter(key);
	                if (value != null) {
	                    // we do allow the value to be blank ( value may be existence of parameter)
	                    initial.put(key, value);
	                }
	            }
	        }

	        put(initial, FSREFERRING_URL, flowRequest.getReferingUri());
			doActualService(flowRequest, initial);
        }
    }

    protected FlowState doActualService(FlowRequest request, Map<String, String> initial) throws Exception {
        return completeFlowState(request, initial);
    }

    private FlowState getFlowState(FlowRequest request, Map<String, String> initial) throws Exception {
        FlowState flowState = null;
        String flowId = request.getFlowId();
        String flowType = request.getFlowType();

        if (isNotBlank(flowId)) {
            flowState = getFlowManagement().getFlowState(flowId);
        }

		if (flowState != null) {
        	flowState.setAllProperties(initial);
        } else if (isNotBlank(flowType)) {
            if (!getFlowManager().isFlowDefined(flowType)) {
                renderError(request, flowType + ": no such flow type", null, null);
                return null;
            }

            if (USE_CURRENT.equals(flowId)) {
                flowState = getFlowManagement().getFirstFlowStateByType(flowType);
            }

            if (flowState == null) {
                String returnToFlowLookupKey = null;
                boolean currentFlow = !request.isBackground();
				flowState = getFlowManagement().startFlowState(flowType, currentFlow, initial, returnToFlowLookupKey);
                if (flowState == null || flowState.getFlowStateLifecycle() == FlowStateLifecycle.failed) {
                    renderError(request, flowType + ": could not start flow type", flowState, null);
                    return null;
                }
            }
        } else {
            String message = String.format("Query String for request didn't contain %s or %s. At least one needs to be specified.", ServicesConstants.FLOW_TYPE, FLOW_ID);
            renderError(request, message, null, null);
            return null;
        }
        return flowState;
    }

    public void setFlowManager(FlowManager flowManager) {
        this.flowManager = flowManager;
    }

    public FlowManager getFlowManager() {
        return flowManager;
    }

    public FlowManagement getFlowManagement() {
        return getFlowManager().getFlowManagement();
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    protected void advanceFlow(FlowState flowState) {
    	flowState.next();
    }

    public void setDefaultComplete(String defaultComplete) {
        this.defaultComplete = defaultComplete;
    }

    public String getDefaultComplete() {
        return defaultComplete;
    }

    public void setRenderResultDefault(String renderResultDefault) {
        this.renderResultDefault = renderResultDefault;
    }

    public String getRenderResultDefault() {
        return renderResultDefault;
    }

    public void setDiscardSessionOnExit(boolean discardSession) {
        this.discardSessionOnExit = discardSession;
    }

    public boolean isDiscardSessionOnExit() {
        return discardSessionOnExit;
    }

    /**
     * @param assumeApiCall the assumeApiCall to set
     */
    public void setAssumeApiCall(boolean assumeApiCall) {
        this.assumeApiCall = assumeApiCall;
    }

    /**
     * @return the assumeApiCall
     */
    public boolean isAssumeApiCall() {
        return assumeApiCall;
    }

    private void renderError(FlowRequest flowRequest, String message, FlowState flowState, Exception exception) throws Exception {
        String errorMessage = flowRequest.getReferingUri();
        if (flowState != null ) {
            errorMessage += " Error while running flowState=" + flowState;
        }

        getLog().error(errorMessage, exception);
        flowRequest.setStatus(HttpStatus.SC_BAD_REQUEST);
    	String renderResult = flowRequest.getRenderResultType();
		Writer writer = flowRequest.getWriter();
		getRenderer(renderResult).renderError(flowState, message, exception, writer);
    }

    protected FlowState completeFlowState(FlowRequest flowRequest, Map<String, String> initial) throws Exception {
        FlowState flowState = null;
        try {
            if ((flowState = getFlowState(flowRequest, initial)) != null) {
                String complete = flowRequest.getCompleteType();
                if (complete == null) {
                    // blank means don't try to complete flow.
                    complete = defaultComplete;
                }
                if (ADVANCE_TO_END.equalsIgnoreCase(complete)) {
                    while (!flowState.isCompleted()) {
                        advanceFlow(flowState);
                    }
                } else if (AS_FAR_AS_POSSIBLE.equalsIgnoreCase(complete)) {
                    String advanceToActivity = flowRequest.getAdvanceToActivity();
                    if (advanceToActivity != null) {
                        while (!flowState.isCompleted() && !flowState.getCurrentActivityByName().equals(advanceToActivity)) {
                            advanceFlow(flowState);
                        }
                    } else {
                        while (!flowState.isCompleted() && !flowState.isFinishable()) {
                            advanceFlow(flowState);
                        }
                    }
                }
                initializeRequestedParameters(flowRequest.getPropertiesToInitialize(), flowState);
                getRenderer(flowRequest.getRenderResultType()).render(flowState, flowRequest.getWriter());
                flowRequest.setStatus(HttpStatus.SC_OK);
            }
        } catch (Exception e) {
            renderError(flowRequest, "Error", flowState, e);
            if (flowState != null && !flowState.isPersisted()) {
                getFlowManagement().dropFlowState(flowState);
            }
        }
        return flowState;
    }

    private FlowRenderer getRenderer(String renderResult) {
    	if (renderResult == null) {
    		renderResult = renderResultDefault;
    	}
    	if (flowRenderers != null){
    		for (FlowRenderer renderer : flowRenderers) {
				if (renderer.getRenderResultType().equals(renderResult)){
					return renderer;
				}
			}
    	}
    	throw new IllegalStateException("There is no flow renderer for requested render result type: " + renderResult);
	}

	/**
	 * Needed to initialize some common properties which might not be part of the flow. For example if a client needs to get
	 * current user name along with messages list flow request.
	 *
     * @param propertiesToInitialize
     * @param flowState
     */
    private void initializeRequestedParameters(Iterable<String> propertiesToInitialize, FlowState flowState) {
        //Now just request all the properties that client asked for.
        NotNullIterator<String> it = NotNullIterator.newNotNullIterator(propertiesToInitialize);
        while (it.hasNext()) {
        	flowState.getProperty(it.next());
        }
    }

	public List<FlowRenderer> getFlowRenderers() {
		return flowRenderers;
	}

	public void setFlowRenderers(List<FlowRenderer> flowRenderers) {
		this.flowRenderers = flowRenderers;
	}

}