/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;


/**
 * A template class definition.
 * 
 * @author Holger Knublauch
 */
public interface Template extends Module {

	/**
	 * Gets the declared spin:labelTemplate (if any exists).
	 * @return the label template string or null
	 */
	String getLabelTemplate();
}
