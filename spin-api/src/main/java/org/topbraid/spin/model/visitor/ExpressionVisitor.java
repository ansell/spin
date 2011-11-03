/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import java.util.Set;

import org.topbraid.spin.model.Aggregation;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.Variable;

import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * A visitor to visit the various types of expression elements.
 * 
 * @author Holger Knublauch
 */
public interface ExpressionVisitor {
	
	void visit(Aggregation aggregation, Set<Object> validFunctionSources);
	
	
	void visit(FunctionCall functionCall, Set<Object> validFunctionSources);

	
	void visit(RDFNode node, Set<Object> validFunctionSources);
	
	
	void visit(Variable variable, Set<Object> validFunctionSources);
}
