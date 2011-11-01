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
 * An interface to visit the various kinds of Elements.
 * 
 * @author Holger Knublauch
 */
public interface ElementVisitor {
	
	void visit(ElementList elementList, SPINModuleRegistry registry);
	
	
	void visit(Filter filter, SPINModuleRegistry registry);
	
	
	void visit(Bind bind, SPINModuleRegistry registry);
	
	
	void visit(Minus minus, SPINModuleRegistry registry);
	
	
	void visit(NamedGraph namedGraph, SPINModuleRegistry registry);
	
	
	void visit(NotExists notExists, SPINModuleRegistry registry);
	
	
	void visit(Optional optional, SPINModuleRegistry registry);

	
	void visit(Service service, SPINModuleRegistry registry);
	
	
	void visit(SubQuery subQuery, SPINModuleRegistry registry);
	
	
	void visit(TriplePath triplePath, SPINModuleRegistry registry);

	
	void visit(TriplePattern triplePattern, SPINModuleRegistry registry);
	
	
	void visit(Union union, SPINModuleRegistry registry);
}
