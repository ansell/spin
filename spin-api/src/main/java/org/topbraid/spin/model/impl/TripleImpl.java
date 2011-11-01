/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Triple;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;


public abstract class TripleImpl extends TupleImpl implements Triple {

	public TripleImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	public Resource getPredicate() {
		return (Resource) getRDFNodeOrVariable(SP.predicate);
	}
	
	
	public void print(PrintContext p, SPINModuleRegistry registry) {
		print(getSubject(), p, registry);
		p.print(" ");
		print(getPredicate(), p, true, registry);
		p.print(" ");
		print(getObject(), p, registry);
	}
}
