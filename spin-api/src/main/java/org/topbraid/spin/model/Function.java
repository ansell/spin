/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A SPIN Function module (not: FunctionCall).
 * 
 * @author Holger Knublauch
 */
public interface Function extends Module {
	
	/**
	 * Gets the value of the spin:returnType property, if any.
	 * @return the return type or null
	 */
	Resource getReturnType();
	
	
	/**
	 * Indicates if spin:private is set to true for this function.
	 * @return true  if marked private
	 */
	boolean isPrivate();
}
