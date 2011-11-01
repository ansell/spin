/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.arq.Aggregations;
import org.topbraid.spin.model.Aggregation;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.Printable;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;


public class AggregationImpl extends AbstractSPINResourceImpl implements Aggregation {

	public AggregationImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	public Variable getAs() {
		Resource as = getResource(SP.as);
		if(as != null) {
			return SPINFactory.asVariable(as);
		}
		else {
			return null;
		}
	}

	
	public Resource getExpression() {
		return getResource(SP.expression);
	}

	
	public boolean isDistinct() {
		return hasProperty(SP.distinct, getModel().createTypedLiteral(true));
	}

	
	public void print(PrintContext p, SPINModuleRegistry registry) {
		
		Variable asVar = getAs(); 
		if(asVar != null) {
			p.print("(");
		}

		Resource aggType = getResource(RDF.type);
		String aggName = Aggregations.getName(aggType);
		p.printKeyword(aggName);
		p.print("(");
		
		if(isDistinct()) {
			p.print("DISTINCT ");
		}
		
		Statement exprS = getProperty(SP.expression);
		if(exprS != null && exprS.getObject().isResource()) {
			Resource r = exprS.getResource();
			RDFNode expr = SPINFactory.asExpression(r);
			if(expr instanceof Printable) {
				((Printable)expr).print(p, registry);
			}
			else {
				p.printURIResource(r);
			}
		}
		else {
			p.print("*");
		}
		if(asVar != null) {
			p.print(") ");
			p.printKeyword("AS");
			p.print(" ");
			p.print(asVar.toString(registry));
		}
		p.print(")");
	}
}
