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
package org.amplafi.flow.definitions;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.PropertyUsage;
import org.amplafi.flow.impl.FlowImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sworddance.util.ApplicationGeneralException;

/**
 * @author patmoore
 *
 */
public class XmlDefinitionSource implements DefinitionSource {

    /**
     *
     */
    private static final String PAGE_NAME_ATTR = "pageName";
    /**
     *
     */
    private static final String NAME_ATTR = "name";
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private Document xmlDocument;
    private Map<String, FlowImplementor> flows = new ConcurrentHashMap<String, FlowImplementor>();
    private String fileName;

    public XmlDefinitionSource() {

    }
    public XmlDefinitionSource(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
        parseDocument();
    }
    public XmlDefinitionSource(String fileName) {
        this.fileName = fileName;
        File file = new File(fileName);
        try {
            this.xmlDocument = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().parse(file);
        } catch (SAXException e) {
            throw new ApplicationGeneralException(e);
        } catch (IOException e) {
            throw new ApplicationGeneralException(e);
        } catch (ParserConfigurationException e) {
            throw new ApplicationGeneralException(e);
        }
        parseDocument();
    }
    private void parseDocument() {
        NodeList flowDefinitions = this.xmlDocument.getElementsByTagName("definition");
        for(int i = 0; i < flowDefinitions.getLength(); i++ ) {
            FlowImplementor flow = parseFlow(flowDefinitions.item(i));
            this.flows.put(flow.getFlowPropertyProviderName(), flow);
        }
    }
    /**
     * @param flowNode
     */
    private FlowImplementor parseFlow(Node flowNode) {
        NamedNodeMap attributes = flowNode.getAttributes();
        String name = getNameAttribute(attributes);
        FlowImpl flow = new FlowImpl(name);
        for(int index = 0; index< attributes.getLength(); index++) {
            Node attribute = attributes.item(index);
            String nodeName = attribute.getNodeName();
            String nodeValue = attribute.getNodeValue();
            if ( "link-title".equals(nodeName)) {
                flow.setLinkTitle(nodeValue);
            } else if ( "default-after-page".equals(nodeName)) {
                flow.setDefaultAfterPage(nodeValue);
            } else if ("flow-title".equals(nodeName)) {
                flow.setFlowTitle(nodeValue);
            } else if ( "continue-link-title".equals(nodeName)) {
                flow.setContinueFlowTitle(nodeValue);
            } else if ( PAGE_NAME_ATTR.equals(nodeName)) {
                flow.setPageName(nodeValue);
            } else if ( "activatable".equals(nodeName)) {
                flow.setActivatable(Boolean.parseBoolean(nodeValue));
            } else if ("not-current-allowed".equals(nodeName)) {
                flow.setNotCurrentAllowed(Boolean.parseBoolean(nodeValue));
            }
        }
        NodeList children = flowNode.getChildNodes();
        for(int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if ( "property".equals(child.getNodeName())) {
                flow.addPropertyDefinition(parseProperty(child));
            } else if ("activity".equals(child.getNodeName())) {
                flow.addActivity(parseStep(child));
            }
        }
        return flow;
    }
    /**
     * @param attributes
     * @return
     */
    private String getNameAttribute(NamedNodeMap attributes) {
        return getAttributeString(attributes, NAME_ATTR);
    }
    private String getAttributeString(NamedNodeMap attributes, String attributeName) {
        Node node = attributes.getNamedItem(attributeName);
        String attributeValue = node.getNodeValue();
        return attributeValue;
    }
    /**
     * @param child
     */
    @SuppressWarnings("unchecked")
    private FlowActivityImplementor parseStep(Node flowActivityImplementorNode) {
        NamedNodeMap attributes = flowActivityImplementorNode.getAttributes();
        String className = getAttributeString(attributes, "class");
        Class<FlowActivityImplementor> clazz;
        FlowActivityImplementor flowActivity;
        try {
            clazz = (Class<FlowActivityImplementor>) Class.forName(className);
            flowActivity = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ApplicationGeneralException(e);
        } catch (InstantiationException e) {
            throw new ApplicationGeneralException(e);
        } catch (IllegalAccessException e) {
            throw new ApplicationGeneralException(e);
        }
        String name = getNameAttribute(attributes);
        flowActivity.setFlowPropertyProviderName(name);
        for(int index = 0; index< attributes.getLength(); index++) {
            Node attribute = attributes.item(index);
            String nodeName = attribute.getNodeName();
            String nodeValue = attribute.getNodeValue();
            Boolean booleanValue = Boolean.parseBoolean(nodeValue);
            if ( PAGE_NAME_ATTR.equals(nodeName)) {
                flowActivity.setPageName(nodeValue);
            } else if ( "componentName".equals(nodeName)) {
                flowActivity.setComponentName(nodeValue);
            } else if ( "link-title".equals(nodeName)) {
                flowActivity.setActivityTitle(nodeValue);
            } else if ( "finishing".equals(nodeName)) {
                flowActivity.setFinishingActivity(booleanValue);
            } else if ( "invisible".equals(nodeName)) {
                flowActivity.setInvisible(booleanValue);
            } else if ( "persistFlow".equals(nodeName)) {
                flowActivity.setPersistFlow(booleanValue);
            }
        }
        NodeList children = flowActivityImplementorNode.getChildNodes();
        for(int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if ( "property".equals(child.getNodeName())) {
                flowActivity.addPropertyDefinition(parseProperty(child));
            }
        }
        return flowActivity;
    }
    private FlowPropertyDefinitionImplementor parseProperty(Node flowPropertyDefinitionNode) {
        NamedNodeMap attributes = flowPropertyDefinitionNode.getAttributes();
        String name = getNameAttribute(attributes);
        FlowPropertyDefinitionImpl flowPropertyDefinition = new FlowPropertyDefinitionImpl(name);
        for(int index = 0; index< attributes.getLength(); index++) {
            Node attribute = attributes.item(index);
            String nodeName = attribute.getNodeName();
            String nodeValue = attribute.getNodeValue();
            if ( "initial".equals(nodeName)) {
                flowPropertyDefinition.setInitial(nodeValue);
            } else if ( "default".equals(nodeName)) {
                flowPropertyDefinition.setDefaultObject(nodeValue);
            } else if ("data-class".equals(nodeName)) {

                Class<? extends Object> dataClass;
                try {
                    dataClass = Class.forName(nodeValue, true, this.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new ApplicationGeneralException(e);
                }
                flowPropertyDefinition.setDataClass(dataClass);
            } else if ( "usage".equals(nodeName)) {
                PropertyUsage propertyUsage = PropertyUsage.valueOf(nodeValue);
                flowPropertyDefinition.setPropertyUsage(propertyUsage);
            } else if ( "parameterName".equals(nodeName)) {
                flowPropertyDefinition.setUiComponentParameterName(nodeValue);
            }
        }
        return flowPropertyDefinition;
    }
    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinition(java.lang.String)
     */
    @Override
    public FlowImplementor getFlowDefinition(String flowTypeName) {
        return this.flows.get(flowTypeName);
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinitions()
     */
    @Override
    public Map<String, FlowImplementor> getFlowDefinitions() {
        return this.flows;
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#isFlowDefined(java.lang.String)
     */
    @Override
    public boolean isFlowDefined(String flowTypeName) {
        return this.flows.containsKey(flowTypeName);
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
