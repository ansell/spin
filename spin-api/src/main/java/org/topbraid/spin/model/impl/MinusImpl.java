/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Minus;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;

public class MinusImpl extends ElementImpl implements Minus {
	
	public MinusImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}

	
	public void print(PrintContext p) {
		p.printKeyword("MINUS");
		printNestedElementList(p);
	}
}
