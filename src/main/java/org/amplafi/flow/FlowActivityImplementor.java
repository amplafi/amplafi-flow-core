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

package org.amplafi.flow;

/**
 * @author patmoore
 *
 */
public interface FlowActivityImplementor extends FlowActivity {
    /**
     * Called when a property changes value.
     *
     * @param flowActivityName
     * @param key
     * @param newValue the new raw property value
     * @param oldValue the old raw property value.
     * @return what the value should be. Usually just return the value
     *         parameter. By default should be 'value'.
     */
    String propertyChange(String flowActivityName, String key, String newValue, String oldValue);

    /**
     * @return instance of this definition
     */
    FlowActivityImplementor createInstance();

    /**
     * @param nextFlow
     * @return the nextFlow after all property substitution has had a change to find the real name.
     */
    String resolve(String nextFlow);

    /**
     *
     */
    void processDefinitions();
    /**
     * If the property has no value stored in the flowState's keyvalueMap then
     * put the supplied value in it.
     *
     * @param key
     * @param value
     * @see #isPropertyNotSet(String)
     */
    void initPropertyIfNull(String key, Object value) ;

    void initPropertyIfBlank(String key, Object value);

    void addPropertyDefinitions(Iterable<FlowPropertyDefinition> flowPropertyDefinitions);

    /**
     * @param activityName The activityName to set.
     */
    void setActivityName(String activityName);

    String getRawProperty(String key);

    boolean isPropertyNotBlank(String key);

    boolean isPropertyBlank(String key);

}
