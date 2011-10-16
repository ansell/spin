/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple;
import com.hp.hpl.jena.sparql.syntax.TemplateVisitor;


/**
 * Base implementation of TemplateVisitor that calls visit for each triple
 * in a group.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractTemplateVisitor implements TemplateVisitor {

	public void visit(TemplateTriple triple) {
	}

	
	public void visit(TemplateGroup group) {
		Iterator<Template> templates = group.templates();
		while(templates.hasNext()) {
			Template t = templates.next();
			t.visit(this);
		}
	}
}
