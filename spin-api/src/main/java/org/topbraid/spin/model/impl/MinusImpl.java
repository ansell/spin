/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Minus;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;

public class MinusImpl extends ElementImpl implements Minus {
	
	public MinusImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	public void visit(ElementVisitor visitor, SPINModuleRegistry registry) {
		visitor.visit(this, registry);
	}

	
	public void print(PrintContext p, SPINModuleRegistry registry) {
		p.printKeyword("MINUS");
		printNestedElementList(p, registry);
	}
}
