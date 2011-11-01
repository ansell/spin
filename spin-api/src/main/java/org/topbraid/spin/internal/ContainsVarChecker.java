/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.internal;

import org.topbraid.spin.model.CommandWithWhere;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.Triple;
import org.topbraid.spin.model.TriplePath;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.TripleTemplate;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.update.Modify;
import org.topbraid.spin.model.visitor.AbstractElementVisitor;
import org.topbraid.spin.model.visitor.AbstractExpressionVisitor;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.model.visitor.ElementWalker;
import org.topbraid.spin.model.visitor.ExpressionVisitor;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * Can be used to check whether a given SPIN Query contains a certain
 * variable, either in the WHERE clause or the CONSTRUCT template(s).
 * 
 * @author Holger Knublauch
 */
public class ContainsVarChecker {
	
	private boolean result;
	
	private Resource var;
	

	public boolean contains(CommandWithWhere command, Resource var_, SPINModuleRegistry registry) {
		
		this.var = var_;

		if(command instanceof Construct) {
			// Check head of Construct
			for(TripleTemplate template : ((Construct)command).getTemplates()) {
				if(containsVar(template)) {
					return true;
				}
			}
		}
		else if(command instanceof Modify) {
			Modify modify = (Modify) command;
			if(templateContainsVar(modify.getProperty(SP.insertPattern))) {
				return true;
			}
			if(templateContainsVar(modify.getProperty(SP.deletePattern))) {
				return true;
			}
		}
		
		// Check WHERE clause
		ElementVisitor el = new AbstractElementVisitor() {

			@Override
			public void visit(TriplePath triplePath, SPINModuleRegistry registry) {
				if(var.equals(triplePath.getObject()) ||
					var.equals(triplePath.getSubject())) {
					result = true;
				}
			}

			@Override
			public void visit(TriplePattern triplePattern, SPINModuleRegistry registry) {
				if(containsVar(triplePattern)) {
					result = true;
				}
			}
		};
		ExpressionVisitor ex = new AbstractExpressionVisitor() {

			@Override
			public void visit(Variable variable, SPINModuleRegistry registry) {
				if(var.equals(variable)) {
					result = true;
				}
			}
		};
		ElementWalker walker = new ElementWalker(el, ex);
		ElementList where = command.getWhere();
		if(where != null) {
			walker.visit(where, registry);
		}
		
		return result;
	}


	private boolean containsVar(Triple triple) {
		return var.equals(triple.getObject()) ||
			var.equals(triple.getPredicate()) ||
			var.equals(triple.getSubject());
	}
	
	
	private boolean templateContainsVar(Statement listS) {
		if(listS != null && listS.getObject().isResource()) {
			ExtendedIterator<RDFNode> nodes = (listS.getObject().as(RDFList.class)).iterator();
			while(nodes.hasNext()) {
				Resource node = (Resource) nodes.next();
				if(node.hasProperty(RDF.type, SP.NamedGraph)) {
					NamedGraph namedGraph = node.as(NamedGraph.class);
					for(Element element : namedGraph.getElements()) {
						if(element instanceof Triple) {
							if(containsVar((Triple)element)) {
								nodes.close();
								return true;
							}
						}
					}
				}
				else {
					if(containsVar(node.as(TripleTemplate.class))) {
						nodes.close();
						return true;
					}
				}
			}
		}
		return false;
	}
}
