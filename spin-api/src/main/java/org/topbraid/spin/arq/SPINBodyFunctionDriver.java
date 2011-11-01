package org.topbraid.spin.arq;

import org.topbraid.spin.model.Function;
import org.topbraid.spin.system.SPINModuleRegistry;


/**
 * A (default) SPINFunctionDriver using spin:body to find an executable
 * body for a SPIN function.
 * 
 * @author Holger Knublauch
 */
public class SPINBodyFunctionDriver implements SPINFunctionDriver {

	@Override
	public SPINFunctionFactory create(Function spinFunction, SPINModuleRegistry registry) {
		return doCreate(spinFunction, registry);
	}
	
	
	public static SPINFunctionFactory doCreate(Function spinFunction, SPINModuleRegistry registry) {
		return new SPINARQFunction(spinFunction, registry);
	}
}
