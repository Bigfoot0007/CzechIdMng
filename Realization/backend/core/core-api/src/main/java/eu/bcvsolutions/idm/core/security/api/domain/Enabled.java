package eu.bcvsolutions.idm.core.security.api.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.service.ModuleService;

/**
 * Checks, if given modules or configuration properties are enabled before method invocation.
 * 
 * @author Radek Tomiška
 * @see ModuleDescriptor
 * @see ModuleService
 * @see ConfigurationService
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Enabled {
	
	/**
	 * Module ids (alias)
	 * 
	 * @return
     * @see ModuleService
	 */
	@AliasFor("module")
	String[] value() default {};
	
	/**
	 * Module ids
	 * 
	 * @return
     * @see ModuleService
	 */	
	@AliasFor("value")
	String[] module() default {};
	
	/**
	 * configuration property value
	 * 
	 * @return
     * @see ConfigurationService
	 */
	String[] property() default {};
}
