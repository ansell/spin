/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import java.util.List;

import org.topbraid.spin.model.Aggregation;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * An ExpressionVisitor that recursively visits all expressions under
 * a given root.
 * 
 * @author Holger Knublauch
 */
public class ExpressionWalker implements ExpressionVisitor {

	private ExpressionVisitor visitor;
	
	
	public ExpressionWalker(ExpressionVisitor visitor) {
		this.visitor = visitor;
	}

	
	public void visit(Aggregation aggregation, SPINModuleRegistry registry) {
		visitor.visit(aggregation, registry);
		Variable as = aggregation.getAs();
		if(as != null) {
			visitor.visit(as, registry);
		}
		Resource expr = aggregation.getExpression();
		if(expr != null) {
			ExpressionVisitors.visit(expr, this, registry);
		}
	}


	public void visit(FunctionCall functionCall, SPINModuleRegistry registry) {
		visitor.visit(functionCall, registry);
		List<RDFNode> args = functionCall.getArguments();
		for(RDFNode arg : args) {
			ExpressionVisitors.visit(arg, this, registry);
		}
	}

	
	public void visit(RDFNode node, SPINModuleRegistry registry) {
		visitor.visit(node, registry);
	}

	
	public void visit(Variable variable, SPINModuleRegistry registry) {
		visitor.visit(variable, registry);
	}
}
