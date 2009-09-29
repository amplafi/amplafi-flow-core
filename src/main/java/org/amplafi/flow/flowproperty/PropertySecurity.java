/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowState;

/**
 * Controls visibility and access to the property while the flow is active.
 * This differs from how the property is initialized and exported ( {@link PropertyUsage} ) and how
 * broad any changes to the property are spread. ( PropertyScope )
 *
 * @author patmoore
 *
 */
public enum PropertySecurity {
    /**
     * can be accessed externally both for modification and viewing
     */
    noRestrictions(true, true),
    /**
     * property value is visible external to the user  ( isn't this {@link PropertyUsage#use}? )
     */
    readonly(true, false),
    /**
     * this is typically used for passwords (or any other security credential )
     * User is allowed to send in the password but not allowed to see stored passwords
     */
    writeonly(false, true),
    /**
     * no access. These are properties that are really internal state only.
     * Property must NOT be included in auto generated documentation.
     *
     * TODO need method to allow setting secured parameters?
     */
    noAccess(false, false);
    private final boolean externalReadAccessAllowed;
    private final boolean externalWriteAccessAllowed;
    /**
     * @param externalReadAccess
     * @param externalWriteAccess
     */
    private PropertySecurity(boolean externalReadAccess, boolean externalWriteAccess) {
        this.externalReadAccessAllowed = externalReadAccess;
        this.externalWriteAccessAllowed = externalWriteAccess;
    }
    /**
     * @return if false, {@link FlowState#getPropertyAsObject(String)} method should return null rather than return a result.
     *
     */
    public boolean isExternalReadAccessAllowed() {
        return externalReadAccessAllowed;
    }
    /**
     * Should security check happen at different point?
     * @return if false, {@link FlowState#setPropertyAsObject(String, Object)} will be ignored. If true and {@link #isExternalReadAccessAllowed()} is false then
     * care must be taken to not accidently
     */
    public boolean isExternalWriteAccessAllowed() {
        return externalWriteAccessAllowed;
    }
}
