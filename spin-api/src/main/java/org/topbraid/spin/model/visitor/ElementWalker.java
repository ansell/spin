/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import java.util.List;

import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementGroup;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.Filter;
import org.topbraid.spin.model.Bind;
import org.topbraid.spin.model.Minus;
import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.NotExists;
import org.topbraid.spin.model.Optional;
import org.topbraid.spin.model.Service;
import org.topbraid.spin.model.SubQuery;
import org.topbraid.spin.model.TriplePath;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.Union;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * An object that can be used to recursively walk through an Element
 * and the embedded expressions.
 * 
 * @author Holger Knublauch
 */
public class ElementWalker implements ElementVisitor {
	
	private ElementVisitor elementVisitor;
	
	private ExpressionVisitor expressionVisitor;
	
	
	public ElementWalker(ElementVisitor elementVisitor, ExpressionVisitor expressionVisitor) {
		this.elementVisitor = elementVisitor;
		this.expressionVisitor = expressionVisitor;
	}


	public void visit(Bind bind, SPINModuleRegistry registry) {
		elementVisitor.visit(bind, registry);
		visitExpression(bind.getExpression(), registry);
	}

	
	public void visit(ElementList elementList, SPINModuleRegistry registry) {
		elementVisitor.visit(elementList, registry);
		visitChildren(elementList, registry);
	}


	public void visit(Filter filter, SPINModuleRegistry registry) {
		elementVisitor.visit(filter, registry);
		visitExpression(filter.getExpression(), registry);
	}


	@Override
	public void visit(Minus minus, SPINModuleRegistry registry) {
		elementVisitor.visit(minus, registry);
		visitChildren(minus, registry);
	}


	public void visit(NamedGraph namedGraph, SPINModuleRegistry registry) {
		elementVisitor.visit(namedGraph, registry);
		visitChildren(namedGraph, registry);
	}


	public void visit(NotExists notExists, SPINModuleRegistry registry) {
		elementVisitor.visit(notExists, registry);
		visitChildren(notExists, registry);
	}


	public void visit(Optional optional, SPINModuleRegistry registry) {
		elementVisitor.visit(optional, registry);
		visitChildren(optional, registry);
	}


	public void visit(Service service, SPINModuleRegistry registry) {
		elementVisitor.visit(service, registry);
		visitChildren(service, registry);
	}


	public void visit(SubQuery subQuery, SPINModuleRegistry registry) {
		elementVisitor.visit(subQuery, registry);
	}


	public void visit(TriplePath triplePath, SPINModuleRegistry registry) {
		elementVisitor.visit(triplePath, registry);
	}


	public void visit(TriplePattern triplePattern, SPINModuleRegistry registry) {
		elementVisitor.visit(triplePattern, registry);
	}


	public void visit(Union union, SPINModuleRegistry registry) {
		elementVisitor.visit(union, registry);
		visitChildren(union, registry);
	}
	
	
	private void visitChildren(ElementGroup group, SPINModuleRegistry registry) {
		List<Element> childElements = group.getElements();
		for(Element childElement : childElements) {
			childElement.visit(this, registry);
		}
	}
	
	
	private void visitExpression(RDFNode node, SPINModuleRegistry registry) {
		if(expressionVisitor != null) {
			ExpressionWalker expressionWalker = new ExpressionWalker(expressionVisitor);
			ExpressionVisitors.visit(node, expressionWalker, registry);
		}
	}
}
