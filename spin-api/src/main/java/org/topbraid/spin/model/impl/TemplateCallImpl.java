/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(TemplateCallImpl.class);

    public TemplateCallImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


    @Override
	public Map<Argument,RDFNode> getArgumentsMap(SPINModuleRegistry registry) {
		Map<Argument,RDFNode> map = new HashMap<Argument,RDFNode>();
		Template template = getTemplate(registry);
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


    @Override
	public Map<Property, RDFNode> getArgumentsMapByProperties(SPINModuleRegistry registry) {
		Map<Property,RDFNode> map = new HashMap<Property,RDFNode>();
		Template template = getTemplate(registry);
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


    @Override
	public Map<String, RDFNode> getArgumentsMapByVarNames(SPINModuleRegistry registry) {
		Map<String,RDFNode> map = new HashMap<String,RDFNode>();
		Template template = getTemplate(registry);
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
        log.warn("using singleton based getModule method");
		return getModule(SPINModuleRegistry.get());
	}

    @Override
    public Module getModule(SPINModuleRegistry registry) {
        return getTemplate(registry);
    }

    @Override
	public String getQueryString() {
        log.warn("using singleton based getQueryString method");
        return getQueryString(SPINModuleRegistry.get());
    }
    
    @Override
    public String getQueryString(SPINModuleRegistry registry) {
		Map<String,RDFNode> map = getArgumentsMapByVarNames(registry);
		StringPrintContext p = new StringPrintContext(new StringBuilder(), map);
		Template template = getTemplate(registry);
		p.setUsePrefixes(false);
		template.getBody().print(p, registry);
		return p.getString();
	}

	@Override
	public Template getTemplate() {
        log.warn("using singleton based getTemplate method");
        return getTemplate(SPINModuleRegistry.get());
	}
	
    @Override
    public Template getTemplate(SPINModuleRegistry registry) {
		Statement s = getProperty(RDF.type); //SPIN.template);
		if(s != null && s.getObject().isURIResource()) {
			return registry.getTemplate(s.getResource().getURI(), getModel());
		}
		else {
			return null;
		}
	}


    @Override
	public void print(PrintContext p, SPINModuleRegistry registry) {
		// TODO Auto-generated method stub
		
	}
}
