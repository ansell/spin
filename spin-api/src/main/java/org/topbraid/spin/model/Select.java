/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A SELECT query.
 * 
 * @author Holger Knublauch
 */
public interface Select extends SolutionModifierQuery {

	/**
	 * Gets a list of result variables, or null if we have a star
	 * results list.  Note that the "variables" may in fact be
	 * wrapped aggregations or expressions.
	 * The results can be tested with instanceof against
	 * <code>Variable</code>, <code>Aggregation</code> or
	 * <code>FunctionCall</code>.  Variables can have an additional
	 * <code>sp:expression</code>, representing AS expressions.
	 * @return the result "variables"
	 */
	List<Resource> getResultVariables();
	
	
	/**
	 * Checks is this query has the DISTINCT flag set.
	 * @return true if distinct
	 */
	boolean isDistinct();
	

	/**
	 * Checks if this query has the REDUCED flag set.
	 * @return true if reduced
	 */
	boolean isReduced();
}
