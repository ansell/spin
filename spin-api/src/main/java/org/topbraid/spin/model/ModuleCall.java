/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Abstract base interface of TemplateCall and FunctionCall.
 * 
 * @author Holger Knublauch
 */
public interface ModuleCall extends Resource {
	
	Module getModule();

    Module getModule(Set<Object> validFunctionSources);

}
