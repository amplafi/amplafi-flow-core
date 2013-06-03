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

package org.amplafi.flow.launcher;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.amplafi.flow.FlowConstants.*;
import static org.apache.commons.lang.StringUtils.*;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;
import org.apache.commons.lang.ObjectUtils;

import com.sworddance.util.ApplicationNullPointerException;
import com.sworddance.util.CUtilities;

/**
 *
 *
 */
public abstract class BaseFlowLauncher implements FlowLauncher,
// HACK Need to rework how links are being constructed in FlowEntryPoint to use FlowLauncherLinkGenerator
// right now we are relying on the tapestry serialization mechanism.
    Serializable{
    private transient FlowManagement flowManagement;
    private ConcurrentMap<String, String> valuesMap = new ConcurrentHashMap<String, String>();
    private String flowTypeName;
    /**
     * Used for Listable items. Maybe null but should be set only once.
     */
    private Serializable keyExpression;
    /**
     * Lookup Key to find existing flow. May not be unique within a list so can not be used as the keyExpression.
     */
    private String existingFlowStateLookupKey;
    protected BaseFlowLauncher() {

    }
    /**
     * @param flowTypeName
     * @param valuesMap
     * @param keyExpression
     */
    public BaseFlowLauncher(String flowTypeName, Map<String, String> valuesMap, Serializable keyExpression) {
        this.flowTypeName = flowTypeName;
        this.putAll(valuesMap);
        this.keyExpression = keyExpression;
    }
    /**
     * @param flowTypeName
     * @param flowManagement
     * @param valuesMap
     * @param keyExpression
     */
    public BaseFlowLauncher(String flowTypeName, FlowManagement flowManagement, Map<String, String> valuesMap, Serializable keyExpression) {
        this(flowTypeName, valuesMap, keyExpression);
        this.flowManagement = flowManagement;
    }

    @Override
    public void setFlowManagement(FlowManagement sessionFlowManagement) {
        this.flowManagement = sessionFlowManagement;
    }
    /**
     * No check is done to allow for auto wiring by DI frameworks
     * @return flowManagement that is checked to be not null.
     */
    public FlowManagement getFlowManagement() {
        return flowManagement;
    }
    /**
     * Should be used by subclasses so that check happens
     * @return flowManagement that is checked to be not null.
     */
    protected FlowManagement getFlowManagementWithCheck() {
        ApplicationNullPointerException.notNull(flowManagement,"no flowmanagement object supplied!");
        return flowManagement;
    }
    /**
     * @param lookupKey
     */
    public void setReturnToFlow(String lookupKey) {
        put(FSRETURN_TO_FLOW, lookupKey);
    }
    /**
     * @return the lookupKeyOrBoolean
     */
    public String getReturnToFlow() {
        return getValuesMap().get(FSRETURN_TO_FLOW);
    }
    protected ConcurrentMap<String, String> getValuesMap() {
        return this.valuesMap;
    }
    @Override
    public Map<String, String> getInitialFlowState() {
        return Collections.unmodifiableMap(getValuesMap());
    }

    /**
     * @see org.amplafi.flow.launcher.FlowLauncher#put(java.lang.String, java.lang.String)
     */
    @Override
    public String put(String key, String value) {
        return CUtilities.put(this.getValuesMap(),key, value);
    }
    /**
     * @see org.amplafi.flow.launcher.FlowLauncher#putIfAbsent(java.lang.String, java.lang.String)
     */
    @Override
    public String putIfAbsent(String key, String defaultValue) {
        return this.getValuesMap().putIfAbsent(key, defaultValue);
    }

    public void putAll(Map<? extends String, ? extends String> map) {
        if ( CUtilities.isNotEmpty(map)) {
            this.getValuesMap().putAll(map);
        }
    }
    public String get(Object key) {
        return this.getValuesMap().get(key);
    }
    public String getListDisplayValue() {
        return getLinkTitle();
    }
    @Override
    public String getFlowTypeName() {
        return flowTypeName;
    }
    /**
     * Used when building a FlowLauncher and the exact flow to launch is determined by a user selection.
     * @param flowTypeName
     */
    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }
    public void setLinkTitle(String linkTitle) {
        this.put(FSLINK_TEXT, linkTitle);
    }
    @Override
    public String getLinkTitle() {
        String linkTitle = this.get(FSLINK_TEXT);
        if ( isBlank(linkTitle) ) {
            FlowPropertyProviderWithValues flowPropertyProvider = getFlowState();
            if ( flowPropertyProvider != null ) {
                linkTitle = flowPropertyProvider.getProperty(FSLINK_TEXT, String.class);
            }
            if ( isBlank(linkTitle) && this.flowManagement !=null) {
                Flow flow = getFlowManagementWithCheck().getFlowDefinition(getFlowTypeName());
                linkTitle = flow.getLinkTitle();
            }
        }
        return linkTitle;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+" :" +this.getFlowTypeName()+ " initialValues="+getValuesMap();
    }
    @SuppressWarnings("unchecked")
    protected <FS extends FlowState> FS getFlowState() {
        return (FS) getFlowManagementWithCheck().getFlowState(getExistingFlowStateLookupKey());
    }
    protected String getExistingFlowStateLookupKey() {
        return this.existingFlowStateLookupKey;
    }

    protected void setExistingFlowStateLookupKey(String existingFlowStateLookupKey) {
        this.existingFlowStateLookupKey = existingFlowStateLookupKey;
    }
    public Object getKeyExpression() {
        return this.keyExpression == null?this.getFlowTypeName():this.keyExpression;
    }

    public boolean hasKey(Object key) {
        return ObjectUtils.equals(this.getKeyExpression(), key);
    }
}
