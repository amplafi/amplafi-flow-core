/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;


/**
 * Implementers will add the needed FlowPropertyDefinitions to {@link FlowPropertyProvider}s.
 * @author patmoore
 *
 */
public interface FlowPropertyDefinitionProvider {

    /**
     * Add to the flowPropertyProvider the definitions needed by the
     * @param flowPropertyProvider
     */
    void defineFlowPropertyDefinitions(FlowPropertyProvider flowPropertyProvider);

}
