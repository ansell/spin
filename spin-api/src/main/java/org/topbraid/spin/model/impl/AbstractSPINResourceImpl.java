/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.Printable;
import org.topbraid.spin.model.print.StringPrintContext;
import org.topbraid.spin.system.ExtraPrefixes;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.SPINExpressions;
import org.topbraid.spin.util.SPINUtil;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public abstract class AbstractSPINResourceImpl extends org.topbraid.spin.model.SPINResourceImpl implements Printable {
	
	/**
	 * One level of indentation (four spaces), used by toString methods
	 */
	public static final String INDENTATION = "    ";
    private static final Logger log = LoggerFactory.getLogger(AbstractSPINResourceImpl.class);

	
	public AbstractSPINResourceImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	public String getComment() {
		return getString(RDFS.comment);
	}

	
	public List<Element> getElements() {
		return getElements(SP.elements);
	}
	

	public List<Element> getElements(Property predicate) {
		List<Element> results = new LinkedList<Element>();
		for(RDFNode node : getList(predicate)) {
			if(node != null && node.isResource()) {
				results.add(SPINFactory.asElement((Resource)node));
			}
		}
		return results;
	}

	
	public List<RDFNode> getList(Property predicate) {
		Resource rawList = getResource(predicate);
		List<RDFNode> results = new LinkedList<RDFNode>();
		if(rawList != null) {
			RDFList list = rawList.as(RDFList.class);
			for(ExtendedIterator<RDFNode> it = list.iterator(); it.hasNext(); ) {
				RDFNode node = it.next();
				results.add(node);
			}
		}
		return results;
	}
	
	
	private String getPrefix(String namespace, PrintContext context) {
		String prefix = getModel().getNsURIPrefix(namespace);
		if(prefix == null && context.getUseExtraPrefixes()) {
			Map<String,String> extras = ExtraPrefixes.getExtraPrefixes();
			for(String extraPrefix : extras.keySet()) {
				String ns = extras.get(extraPrefix);
				if(namespace.equals(ns)) {
					return extraPrefix;
				}
			}
		}
		return prefix;
	}

	
	public static boolean hasRDFType(Node node, EnhGraph graph, Resource type) {
		return graph.asGraph().contains(node, RDF.type.asNode(), type.asNode());
	}
	
	
	protected void printComment(PrintContext context) {
		String str = getComment();
		if(str != null) {
			String[] rows = str.split("\n");
			for(int i = 0; i < rows.length; i++) {
				context.print("# ");
				context.print(rows[i]);
				context.println();
			}
		}
	}


	protected void printNestedElementList(PrintContext p, SPINModuleRegistry registry) {
		printNestedElementList(p, SP.elements, registry);
	}
	

	protected void printNestedElementList(PrintContext p, Property predicate, SPINModuleRegistry registry) {
		p.print(" {");
		p.println();
		Resource elementsRaw = getResource(predicate);
		if(elementsRaw != null) {
			ElementList elements = elementsRaw.as(ElementList.class);
			p.setIndentation(p.getIndentation() + 1);
			elements.print(p, registry);
			p.setIndentation(p.getIndentation() - 1);
		}
		p.printIndentation(p.getIndentation());
		p.print("}");
	}

	
	protected void printNestedExpressionString(PrintContext context, RDFNode node, SPINModuleRegistry registry) {
		printNestedExpressionString(context, node, false, registry);
	}
	
	
	protected void printNestedExpressionString(PrintContext p, RDFNode node, boolean force, SPINModuleRegistry registry) {
		SPINExpressions.printExpressionString(p, node, true, force, getModel().getGraph().getPrefixMapping(), registry);
	}
	
	
	protected void printPrefixes(PrintContext context, SPINModuleRegistry registry) {
		if(context.getPrintPrefixes()) {
			Set<Resource> uriResources = SPINUtil.getURIResources(this, registry);
			Set<String> namespaces = new HashSet<String>();
			for(Resource uriResource : uriResources) {
				String namespace = uriResource.getNameSpace();
				namespaces.add(namespace);
			}
			Map<String,String> prefix2Namespace = new HashMap<String,String>();
			for(String namespace : namespaces) {
				String prefix = getPrefix(namespace, context);
				if(prefix != null) {
					prefix2Namespace.put(prefix, namespace);
				}
			}
			List<String> prefixes = new ArrayList<String>(prefix2Namespace.keySet());
			Collections.sort(prefixes);
			for(String prefix : prefixes) {
				context.printKeyword("PREFIX");
				context.print(" ");
				context.print(prefix);
				context.print(": <");
				String ns = prefix2Namespace.get(prefix);
				context.print(ns);
				context.print(">");
				context.println();
			}
		}
	}

	/**
	 * Prints the resource using the global registry as a reference for functions and modules
	 */
	@Override
	public String toString() {
	    log.warn("using singleton based toString method");
	    return toString(SPINModuleRegistry.get());
	}
	
	@Override
    public String toString(SPINModuleRegistry registry) {
	    
		StringPrintContext p = new StringPrintContext();
		print(p, registry);
		return p.getString();
	}


	public static void printVarOrResource(PrintContext p, Resource resource, SPINModuleRegistry registry) {
		Variable variable = SPINFactory.asVariable(resource);
		if(variable != null) {
			variable.print(p, registry);
		}
		else if(resource.isURIResource()) {
			p.printURIResource(resource);
		}
		else {
			p.print("[]");
		}
	}
}
