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

/**
 * @author patmoore
 *
 */
public interface FlowPropertyProviderWithValues extends FlowPropertyProvider {

    /**
     * override to treat some properties as special. This method is called by
     * FlowPropertyBinding.
     *
     * @param key
     * @param <T> type of property.
     * @return property
     */
    <T> T getProperty(String key);

    /**
     * override to treat some properties as special. This method is called by
     * FlowPropertyBinding. Default behavior caches value and sets the property
     * to value.
     *
     * @param key
     * @param value
     * @param <T> value's type
     * @throws UnsupportedOperationException if property modification is not supported.
     * @throws IllegalStateException if the property cannot be modified ( but other properties maybe could be modified )
     */
    <T> void setProperty(String key, T value) throws UnsupportedOperationException, IllegalStateException;

}
