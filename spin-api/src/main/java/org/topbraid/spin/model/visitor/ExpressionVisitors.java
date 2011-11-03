/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import java.util.HashSet;
import java.util.Set;

import org.topbraid.spin.model.Aggregation;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.Variable;

import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * Utility functions for ExpressionVisitors.
 * 
 * @author Holger Knublauch
 */
public class ExpressionVisitors {

	public static void visit(RDFNode node, ExpressionVisitor visitor) {
	    visit(node, visitor, new HashSet<Object>());
	}
	
    public static void visit(RDFNode node, ExpressionVisitor visitor, Set<Object> validFunctionSources) {
	    if(node instanceof Variable) {
			visitor.visit((Variable)node, validFunctionSources);
		}
		else if(node instanceof FunctionCall) {
			visitor.visit((FunctionCall)node, validFunctionSources);
		}
		else if(node instanceof Aggregation) {
			visitor.visit((Aggregation)node, validFunctionSources);
		}
		else if(node != null) {
			visitor.visit(node, validFunctionSources);
		}
	}
}
