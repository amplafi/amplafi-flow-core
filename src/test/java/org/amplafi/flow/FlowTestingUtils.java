/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

import java.util.concurrent.atomic.AtomicInteger;

import org.amplafi.flow.FlowDefinitionsManager;
import org.amplafi.flow.FlowDefinitionsManagerImpl;
import org.amplafi.flow.FlowImpl;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.translator.BaseFlowTranslatorResolver;
import org.amplafi.flow.translator.FlowTranslatorResolver;


/**
 * @author patmoore
 *
 */
public class FlowTestingUtils {

    private FlowDefinitionsManagerImpl flowDefinitionsManager;

    private BaseFlowTranslatorResolver flowTranslatorResolver;

    private AtomicInteger counter = new AtomicInteger();
    public FlowTestingUtils() {
        this(new FlowDefinitionsManagerImpl(), new BaseFlowTranslatorResolver());
    }

    public FlowTestingUtils(FlowDefinitionsManagerImpl flowDefinitionsManager, BaseFlowTranslatorResolver flowTranslatorResolver) {
        this.flowDefinitionsManager = flowDefinitionsManager;
        this.flowTranslatorResolver = flowTranslatorResolver;
        initializeService();
    }

    /**
     *
     */
    private void initializeService() {
        flowDefinitionsManager.setFlowTranslatorResolver(flowTranslatorResolver);
        flowTranslatorResolver.setFlowDefinitionsManager(flowDefinitionsManager);
        flowDefinitionsManager.initializeService();
        flowTranslatorResolver.initializeService();
    }

    public <T extends FlowActivityImplementor> String addDefinition(T...flowActivities) {
        String flowTypeName = "testflow"+counter.incrementAndGet()+":"+System.nanoTime();
        this.flowDefinitionsManager.addDefinitions(new FlowImpl(flowTypeName, flowActivities));
        return flowTypeName;
    }

    /**
     * @return the flowDefinitionsManager
     */
    public FlowDefinitionsManager getFlowDefinitionsManager() {
        return flowDefinitionsManager;
    }

    /**
     * @return the flowTranslatorResolver
     */
    public FlowTranslatorResolver getFlowTranslatorResolver() {
        return flowTranslatorResolver;
    }

    /**
     * @param flowState
     */
    public void advanceToEnd(FlowState flowState) {
        while( flowState.hasNext()) {
            flowState.next();
        }
    }
}
