/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Command;
import org.topbraid.spin.model.Module;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.ModulesUtil;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;


public class ModuleImpl extends AbstractSPINResourceImpl implements Module {
	
	
	public ModuleImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}

	
	public List<Argument> getArguments(boolean ordered) {
		List<Argument> results = new ArrayList<Argument>();
		Set<Resource> classes = JenaUtil.getAllSuperClasses(this);
		classes.add(this);
		for(Resource cls : classes) {
			StmtIterator it = cls.listProperties(SPIN.constraint);
			while(it.hasNext()) {
				Statement s = it.nextStatement();
				if(s.getObject().isResource() && JenaUtil.hasIndirectType(s.getResource(), (Resource)SPL.Argument.inModel(s.getModel()))) {
					results.add((Argument)s.getResource().as(Argument.class));
				}
			}
		}
		
		if(ordered) {
			Collections.sort(results, new Comparator<Argument>() {
				public int compare(Argument o1, Argument o2) {
					Property p1 = o1.getPredicate();
					Property p2 = o2.getPredicate();
					if(p1 != null && p2 != null) {
						return p1.getLocalName().compareTo(p2.getLocalName());
					}
					else {
						return 0;
					}
				}
			});
		}
		
		return results;
	}


	public Map<String, Argument> getArgumentsMap() {
		Map<String,Argument> results = new HashMap<String,Argument>();
		for(Argument argument : getArguments(false)) {
			Property property = argument.getPredicate();
			if(property != null) {
				results.put(property.getLocalName(), argument);
			}
		}
		return results;
	}


	public Command getBody() {
		RDFNode node = ModulesUtil.getBody(this);
		if(node instanceof Resource) {
			return SPINFactory.asCommand((Resource)node);
		}
		else {
			return null;
		}
	}
	
	
	public String getComment() {
		return getString(RDFS.comment);
	}


	public boolean isAbstract() {
		return SPINFactory.isAbstract(this);
	}


	public void print(PrintContext p) {
		// TODO Auto-generated method stub

	}
}
