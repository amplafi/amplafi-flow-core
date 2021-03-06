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

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowConfigurationException;
import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowGroup;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.PropertyScope;
import org.amplafi.flow.flowproperty.PropertyUsage;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowGroupImpl;
import org.amplafi.flow.impl.FlowImpl;
import org.amplafi.flow.impl.TransitionFlowActivity;
import com.sworddance.util.AbstractXmlParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static org.apache.commons.lang.StringUtils.*;
/**
 * @author patmoore
 */
public class XmlDefinitionSource extends AbstractXmlParser implements DefinitionSource<FlowImplementor> {
    /**
     * element name that defines a property.
     */
    private static final String PROPERTY = "property";

    /**
     *
     */
    private static final String ACTIVATABLE = "activatable";

    /**
     *
     */
    private static final String CONTINUE_LINK_TITLE = "continue-link-title";

    /**
     *
     */
    private static final String FLOW_TITLE = "flow-title";

    /**
     *
     */
    private static final String TRANSITION = "transition";

    /**
     *
     */
    private static final String INVISIBLE = "invisible";

    /**
     *
     */
    private static final String FINISHING = "finishing";

    /**
     *
     */
    private static final String ACTIVITY = "activity";

    /**
     *
     */
    private static final String DEFINITION = "definition";

    /**
     *
     */
    private static final String CLASS = "class";

    /**
     * UI component name - any string that is meaningful to the UI/webframework.
     */
    private static final String COMPONENT_NAME = "component-name";

    /**
     * UI page name - some webframeworks separate the concept of page from component ( tapestry )
     */
    private static final String PAGE_NAME_ATTR = "page-name";

    /**
     * the text used in a link to the flow or flowActivity
     */
    private static final String LINK_TITLE = "link-title";

    /**
     * name of the flow or flowActivity.
     */
    private static final String NAME_ATTR = "name";

    private Map<String, FlowImplementor> flows = new ConcurrentHashMap<String, FlowImplementor>();

    public XmlDefinitionSource() {

    }

    public XmlDefinitionSource(Document xmlDocument) {
        super(xmlDocument);
        parseDocument();
    }

    public XmlDefinitionSource(String fileName) {
        super(fileName, "flows");
        parseDocument();
    }


    public XmlDefinitionSource(InputStream inputStream) {
        super(inputStream);
        parseDocument();
    }

    private void parseDocument() {
        FlowGroup flowGroup = parseFlowGroup(this.getDocumentElement());
    }


    private FlowGroup parseFlowGroup(Node flowGroupNode) {
    	NamedNodeMap attributes = flowGroupNode.getAttributes();
    	String flowGroupId = getNameAttribute(attributes);
        FlowGroupImpl flowGroup = new FlowGroupImpl(flowGroupId);
        NodeList children = this.getChildElementsByTagName(flowGroupNode, false, PROPERTY, DEFINITION);
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            String nodeName = child.getNodeName();
            if (PROPERTY.equals(nodeName)) {
                flowGroup.addPropertyDefinitions(parseProperty(child, PropertyScope.global));
            } else if (DEFINITION.equals(nodeName)) {
                FlowImplementor flow = parseFlow(child);
                this.flows.put(flow.getFlowPropertyProviderName(), flow);
            } else {
                //                    System.out.println(nodeName);
            }
        }
        return flowGroup;
    }

    /**
     * @param flowNode
     */
    @SuppressWarnings("unchecked")
    private FlowImplementor parseFlow(Node flowNode) {
        FlowImplementor flow;
        NamedNodeMap attributes = flowNode.getAttributes();
        String name = getNameAttribute(attributes);
        String className = getAttributeString(attributes, CLASS);
        if ( className == null ) {
            flow = new FlowImpl(name);
        } else {
            Class<FlowImplementor> clazz;
            try {
                clazz = (Class<FlowImplementor>) Class.forName(className);
                flow = clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new FlowConfigurationException(e);
            } catch (InstantiationException e) {
                throw new FlowConfigurationException(e);
            } catch (IllegalAccessException e) {
                throw new FlowConfigurationException(e);
            }
            flow.setFlowPropertyProviderName(name);
        }
        for (int index = 0; index < attributes.getLength(); index++) {
            Node attribute = attributes.item(index);
            String nodeName = attribute.getNodeName();
            String nodeValue = attribute.getNodeValue();
            Boolean booleanValue = Boolean.parseBoolean(nodeValue);
            if ( CLASS.equals(nodeName) || NAME_ATTR.equals(nodeName)) {
                continue;
            } else if (LINK_TITLE.equals(nodeName)) {
                flow.setLinkTitle(nodeValue);
            } else if ("default-after-page".equals(nodeName)) {
                flow.setDefaultAfterPage(nodeValue);
            } else if (FLOW_TITLE.equals(nodeName)) {
                flow.setFlowTitle(nodeValue);
            } else if (CONTINUE_LINK_TITLE.equals(nodeName)) {
                flow.setContinueFlowTitle(nodeValue);
            } else if (PAGE_NAME_ATTR.equals(nodeName)) {
                flow.setPageName(nodeValue);
            } else if (ACTIVATABLE.equals(nodeName)) {
                flow.setActivatable(booleanValue);
            } else if ("not-current-allowed".equals(nodeName)) {
                flow.setNotCurrentAllowed(booleanValue);
            } else {
                throw new FlowConfigurationException("attribute is unknown "+attribute);
            }
        }
        NodeList children = flowNode.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            switch ( child.getNodeType()) {
            case Node.ELEMENT_NODE:
                String nodeName = child.getNodeName();
                if (PROPERTY.equals(nodeName)) {
                    flow.addPropertyDefinitions(parseProperty(child, PropertyScope.flowLocal));
                } else if (ACTIVITY.equals(nodeName)) {
                    flow.addActivity(parseStep(child, false));
                } else if (TRANSITION.equals(nodeName)) {
                    flow.addActivity(parseStep(child, true));
                } else {
                    throw new IllegalArgumentException("element is unknown "+child);
                }
                break;
            case Node.TEXT_NODE:
                String text = flow.getFlowDescriptionText();
                String textContent = child.getTextContent();
                if ( isNotBlank(textContent)) {
                    if ( isBlank(text)) {
                        flow.setFlowDescriptionText(textContent);
                    } else {
                        flow.setFlowDescriptionText(text.trim()+" "+textContent);
                    }
                }
                break;
            default:
                break;
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

    private FlowActivityImplementor parseStep(Node flowActivityImplementorNode, boolean transition) {
        FlowActivityImplementor flowActivity = createFlowActivityImplementor(flowActivityImplementorNode, transition);
        NamedNodeMap attributes = flowActivityImplementorNode.getAttributes();
        for (int index = 0; index < attributes.getLength(); index++) {
            Node attribute = attributes.item(index);
            String nodeName = attribute.getNodeName();
            String nodeValue = attribute.getNodeValue();
            boolean booleanValue = Boolean.parseBoolean(nodeValue);
            if ( CLASS.equals(nodeName) || NAME_ATTR.equals(nodeName)) {
                continue;
            } else if (PAGE_NAME_ATTR.equals(nodeName)) {
                flowActivity.setPageName(nodeValue);
            } else if (COMPONENT_NAME.equals(nodeName)) {
                flowActivity.setComponentName(nodeValue);
            } else if (LINK_TITLE.equals(nodeName)) {
                flowActivity.setActivityTitle(nodeValue);
            } else if (FINISHING.equals(nodeName)) {
                flowActivity.setFinishingActivity(booleanValue);
            } else if (INVISIBLE.equals(nodeName)) {
                flowActivity.setInvisible(booleanValue);
            } else if ( !transition ) {
                throw new FlowException("attribute is unknown "+attribute);
            } else if ( "finish-key".equals(nodeName)) {
                ((TransitionFlowActivity)flowActivity).setFinishKey(nodeValue);
            } else if ( "transition-label".equals(nodeName)) {
                ((TransitionFlowActivity)flowActivity).setFinishKey(nodeValue);
            } else if ( "type".equals(nodeName)) {
                ((TransitionFlowActivity)flowActivity).setType(nodeValue);
            } else if ( "nextFlow".equals(nodeName)) {
                ((TransitionFlowActivity)flowActivity).setNextFlowType(nodeValue);
            } else {
                throw new FlowConfigurationException("attribute is unknown "+attribute);
            }
        }
        NodeList children = flowActivityImplementorNode.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            switch(child.getNodeType()) {
            case Node.ELEMENT_NODE:
                if (PROPERTY.equals(child.getNodeName())) {
                    flowActivity.addPropertyDefinitions(parseProperty(child, PropertyScope.activityLocal));
                } else {
                    throw new FlowConfigurationException("element is unknown "+child);
                }
                break;
            case Node.TEXT_NODE:
                //                String text = flow.getFlowDescriptionText();
                //                String textContent = child.getTextContent();
                //                if ( isNotBlank(textContent)) {
                //                    if ( isBlank(text)) {
                //                        flowActivity.setFlowDescriptionText(textContent);
                //                    } else {
                //                        flowActivity.setFlowDescriptionText(text.trim()+" "+textContent);
                //                    }
                //                }
                break;
            default:
                break;
            }
        }
        return flowActivity;
    }

    /**
     * @param transition TODO
     * @param attributes
     * @return
     */
    @SuppressWarnings("unchecked")
    private FlowActivityImplementor createFlowActivityImplementor(Node flowActivityImplementorNode, boolean transition) {
        NamedNodeMap attributes = flowActivityImplementorNode.getAttributes();
        String name = getNameAttribute(attributes);
        String className = getAttributeString(attributes, CLASS);
        FlowActivityImplementor flowActivity;
        if ( isNotBlank(className)) {
            Class<FlowActivityImplementor> clazz;
            try {
                clazz = (Class<FlowActivityImplementor>) Class.forName(className);
                flowActivity = clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new FlowConfigurationException(e);
            } catch (InstantiationException e) {
                throw new FlowConfigurationException(e);
            } catch (IllegalAccessException e) {
                throw new FlowConfigurationException(e);
            }
        } else if(transition) {
            flowActivity = new TransitionFlowActivity();
        } else {
            flowActivity = new FlowActivityImpl();
        }
        flowActivity.setFlowPropertyProviderName(name);
        return flowActivity;
    }

    private FlowPropertyDefinitionBuilder parseProperty(Node flowPropertyDefinitionNode, PropertyScope propertyScope) {
        NamedNodeMap attributes = flowPropertyDefinitionNode.getAttributes();
        String name = getNameAttribute(attributes);
        // TODO : use the FlowPropertyDefinitionBuilder
        FlowPropertyDefinitionBuilder flowPropertyDefinition = new FlowPropertyDefinitionBuilder(name).initPropertyScope(propertyScope);
        for (int index = 0; index < attributes.getLength(); index++) {
            Node attribute = attributes.item(index);
            String nodeName = attribute.getNodeName();
            String nodeValue = attribute.getNodeValue();
            if ( NAME_ATTR.equals(nodeName)) {
                continue;
            } else if ("initial".equals(nodeName)) {
                flowPropertyDefinition.setInitial(nodeValue);
            } else if ("default".equals(nodeName)) {
                flowPropertyDefinition.initDefaultObject(nodeValue);
            } else if("translator".equals(nodeName)) {
                // TODO: need to do flowTranslator injection.
            } else if ("data-class".equals(nodeName)) {
                Class<? extends Object> dataClass;
                try {
                    dataClass = Class.forName(nodeValue, true, this.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new FlowConfigurationException(e);
                }
                flowPropertyDefinition.setDataClass(dataClass);
            } else if ("usage".equals(nodeName)) {
                PropertyUsage propertyUsage = PropertyUsage.valueOf(nodeValue);
                flowPropertyDefinition.initPropertyUsage(propertyUsage);
            } else {
                throw new FlowConfigurationException(nodeName, ": unknown attribute on ", flowPropertyDefinitionNode);
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
}
