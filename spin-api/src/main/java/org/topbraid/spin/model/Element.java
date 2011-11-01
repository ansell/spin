/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import org.topbraid.spin.model.print.Printable;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * The abstract base interface for the various Element types.
 * 
 * @author Holger Knublauch
 */
public interface Element extends Printable, Resource {
	
	/**
	 * Visits this with a given visitor.
	 * @param visitor  the visitor to visit this with
	 * @param registry The registry to use to reconcile elements with their descriptions
	 */
	void visit(ElementVisitor visitor, SPINModuleRegistry registry);
}
