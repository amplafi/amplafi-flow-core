/**
 * Copyright 2006-8 by Amplafi, Inc.
 */
package org.amplafi.flow;

import org.amplafi.json.JSONWriter;


/**
 * Implementations determine which {@link FlowTranslator} should be used
 * for a given {@link FlowPropertyDefinition}.
 *
 */
public interface FlowTranslatorResolver {
    /**
     *
     * @param flowPropertyDefinition
     */
    public void resolve(FlowPropertyDefinition flowPropertyDefinition);
    /**
    *
    * @param dataClassDefinition
    */
   public void resolve(DataClassDefinition dataClassDefinition);
    /**
     * all the other methods end up calling this method.
     * @param clazz
     * @return the FlowTranslator or null if none could be found.
     */
    public FlowTranslator<?> resolve(Class<?> clazz);
    public void resolveFlow(Flow flow);

    JSONWriter getJsonWriter();

    /**
     * @param key
     * @return the {@link FlowPropertyDefinition} for this key.
     */
    public FlowPropertyDefinition getFlowPropertyDefinition(String key);
    public void putCommonFlowPropertyDefinitions(FlowPropertyDefinition... flowPropertyDefinitions);
    /**
     * @param flowActivity
     */
    public void resolve(FlowActivity flowActivity);
}
