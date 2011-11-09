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

import java.net.URI;
import com.sworddance.util.UriFactoryImpl;
import static org.apache.commons.lang.StringUtils.*;

/**
 * Generates a plain text link for the FlowLauncher.
 * Need to rework how links are being constructed in FlowEntryPoint
 * @author patmoore
 *
 */
// TODO: see FlowLinkEncoder and reconcile.
public class FlowLauncherLinkGeneratorImpl implements FlowLauncherLinkGenerator {

    private String servicePrefix;
    public FlowLauncherLinkGeneratorImpl() {

    }
    public FlowLauncherLinkGeneratorImpl(String servicePrefix) {
        this.servicePrefix = servicePrefix;
    }
    public URI createURI(URI base, FlowLauncher flowLauncher) {
        StringBuilder uriBuilder = new StringBuilder();
        if (this.servicePrefix!= null) {
            uriBuilder.append(this.servicePrefix);
        }
        uriBuilder.append("/").append(flowLauncher.getFlowTypeName());
        if ( flowLauncher instanceof MorphFlowLauncher) {
            // HACK (should be explicitly the FlowStateLookupKey)
            uriBuilder.append("/").append(((MorphFlowLauncher)flowLauncher).getKeyExpression().toString());
            // what would be in the initial state of a morph? perhaps control operations like which flow to morph to?
        } else if ( flowLauncher instanceof ContinueFlowLauncher) {
            // HACK (should be explicitly the FlowStateLookupKey)
            uriBuilder.append("/").append(((ContinueFlowLauncher)flowLauncher).getKeyExpression().toString());
            // what would be in the initial state of a continue? perhaps control operations?
        } else if ( flowLauncher instanceof StartFromDefinitionFlowLauncher ) {
            String createQueryString = UriFactoryImpl.createQueryString( flowLauncher.getInitialFlowState());
            if ( isNotBlank(createQueryString)) {
                uriBuilder.append("?");
                uriBuilder.append(createQueryString);
            }
        }
        String uriStr = uriBuilder.toString();
        if ( base != null ) {
            return base.resolve(uriStr);
        } else {
            return UriFactoryImpl.createUriWithSchemaAndPath(uriStr);
        }
    }
    /**
     * @param servicePrefix the servicePrefix to set
     */
    public void setServicePrefix(String servicePrefix) {
        this.servicePrefix = servicePrefix;
    }
    /**
     * @return the servicePrefix
     */
    public String getServicePrefix() {
        return servicePrefix;
    }
}
