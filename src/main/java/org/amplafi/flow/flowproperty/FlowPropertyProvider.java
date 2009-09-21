/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import java.util.Map;

import org.amplafi.flow.FlowPropertyDefinition;

/**
 * Implementers manage a map of {@link FlowPropertyDefinition}s
 * @author patmoore
 *
 */
public interface FlowPropertyProvider {
    void setPropertyDefinitions(Map<String, FlowPropertyDefinition> flowPropertyDefinitions);

    Map<String, FlowPropertyDefinition> getPropertyDefinitions();

    FlowPropertyDefinition getPropertyDefinition(String key);

    void addPropertyDefinitions(FlowPropertyDefinition...flowPropertyDefinitions);
}
