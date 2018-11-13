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

import java.util.EventListener;
import java.util.Optional;

import org.amplafi.flow.FlowPropertyDefinition;

/**
 *
 * Implementors register to be notified when a property changes its value. This includes the initialization.
 * TODO: really need a propertyChange method that gets the original objects.
 * TODO: maybe extend {@link java.beans.PropertyChangeListener}? PropertyChangeEvent is not very different than current propertyChange()
 * even has same method :-)
 * TODO: investigate use of {@link java.beans.VetoableChangeListener} to allow vetoing.
 * @author patmoore
 *
 */
public interface FlowPropertyValueChangeListener extends EventListener {
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
    <T> Optional<T> propertyChange(FlowPropertyProvider flowPropertyProvider, String namespace, FlowPropertyDefinition flowPropertyDefinition,
            Optional<T> newValue, Optional<T> oldValue);
}
