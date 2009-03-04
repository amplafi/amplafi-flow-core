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

import java.util.concurrent.atomic.AtomicInteger;

import org.amplafi.flow.impl.FlowDefinitionsManagerImpl;
import org.amplafi.flow.impl.FlowImpl;
import org.amplafi.flow.impl.BaseFlowManagement;
import org.amplafi.flow.translator.BaseFlowTranslatorResolver;
import org.amplafi.flow.translator.EnumFlowTranslator;
import org.amplafi.flow.translator.ShortFlowTranslator;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;


/**
 * @author patmoore
 *
 */
public class FlowTestingUtils {

    private FlowDefinitionsManager flowDefinitionsManager;

    private FlowTranslatorResolver flowTranslatorResolver;

    private BaseFlowManagement flowManagement;

    private AtomicInteger counter = new AtomicInteger();
    public FlowTestingUtils() {
        this(new FlowDefinitionsManagerImpl(), new BaseFlowTranslatorResolver());
    }
    public FlowTestingUtils(FlowDefinitionsManager flowDefinitionsManager, FlowTranslatorResolver flowTranslatorResolver) {
        this.flowDefinitionsManager = flowDefinitionsManager;
        this.flowTranslatorResolver = flowTranslatorResolver;
        this.flowManagement = new BaseFlowManagement();
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
    @SuppressWarnings("unchecked")
    private void initializeService() {
        ((BaseFlowTranslatorResolver)flowTranslatorResolver).addStandardFlowTranslators();
        ((BaseFlowTranslatorResolver)flowTranslatorResolver).initializeService();
        ((BaseFlowTranslatorResolver)flowTranslatorResolver).addFlowTranslator(new ShortFlowTranslator());
        ((BaseFlowTranslatorResolver)flowTranslatorResolver).addFlowTranslator(new EnumFlowTranslator());
        ((FlowDefinitionsManagerImpl)flowDefinitionsManager).setFlowTranslatorResolver(flowTranslatorResolver);
        ((BaseFlowTranslatorResolver)flowTranslatorResolver).setFlowDefinitionsManager(flowDefinitionsManager);
        ((FlowDefinitionsManagerImpl)flowDefinitionsManager).initializeService();
        this.flowManagement.setFlowDefinitionsManager(flowDefinitionsManager);
        this.flowManagement.setFlowTranslatorResolver(flowTranslatorResolver);

    }

    public <T extends FlowActivityImplementor> String addDefinition(T...flowActivities) {
        String flowTypeName = "testflow"+counter.incrementAndGet()+":"+System.nanoTime();
        this.flowDefinitionsManager.addDefinitions(new FlowImpl(flowTypeName, flowActivities));
        return flowTypeName;
    }

    /**
     * Instantiates a FlowActivity of the given class, attaches it to a new (dummy)
     * flow (and optionally sets the flow's state).
     * @param clazz
     * @param state
     * @param <T>
     * @return a new T
     */
    public <T extends FlowActivityImplementor> T initActivity(Class<T> clazz, FlowState state)  {
        try {
            T activity = clazz.newInstance();
            Flow flow = new FlowImpl();
            flow.addActivity(activity);
            if (state!=null) {
                flow.setFlowState(state);
            }
            return activity;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiates a FlowActivity of the given class and attaches it to a new (dummy)
     * flow.
     * @param clazz
     * @param <T>
     * @return a new T
     */
    public <T extends FlowActivityImplementor> T initActivity(Class<T> clazz) {
        return initActivity(clazz, null);
    }
    public FlowDefinitionsManager programFlowDefinitionManager(String flowTypeName, FlowActivityImplementor... activities) {
        final Flow def = new FlowImpl(flowTypeName, activities);
        getFlowTranslatorResolver().resolveFlow(def);
        EasyMock.expect(getFlowDefinitionsManager().getFlowDefinition(flowTypeName)).andReturn(def).anyTimes();
        EasyMock.expect(getFlowDefinitionsManager().isFlowDefined(flowTypeName)).andReturn(true).anyTimes();
        EasyMock.expect(getFlowDefinitionsManager().getInstanceFromDefinition(flowTypeName)).andAnswer(new IAnswer<Flow>() {
            @Override
            public Flow answer() {
                return def.createInstance();
            }
        }).anyTimes();
        return getFlowDefinitionsManager();
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
