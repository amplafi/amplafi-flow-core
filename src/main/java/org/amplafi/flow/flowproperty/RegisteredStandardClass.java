package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowActivityPhase;


/**
 * IDEA only (not used)
 * This Annotation is attached to classes
 *
 * {@link org.amplafi.flow.FlowActivityPhase} is NOT available because this is unlikely to be constant for all most flows so defining at annotation level is probably bad?
 * Exception UserImpl objects?
 *
 * Note: a class that is a clone of this annotation might be useful. Sigh, cannot new an annotation...
 * @author patmoore
 *
 */
public @interface RegisteredStandardClass {
	/**
	 * The default is the property name. Any "Impl" is removed.
	 * @return the propertyName
	 */
	String propertyName() default "";
	ExternalPropertyAccessRestriction defaultExternalPropertyAccessRestriction() default ExternalPropertyAccessRestriction.noRestrictions;
	PropertyUsage defaultPropertyUsage() default PropertyUsage.use;
	/**
	 * very rarely needed. only real exception is for security/account objects ( like UserImpl ) which needs to always be available
	 * @return
	 */
	FlowActivityPhase defaultFlowActivityPhase() default FlowActivityPhase.optional;
	/**
	 * A class can implement many interfaces,
	 * @return the interfaces this should match.
	 */
	Class<?>[] interfaces() default {};
}
