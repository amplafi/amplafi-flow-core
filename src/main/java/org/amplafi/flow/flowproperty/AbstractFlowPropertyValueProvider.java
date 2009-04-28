/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import java.util.HashSet;
import java.util.Set;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.apache.commons.collections.CollectionUtils;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationNullPointerException;

/**
 * @author patmoore
 * @param <FA>
 *
 */
public abstract class AbstractFlowPropertyValueProvider<FA extends FlowActivity> implements FlowPropertyValueProvider<FA> {
    private Set<String> propertiesHandled;

    protected AbstractFlowPropertyValueProvider(String...propertiesHandled) {
        this.propertiesHandled = new HashSet<String>();
        CollectionUtils.addAll(this.propertiesHandled, propertiesHandled);
    }

    protected void check(FlowPropertyDefinition flowPropertyDefinition) {
        for(String propertyName: propertiesHandled) {
            if (flowPropertyDefinition.isNamed(propertyName)) {
                return;
            }
        }
        throw new IllegalArgumentException(flowPropertyDefinition+": is not handled by "+this.getClass().getCanonicalName()+" only "+propertiesHandled);
    }
    /**
     * avoids infinite loop by detecting when attempting to get the property that the FlowPropertyValueProvider is supposed to be supplying.
     * @param <T>
     * @param flowActivity
     * @param flowPropertyDefinition
     * @param propertyName
     * @return null if {@link FlowPropertyDefinition#isNamed(String)} is true otherwise the property retrieved.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getSafe(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition, String propertyName) {
        if ( flowPropertyDefinition.isNamed(propertyName)) {
            return null; // TODO throw exception?
        } else {
            return (T) flowActivity.getProperty(propertyName);
        }
    }
    /**
     *
     * @param <T>
     * @param flowActivity
     * @param flowPropertyDefinition
     * @param propertyName
     * @return will not be null.
     */
    protected <T> T getRequired(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition, String propertyName) {
        if ( flowPropertyDefinition.isNamed(propertyName)) {
            throw new ApplicationIllegalArgumentException(propertyName);
        } else {
            T result = (T) flowActivity.getProperty(propertyName);
            if ( result == null ) {
                throw new ApplicationNullPointerException(propertyName);
            }
            return result;
        }
    }
}
