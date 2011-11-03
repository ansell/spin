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
 * An "empty" base implementation of ExpressionVisitor.
 * 
 * @author Holger Knublauch
 */
public class AbstractExpressionVisitor implements ExpressionVisitor {

	public void visit(Aggregation aggregation, Set<Object> validFunctionSources) {
	}

	
	public void visit(FunctionCall functionCall, Set<Object> validFunctionSources) {
	}


	public void visit(RDFNode node, Set<Object> validFunctionSources) {
	}


	public void visit(Variable variable, Set<Object> validFunctionSources) {
	}
}
