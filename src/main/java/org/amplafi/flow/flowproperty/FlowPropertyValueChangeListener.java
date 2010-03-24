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
 * @author patmoore
 *
 */
public interface FlowPropertyValueChangeListener {
    /**
     * Called when a property changes value.
     * @param flowPropertyProvider TODO
     * @param namespace
     * @param flowPropertyDefinition TODO
     * @param newValue the new raw property value
     * @param oldValue the old raw property value.
     * @return what the value should be. Usually just return the value
     *         parameter. By default should be 'value'.
     */
    String propertyChange(FlowPropertyProvider flowPropertyProvider, String namespace, FlowPropertyDefinition flowPropertyDefinition, String newValue, String oldValue);
}
