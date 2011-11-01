/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;


public class NamedGraphImpl extends ElementImpl implements NamedGraph {
	
	public NamedGraphImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	public Resource getNameNode() {
		Resource r = getResource(SP.graphNameNode);
		if(r != null) {
			Variable variable = SPINFactory.asVariable(r);
			if(variable != null) {
				return variable;
			}
			else {
				return r;
			}
		}
		else {
			return null;
		}
	}


	public void print(PrintContext p, SPINModuleRegistry registry) {
		p.printKeyword("GRAPH");
		p.print(" ");
		printVarOrResource(p, getNameNode(), registry);
		printNestedElementList(p, registry);
	}


	public void visit(ElementVisitor visitor, SPINModuleRegistry registry) {
		visitor.visit(this, registry);
	}
}
