package org.amplafi.flow.impl;

import static org.amplafi.flow.FlowConstants.FSACTIVATABLE;
import static org.amplafi.flow.FlowConstants.FSAFTER_PAGE;
import static org.amplafi.flow.FlowConstants.FSALT_FINISHED;
import static org.amplafi.flow.FlowConstants.FSAPI_CALL;
import static org.amplafi.flow.FlowConstants.FSAUTO_COMPLETE;
import static org.amplafi.flow.FlowConstants.FSCONTINUE_WITH_FLOW;
import static org.amplafi.flow.FlowConstants.FSDEFAULT_AFTER_CANCEL_PAGE;
import static org.amplafi.flow.FlowConstants.FSDEFAULT_AFTER_PAGE;
import static org.amplafi.flow.FlowConstants.FSFINISH_TEXT;
import static org.amplafi.flow.FlowConstants.FSFLOW_TRANSITIONS;
import static org.amplafi.flow.FlowConstants.FSHIDE_FLOW_CONTROL;
import static org.amplafi.flow.FlowConstants.FSIMMEDIATE_SAVE;
import static org.amplafi.flow.FlowConstants.FSNEXT_FLOW;
import static org.amplafi.flow.FlowConstants.FSNO_CANCEL;
import static org.amplafi.flow.FlowConstants.FSPAGE_NAME;
import static org.amplafi.flow.FlowConstants.FSREDIRECT_URL;
import static org.amplafi.flow.FlowConstants.FSREFERRING_URL;
import static org.amplafi.flow.FlowConstants.FSRETURN_TO_FLOW;
import static org.amplafi.flow.FlowConstants.FSRETURN_TO_FLOW_TYPE;
import static org.amplafi.flow.FlowConstants.FSRETURN_TO_TEXT;
import static org.amplafi.flow.FlowConstants.FSSUGGESTED_NEXT_FLOW_TYPE;
import static org.amplafi.flow.FlowConstants.FSTITLE_TEXT;
import static org.amplafi.flow.flowproperty.PropertyScope.flowLocal;
import static org.amplafi.flow.flowproperty.PropertyUsage.consume;
import static org.amplafi.flow.flowproperty.PropertyUsage.internalState;
import static org.amplafi.flow.flowproperty.PropertyUsage.io;
import static org.amplafi.flow.flowproperty.PropertyUsage.use;

import java.net.URI;
import java.util.Map;

import org.amplafi.flow.FlowTransition;
import org.amplafi.flow.flowproperty.AbstractFlowPropertyDefinitionProvider;
import org.amplafi.flow.flowproperty.CancelTextFlowPropertyValueProvider;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider;
import org.amplafi.flow.flowproperty.FlowTransitionFlowPropertyValueProvider;
import org.amplafi.flow.flowproperty.MessageFlowPropertyValueProvider;

import com.sworddance.util.ApplicationIllegalStateException;
/**
 * This map is used to connect a standard property name i.e. "user" to a standard class (UserImpl)
 * This map solves the problem where a flowProperty*Provider or changelistener needs (or would like)
 * to have a property available but does not define it.
 *
 * Explicit NOTE: the flow propertydefinitions returned MUST not persist any changes to permanent storage.
 * This is easy to enforce at the primary level (i.e. no persister is called. ) but what about accessing a read-only property
 * that returns a db object and then changes the db object? Can we tell hibernate not to persist?
 */
/**
 * Used to provide standard flow properties that are used every where
 *
 * @author patmoore
 *
 */
public class FactoryFlowPropertyDefinitionProvider extends AbstractFlowPropertyDefinitionProvider implements FlowPropertyDefinitionProvider {

    public static final FactoryFlowPropertyDefinitionProvider INSTANCE = new FactoryFlowPropertyDefinitionProvider(
        new FlowPropertyDefinitionImpl(FSTITLE_TEXT).initAccess(flowLocal, use).initFactoryFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
        new FlowPropertyDefinitionImpl(FSNO_CANCEL, boolean.class).initAccess(flowLocal, use),
        new FlowPropertyDefinitionImpl(FSFINISH_TEXT).initAccess(flowLocal, use).initFactoryFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
        new FlowPropertyDefinitionImpl(FSRETURN_TO_TEXT).initAccess(flowLocal, use).initFactoryFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
        // io -- for now because need to communicate the next page to be displayed
        // TODO think about PropertyScope/PropertyUsage
        new FlowPropertyDefinitionImpl(FSPAGE_NAME).initPropertyUsage(io),
        // TODO think about PropertyScope/PropertyUsage
        new FlowPropertyDefinitionImpl(FSAFTER_PAGE).initPropertyUsage(io),
        new FlowPropertyDefinitionImpl(FSDEFAULT_AFTER_PAGE).initAccess(flowLocal, internalState),
        new FlowPropertyDefinitionImpl(FSDEFAULT_AFTER_CANCEL_PAGE).initAccess(flowLocal, internalState),
        new FlowPropertyDefinitionImpl(FSHIDE_FLOW_CONTROL, boolean.class).initPropertyScope(flowLocal),
        new FlowPropertyDefinitionImpl(FSACTIVATABLE, boolean.class).initAccess(flowLocal, consume),
        new FlowPropertyDefinitionImpl(FSIMMEDIATE_SAVE, boolean.class).initAccess(flowLocal, internalState),

        new FlowPropertyDefinitionImpl(FSAPI_CALL, boolean.class).initAccess(flowLocal, io),
        new FlowPropertyDefinitionImpl(FSAUTO_COMPLETE, boolean.class).initAccess(flowLocal, internalState),
        new FlowPropertyDefinitionImpl(FSALT_FINISHED).initAccess(flowLocal, use),
        new FlowPropertyDefinitionImpl(FSREDIRECT_URL, URI.class).initPropertyUsage(io),
        new FlowPropertyDefinitionImpl(FSREFERRING_URL, URI.class).initPropertyUsage(use),
        new FlowPropertyDefinitionImpl(FSCONTINUE_WITH_FLOW).initPropertyUsage(io),
        new FlowPropertyDefinitionImpl(FSFLOW_TRANSITIONS, FlowTransition.class, Map.class).initAutoCreate().initAccess(flowLocal, use),
        // HACK
        FlowTransitionFlowPropertyValueProvider.FLOW_TRANSITION.clone(),

        new FlowPropertyDefinitionImpl(FSRETURN_TO_FLOW).initPropertyUsage(io),
        new FlowPropertyDefinitionImpl(FSRETURN_TO_FLOW_TYPE).initPropertyUsage(io),
        new FlowPropertyDefinitionImpl(FSSUGGESTED_NEXT_FLOW_TYPE, FlowTransition.class, Map.class).initAutoCreate().initAccess(flowLocal, use),
        // TODO think about PropertyScope/PropertyUsage
        new FlowPropertyDefinitionImpl(FSNEXT_FLOW).initPropertyUsage(io),
        // HACK
        CancelTextFlowPropertyValueProvider.CANCEL_TEXT.clone().initFactoryFlowPropertyValueProvider(CancelTextFlowPropertyValueProvider.INSTANCE)
    );
    public FactoryFlowPropertyDefinitionProvider(FlowPropertyDefinitionImplementor...flowPropertyDefinitions) {
        super(flowPropertyDefinitions);
    }
    /**
    *
    * @param propertyName
    * @param standardDefinitionClass
    */
   public void addStandardPropertyDefinition(String propertyName, Class<?> standardDefinitionClass) {
       FlowPropertyDefinitionImpl flowPropertyDefinition = new FlowPropertyDefinitionImpl(propertyName, standardDefinitionClass);
       addStandardPropertyDefinition(flowPropertyDefinition);
   }

   /**
    * This property should be minimal.
    * @param flowPropertyDefinition supply the default property.
    */
   public void addStandardPropertyDefinition(FlowPropertyDefinitionImplementor flowPropertyDefinition) {
       String propertyName = flowPropertyDefinition.getName();
       ApplicationIllegalStateException.checkState(!this.getFlowPropertyDefinitions().containsKey(propertyName), propertyName, " already defined as a standard property.");
       flowPropertyDefinition.setTemplateFlowPropertyDefinition();
       this.addFlowPropertyDefinitionImplementators(flowPropertyDefinition);
       // Note: alternate names are not automatically added.
   }

}
