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
package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowState;

/**
 * Controls visibility and external access to the property while the flow is active.
 *
 *
 * ExternalPropertyAccessRestriction determines if setProperty() or getProperty() are allowed via external clients.
 *
 * In order for ExternalPropertyAccessRestriction to be checked properly, external client property changes should have a different
 * entry point than internal changes.
 *
 * ExternalPropertyAccessRestriction does NOT affect {@link FlowPropertyValueProvider} setting a value
 * This differs from how the property is initialized and exported ( {@link PropertyUsage} ) and how
 * broad any changes to the property are spread. ( PropertyScope )
 * but this may overlap with {@link PropertyUsage#isExternallySettable()}
 *
 * TODO: what about security levels based on if the data is provided by a potentially tainted source (clients) vs. another flow?
 * so "external" means another flow and "client" means to a browser client or api client.
 *
 * Still experimental - not implemented.
 * @author patmoore
 *
 */
public enum ExternalPropertyAccessRestriction {
    /**
     * can be accessed externally both for modification, viewing
     */
    noRestrictions(true, true),
    /**
     * property value is visible external to the user. property value cannot be modified even during the Flow. The change prohibition during the flow
     * makes this different than {@link PropertyUsage#use}
     */
    readonly(true, false),
    /**
     * this is typically used for passwords (or any other security credential )
     * User is allowed to send in the password but not allowed to see stored passwords.
     *
     * This does not affect internal flow code which is always permitted get/set access.
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
    private ExternalPropertyAccessRestriction(boolean externalReadAccess, boolean externalWriteAccess) {
        this.externalReadAccessAllowed = externalReadAccess;
        this.externalWriteAccessAllowed = externalWriteAccess;
    }
    /**
     * @return if false, {@link FlowState#getProperty(String)} method should return null rather than return a result.
     *
     */
    public boolean isExternalReadAccessAllowed() {
        return externalReadAccessAllowed;
    }
    /**
     * Should security check happen at different point?
     * @return if false, {@link FlowState#setProperty(String, Object)} will be ignored. If true and {@link #isExternalReadAccessAllowed()} is false then
     * care must be taken to not accidently
     */
    public boolean isExternalWriteAccessAllowed() {
        return externalWriteAccessAllowed;
    }
    public boolean isDocumented() {
        return isExternalReadAccessAllowed() || isExternalWriteAccessAllowed();
    }
}
