/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import org.topbraid.spin.model.Aggregation;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * A visitor to visit the various types of expression elements.
 * 
 * @author Holger Knublauch
 */
public interface ExpressionVisitor {
	
	void visit(Aggregation aggregation, SPINModuleRegistry registry);
	
	
	void visit(FunctionCall functionCall, SPINModuleRegistry registry);

	
	void visit(RDFNode node, SPINModuleRegistry registry);
	
	
	void visit(Variable variable, SPINModuleRegistry registry);
}
