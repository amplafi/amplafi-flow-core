/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

import java.util.concurrent.atomic.AtomicInteger;

import org.amplafi.flow.impl.FlowDefinitionsManagerImpl;
import org.amplafi.flow.impl.FlowImpl;
import org.amplafi.flow.impl.BaseFlowManagement;
import org.amplafi.flow.translator.BaseFlowTranslatorResolver;
import org.amplafi.flow.translator.EnumFlowTranslator;
import org.amplafi.flow.translator.ShortFlowTranslator;


/**
 * @author patmoore
 *
 */
public class FlowTestingUtils {

    private FlowDefinitionsManagerImpl flowDefinitionsManager;

    private BaseFlowTranslatorResolver flowTranslatorResolver;

    private BaseFlowManagement flowManagement;

    private AtomicInteger counter = new AtomicInteger();
    public FlowTestingUtils() {
        this(new FlowDefinitionsManagerImpl(), new BaseFlowTranslatorResolver());
    }

    public FlowTestingUtils(FlowDefinitionsManagerImpl flowDefinitionsManager, BaseFlowTranslatorResolver flowTranslatorResolver) {
        this.flowDefinitionsManager = flowDefinitionsManager;
        this.flowTranslatorResolver = flowTranslatorResolver;
        this.flowManagement = new BaseFlowManagement();
        initializeService();
    }

    /**
     *
     */
    private void initializeService() {
        flowTranslatorResolver.addStandardFlowTranslators();
        flowTranslatorResolver.initializeService();
        flowTranslatorResolver.addFlowTranslator(new ShortFlowTranslator());
        flowTranslatorResolver.addFlowTranslator(new EnumFlowTranslator());
        flowDefinitionsManager.setFlowTranslatorResolver(flowTranslatorResolver);
        flowTranslatorResolver.setFlowDefinitionsManager(flowDefinitionsManager);
        flowDefinitionsManager.initializeService();
        this.flowManagement.setFlowDefinitionsManager(flowDefinitionsManager);
        this.flowManagement.setFlowTranslatorResolver(flowTranslatorResolver);

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
    public void resolveAndInit(FlowPropertyDefinition definition) {
        flowTranslatorResolver.resolve(definition);
    }

    /**
     * @return the flowManagement
     */
    public FlowManagement getFlowManagement() {
        return flowManagement;
    }

}
