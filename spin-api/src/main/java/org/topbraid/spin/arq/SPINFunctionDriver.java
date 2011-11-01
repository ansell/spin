/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import org.topbraid.spin.model.Function;
import org.topbraid.spin.system.SPINModuleRegistry;


/**
 * Can be used to define custom function factories such as spinx.
 * 
 * @author Holger Knublauch
 */
public interface SPINFunctionDriver {

	/**
	 * If this factory is responsible for the provided function Resource
	 * then it must create a FunctionFactory which can then be registered.
	 * @param function  the SPIN Function's resource
	 * @param registry TODO
	 * @return the FunctionFactory or null if this is not responsible
	 */
	SPINFunctionFactory create(Function function, SPINModuleRegistry registry);
}
