/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.Map;

import org.topbraid.spin.system.SPINModuleRegistry;

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
	 * @param registry TODO
	 * @return a Map from ArgumentDescriptors to RDFNodes
	 */
	Map<Argument,RDFNode> getArgumentsMap(SPINModuleRegistry registry);

	
	/**
	 * Gets a Map from Properties to RDFNodes derived from the
	 * ArgumentDescriptors.
	 * @param registry TODO
	 * @return a Map from Properties to RDFNodes
	 */
	Map<Property,RDFNode> getArgumentsMapByProperties(SPINModuleRegistry registry);

	
	/**
	 * Gets a Map from variable names to RDFNodes derived from the
	 * ArgumentDescriptors.
	 * @param registry TODO
	 * @return a Map from variable names to RDFNodes
	 */
	Map<String,RDFNode> getArgumentsMapByVarNames(SPINModuleRegistry registry);
	
	
	/**
	 * Gets this template call as a parsable SPARQL string, with all
	 * pre-bound argument variables inserted as constants.
	 * @return a SPARQL query string
	 */
	String getQueryString();
	
	/**
     * Gets this template call as a parsable SPARQL string, with all
     * pre-bound argument variables inserted as constants.
     * @param registry The registry to use to resolve definitions for functions
     * @return a SPARQL query string
	 */
    String getQueryString(SPINModuleRegistry registry);
	
	/**
	 * Gets the associated Template, from the singleton SPINModules registry.
	 * @return the template
	 */
	Template getTemplate();


    /**
     * Gets the associated Template, from the given SPINModules registry.
     * @param registry The registry to search for the template on
     * @return the template
     */
    Template getTemplate(SPINModuleRegistry registry);

}
