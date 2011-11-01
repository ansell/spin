/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

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


/**
 * Basic, "empty" implementation of ElementVisitor.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractElementVisitor implements ElementVisitor {

	public void visit(ElementList elementList, SPINModuleRegistry registry) {
	}


	public void visit(Filter filter, SPINModuleRegistry registry) {
	}


	public void visit(Bind let, SPINModuleRegistry registry) {
	}


	@Override
	public void visit(Minus minus, SPINModuleRegistry registry) {
	}


	public void visit(NamedGraph namedGraph, SPINModuleRegistry registry) {
	}
	
	
	public void visit(NotExists notExists, SPINModuleRegistry registry) {
	}


	public void visit(Optional optional, SPINModuleRegistry registry) {
	}


	public void visit(Service service, SPINModuleRegistry registry) {
	}


	public void visit(SubQuery subQuery, SPINModuleRegistry registry) {
	}


	public void visit(TriplePath triplePath, SPINModuleRegistry registry) {
	}


	public void visit(TriplePattern triplePattern, SPINModuleRegistry registry) {
	}


	public void visit(Union union, SPINModuleRegistry registry) {
	}
}
