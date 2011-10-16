/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Module;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.StringPrintContext;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;


public class TemplateCallImpl extends ModuleCallImpl implements TemplateCall {

	public TemplateCallImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	public Map<Argument,RDFNode> getArgumentsMap() {
		Map<Argument,RDFNode> map = new HashMap<Argument,RDFNode>();
		Template template = getTemplate();
		if(template != null) {
			for(Argument ad : template.getArguments(false)) {
				Property argProperty = ad.getPredicate();
				if(argProperty != null) {
					Statement valueS = getProperty(argProperty);
					if(valueS != null) {
						map.put(ad, valueS.getObject());
					}
				}
			}
		}
		
		return map;
	}


	public Map<Property, RDFNode> getArgumentsMapByProperties() {
		Map<Property,RDFNode> map = new HashMap<Property,RDFNode>();
		Template template = getTemplate();
		if(template != null) {
			for(Argument ad : template.getArguments(false)) {
				Property argProperty = ad.getPredicate();
				if(argProperty != null) {
					Statement valueS = getProperty(argProperty);
					if(valueS != null) {
						map.put(argProperty, valueS.getObject());
					}
				}
			}
		}
		
		return map;
	}


	public Map<String, RDFNode> getArgumentsMapByVarNames() {
		Map<String,RDFNode> map = new HashMap<String,RDFNode>();
		Template template = getTemplate();
		if(template != null) {
			for(Argument ad : template.getArguments(false)) {
				Property argProperty = ad.getPredicate();
				if(argProperty != null) {
					String varName = ad.getVarName();
					Statement valueS = getProperty(argProperty);
					if(valueS != null) {
						map.put(varName, valueS.getObject());
					}
				}
			}
		}
		return map;
	}

	
	@Override
	public Module getModule() {
		return getTemplate();
	}


	public String getQueryString() {
		Map<String,RDFNode> map = getArgumentsMapByVarNames();
		StringPrintContext p = new StringPrintContext(new StringBuilder(), map);
		Template template = getTemplate();
		p.setUsePrefixes(false);
		template.getBody().print(p);
		return p.getString();
	}


	public Template getTemplate() {
		Statement s = getProperty(RDF.type); //SPIN.template);
		if(s != null && s.getObject().isURIResource()) {
			return SPINModuleRegistry.get().getTemplate(s.getResource().getURI(), getModel());
		}
		else {
			return null;
		}
	}


	public void print(PrintContext p) {
		// TODO Auto-generated method stub
		
	}
}
