/*
 * Created on Apr 3, 2007
 * Copyright 2006 by Patrick Moore
 */
package org.amplafi.flow;


import java.util.Map;

import org.amplafi.flow.translator.FlowTranslatorResolver;


/**
 * FlowDefinitionsManager handles defining and creating the standard flows.
 *
 * @author Patrick Moore
 */
public interface FlowDefinitionsManager {

    public void addDefinitions(Flow... flows);

    public Flow getInstanceFromDefinition(String flowTypeName);

    /**
     * Returns the flow having the specified name.
     * @param flowTypeName
     * @return the Flow definition.
     */
    public Flow getFlowDefinition(String flowTypeName);
    public boolean isFlowDefined(String flowTypeName);

    /**
     * Returns all defined flows, keyed by their name.
     * @return the map with all the currently defined flows indexed by (usually) the {@link Flow#getFlowTypeName()}.
     */
    public Map<String, Flow> getFlowDefinitions();

    public FlowManagement getSessionFlowManagement();

    /**
     * @param key (usually) the {@link Flow#getFlowTypeName()}.
     * @param flow the flow definition to add.
     */
    public void addDefinition(String key, Flow flow);

    /**
     * @param flowTranslatorResolver
     */
    public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver);

    public FlowTranslatorResolver getFlowTranslatorResolver();
}