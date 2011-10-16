/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * A template call.
 * 
 * @author Holger Knublauch
 */
public interface TemplateCall extends ModuleCall {
	
	/**
	 * Gets a Map from ArgumentDescriptors to RDFNodes.
	 * @return a Map from ArgumentDescriptors to RDFNodes
	 */
	Map<Argument,RDFNode> getArgumentsMap();

	
	/**
	 * Gets a Map from Properties to RDFNodes derived from the
	 * ArgumentDescriptors.
	 * @return a Map from Properties to RDFNodes
	 */
	Map<Property,RDFNode> getArgumentsMapByProperties();

	
	/**
	 * Gets a Map from variable names to RDFNodes derived from the
	 * ArgumentDescriptors.
	 * @return a Map from variable names to RDFNodes
	 */
	Map<String,RDFNode> getArgumentsMapByVarNames();
	
	
	/**
	 * Gets this template call as a parsable SPARQL string, with all
	 * pre-bound argument variables inserted as constants.
	 * @return a SPARQL query string
	 */
	String getQueryString();
	
	
	/**
	 * Gets the associated Template, from the SPINModules registry.
	 * @return the template
	 */
	Template getTemplate();
}
