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

/**
 * Generates a plain text link for the FlowLauncher.
 * Need to rework how links are being constructed in FlowEntryPoint
 * @author patmoore
 *
 */
// TODO: see FlowLinkEncoder and reconcile.
public class LaunchLinkGenerator {

    private String servicePrefix;
    public LaunchLinkGenerator() {

    }
    public LaunchLinkGenerator(String servicePrefix) {
        this.servicePrefix = servicePrefix;
    }
    public URI createURI(URI base, FlowLauncher flowLauncher) {
        StringBuilder uriBuilder = new StringBuilder(this.servicePrefix).append("/").append(flowLauncher.getFlowTypeName());
        if(!(flowLauncher instanceof StartFromDefinitionFlowLauncher)) {
            throw new IllegalArgumentException("Can not yet handle anything else correctly through BaseFlowService "+flowLauncher);
        }
        if ( flowLauncher instanceof MorphFlowLauncher) {
            // HACK
            uriBuilder.append("/").append(((MorphFlowLauncher)flowLauncher).getKeyExpression().toString());
        } else if ( flowLauncher instanceof ContinueFlowLauncher) {
            // HACK
            uriBuilder.append("/").append(((ContinueFlowLauncher)flowLauncher).getKeyExpression().toString());
        }
        uriBuilder.append("?");
        uriBuilder.append(UriFactoryImpl.createQueryString( flowLauncher.getInitialFlowState()));
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
