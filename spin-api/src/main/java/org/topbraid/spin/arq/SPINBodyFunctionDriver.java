package org.topbraid.spin.arq;

import org.topbraid.spin.model.Function;


/**
 * A (default) SPINFunctionDriver using spin:body to find an executable
 * body for a SPIN function.
 * 
 * @author Holger Knublauch
 */
public class SPINBodyFunctionDriver implements SPINFunctionDriver {

	@Override
	public SPINFunctionFactory create(Function spinFunction) {
		return doCreate(spinFunction);
	}
	
	
	public static SPINFunctionFactory doCreate(Function spinFunction) {
		return new SPINARQFunction(spinFunction);
	}
}
