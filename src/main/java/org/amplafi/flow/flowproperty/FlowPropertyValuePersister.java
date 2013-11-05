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

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;

/**
 * Implementors know how to save changes to the object represented by the FlowProperty in persistent storage.
 *
 * Functionality to save property changes to the database is broken out from the functionality to retrieve the property.
 *
 * This allows the read-only {@link FlowPropertyDefinition#isReadOnly()} properties to be enforced as read-only: they have no persister and thus changes can not be saved to the database.
 *
 * This may including creating a brand new object, or the persister may be restricted to only changing an existing object.
 *
 * Use case: Changing a user's first name/last name is completely different than creating a new user. Creating a new user must be more deliberate.
 * Not all objects need to be so carefully considered.
 * @see org.amplafi.flow.FlowPropertyValueProvider
 * @author patmoore
 * @param <T>
 *
 */
public interface FlowPropertyValuePersister<T extends FlowPropertyProvider> {
    /**
     * @param flowPropertyProvider
     * @param flowPropertyDefinition
     * @return value to be set back to flow state or null set is not needed.
     */
    Object saveChanges(T flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition);

    /**
     * A FlowPropertyValueProvider may not persist everything that it provides.
     * For example, properties that are default values.
     *
     * @param flowPropertyExpectation
     * @return
     */
	boolean isPersisting(FlowPropertyExpectation flowPropertyExpectation);
}
