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

/**
 * Implementors know how to save changes to the object represented by the FlowProperty in persistent storage.
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
    void saveChanges(T flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition);

	boolean isHandling(FlowPropertyDefinition flowPropertyDefinition);
}
