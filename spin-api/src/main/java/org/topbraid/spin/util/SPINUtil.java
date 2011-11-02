/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.internal.ContainsVarChecker;
import org.topbraid.spin.model.CommandWithWhere;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.QueryOrTemplateCall;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.Printable;
import org.topbraid.spin.model.print.StringPrintContext;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * Some static util methods for SPIN that don't fit anywhere else.
 * 
 * @author Holger Knublauch
 */
public class SPINUtil {
	
    private static final Logger log = LoggerFactory.getLogger(SPINUtil.class);

    /**
	 * The name of the variable that will be used in type binding
	 * triple patterns (?this rdf:type ?TYPE_CLASS)
	 */
	public final static String TYPE_CLASS_VAR_NAME = "TYPE_CLASS";


	/**
	 * Collects all queries or template calls at a given class.
	 * @param cls  the class to get the queries at
	 * @param predicate  the predicate such as <code>spin:rule</code>
	 * @param results  the List to add the results to
	 */
	public static void addQueryOrTemplateCalls(Resource cls, Property predicate, List<QueryOrTemplateCall> results) {
        log.warn("using singleton based addQueryOrTemplateCalls method");
	    addQueryOrTemplateCalls(cls, predicate, results, SPINModuleRegistry.get());
	}
	    
    public static void addQueryOrTemplateCalls(Resource cls, Property predicate, List<QueryOrTemplateCall> results, SPINModuleRegistry registry) {
	    List<Statement> ss = JenaUtil.getStatementsList(JenaUtil.listAllProperties(cls, predicate));
		
		// Special case: we might have an instance of a template call like spl:Attribute
		//               Then try to find the Template in the registry
		if(ss.isEmpty() && cls != null && cls.isURIResource()) {
			Template template = registry.getTemplate(cls.getURI(), null);
			if(template != null) {
				ss = JenaUtil.getStatementsList(JenaUtil.listAllProperties(template, predicate));
			}
		}
		
		for(Statement s : ss) {
			if(s.getObject().isResource()) {
				TemplateCall templateCall = SPINFactory.asTemplateCall(s.getResource(), registry);
				if(templateCall != null) {
					results.add(new QueryOrTemplateCall(cls, templateCall));
				}
				else {
					Query query = SPINFactory.asQuery(s.getResource());
					if(query != null) {
						results.add(new QueryOrTemplateCall(cls, query));
					}
				}
			}
		}
	}
	
	
	/**
	 * Inserts a statement  ?this a ?TYPE_CLASS .  after the WHERE { keyword. 
	 * @param str  the input String
	 */
	public static String addThisTypeClause(String str) {
		String varName = TYPE_CLASS_VAR_NAME;
		int index = str.indexOf("WHERE {") + 7;
		StringBuilder sb = new StringBuilder(str);
		sb.insert(index, " ?this a ?" + varName + " . ");
		return sb.toString();
	}
	

	/**
	 * Applies variable bindings, replacing the values of one map with
	 * the values from a given variables map.
	 * @param map  the Map to modify
	 * @param bindings  the current variable bindings
	 */
	public static void applyBindings(Map<Property,RDFNode> map, Map<String,RDFNode> bindings) {
		for(Property property : new ArrayList<Property>(map.keySet())) {
			RDFNode value = map.get(property);
			Variable var = SPINFactory.asVariable(value);
			if(var != null) {
				String varName = var.getName();
				RDFNode b = bindings.get(varName);
				if(b != null) {
					map.put(property, b);
				}
			}
		}
	}
	

	/**
	 * Binds the variable ?this with a given value.
	 * @param qexec  the QueryExecution to modify
	 * @param value  the value to bind ?this with
	 */
	public static void bindThis(QueryExecution qexec, RDFNode value) {
		if(value != null) {
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add(SPIN.THIS_VAR_NAME, value);
			qexec.setInitialBinding(bindings);
		}
	}
	

	/**
	 * Checks whether a given query mentions the variable ?this anywhere.
	 * This can be used to check whether ?this needs to be bound before
	 * execution, etc.
	 * @param command  the query to test
	 * @return true  if query mentions ?this
	 */
	public static boolean containsThis(CommandWithWhere command) {
        log.warn("using singleton based containsThis method");
	    return containsThis(command, SPINModuleRegistry.get());
	}
	
    public static boolean containsThis(CommandWithWhere command, SPINModuleRegistry registry) {
	    return new ContainsVarChecker().contains(command, SPIN._this, registry);
	}
	
	
	/**
	 * Attempts to convert a given RDFNode to a String so that it can be parsed into
	 * a Jena query object.  The node must be either a string Literal, or a sp:Query node
	 * or a template call.  If it's a template call then the resulting query string will
	 * have additional LET statements to "hard-bind" the template variables. 
	 * @param node  the RDFNode to convert
	 * @param usePrefixes  true to use qname abbreviations
	 * @return the String representation of node
	 * @throws IllegalArgumentException  if the node is not a valid SPIN Query or a String
	 */
	public static String getQueryString(RDFNode node, boolean usePrefixes) {
        log.warn("using singleton based getQueryString method");
	    return getQueryString(node, usePrefixes, SPINModuleRegistry.get());
	}
	
    public static String getQueryString(RDFNode node, boolean usePrefixes, SPINModuleRegistry registry) {
	    if(node.isLiteral()) {
			return ((Literal)node).getLexicalForm();
		}
		else {
			Resource resource = (Resource)node;
			org.topbraid.spin.model.Command spinCommand = SPINFactory.asCommand(resource);
			if(spinCommand != null) {
				if(usePrefixes) {
					StringPrintContext p = new StringPrintContext();
					p.setUsePrefixes(usePrefixes);
					spinCommand.print(p, registry);
					return p.getString();
				}
				else {
					return ARQFactory.get().createCommandString(spinCommand, registry);
				}
			}
			else {
				TemplateCall templateCall = SPINFactory.asTemplateCall(resource, registry);
				if(templateCall != null) {
					return templateCall.getQueryString(registry);
				}
				else {
					throw new IllegalArgumentException("Node must be either literal or a SPIN query or a SPIN template call");
				}
			}
		}
	}
	
	
	/**
	 * Gets a Collection of all query strings defined as values of a given property.
	 * This will accept strings or SPIN expressions (including template calls).
	 * The query model is the subject's getModel().
	 * All sub-properties of property from the query model will also be queried.
	 * @param subject  the subject to get the values of
	 * @param property  the property to query
	 * @return a Set of query strings
	 */
	public static Collection<String> getQueryStrings(Resource subject, Property property) {
        log.warn("using singleton based getQueryStrings method");
		Map<Statement,String> map = getQueryStringMap(subject, property, SPINModuleRegistry.get());
		return map.values();
	}
	
	
	/**
	 * Gets a Map of all query strings defined as values of a given property.
	 * This will accept strings or SPIN expressions (including template calls).
	 * The query model is the subject's getModel().
	 * All sub-properties of property from the query model will also be queried.
	 * The resulting Map will associate each query String with the Statement
	 * that has created it.
	 * @param subject  the subject to get the values of
	 * @param property  the property to query
	 * @param registry TODO
	 * @return a Map of Statements to query strings
	 */
	public static Map<Statement,String> getQueryStringMap(Resource subject, Property property, SPINModuleRegistry registry) {
		if(subject != null) {
			property = subject.getModel().getProperty(property.getURI());
		}
		Map<Statement,String> queryStrings = new HashMap<Statement,String>();
		Set<Resource> ps = JenaUtil.getAllSubProperties(property);
		ps.add(property);
		for(Resource p : ps) {
			StmtIterator it = property.getModel().listStatements(subject, JenaUtil.asProperty(p), (RDFNode)null);
			while(it.hasNext()) {
				Statement s = it.nextStatement();
				RDFNode object = s.getObject();
				String str = getQueryString(object, false, registry);
				queryStrings.put(s, str);
			}
		}
		return queryStrings;
	}
	
	
	public static Set<Resource> getURIResources(Printable query, SPINModuleRegistry registry) {
		final Set<Resource> results = new HashSet<Resource>();
		StringPrintContext context = new StringPrintContext() {

			@Override
			public PrintContext clone() {
				return this;
			}

			@Override
			public void printURIResource(Resource resource) {
				super.printURIResource(resource);
				results.add(resource);
			}
		};
		query.print(context, registry);
		return results;
	}


	/**
	 * Checks whether a given Graph is a spin:LibraryOntology.
	 * This is true for the SP and SPIN namespaces, as well as any Graph that
	 * has [baseURI] rdf:type spin:LibraryOntology.
	 * @param graph  the Graph to test
	 * @param baseURI  the base URI of the Graph (to find the library ontology)
	 * @return true if graph is a library ontology
	 */
	public static boolean isLibraryOntology(Graph graph, URI baseURI) {
		if(baseURI != null) {
			if(SP.BASE_URI.equals(baseURI.toString()) || SPIN.BASE_URI.equals(baseURI.toString())) {
				return true;
			}
			else {
				Node ontology = Node.createURI(baseURI.toString());
				return graph.contains(ontology, RDF.type.asNode(), SPIN.LibraryOntology.asNode());
			}
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Converts a map from Properties to RDFNode values to a Map from variable
	 * names (Strings) to those values, for quicker look up.
	 * @param map  the old Map
	 * @return the new Map
	 */
	public static Map<String,RDFNode> mapProperty2VarNames(Map<Property,RDFNode> map) {
		Map<String,RDFNode> results = new HashMap<String,RDFNode>();
		for(Property predicate : map.keySet()) {
			RDFNode value = map.get(predicate);
			results.put(predicate.getLocalName(), value);
		}
		return results;
	}
}
