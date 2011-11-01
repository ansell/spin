/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TripleTemplate;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.RDFNode;


public class TripleTemplateImpl extends TripleImpl implements TripleTemplate {

	public TripleTemplateImpl(Node node, EnhGraph eh) {
		super(node, eh);
	}

	
	@Override
	protected void print(RDFNode node, PrintContext p, SPINModuleRegistry registry) {
		if(node.isAnon() && SPINFactory.asVariable(node) == null) {
			String str = p.getNodeToLabelMap().asString(node.asNode());
			p.print(str);
		}
		else {
			super.print(node, p, registry);
		}
	}
}
