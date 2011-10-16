/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.QueryOrTemplateCall;
import org.topbraid.spin.model.SPINInstance;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * Utilities related to the spl namespace.
 * 
 * @author Holger Knublauch
 */
public class SPLUtil {
	
	private static void addDefaultValuesForType(Resource cls, Map<Property,RDFNode> results, Set<Resource> reached) {
		
		reached.add(cls);
		
		StmtIterator it = cls.listProperties(SPIN.rule);
		while(it.hasNext()) {
			Statement s = it.nextStatement();
			if(s.getObject().isResource()) {
				Resource templateCall = s.getResource();
				if(templateCall.hasProperty(RDF.type, SPL.InferDefaultValue)) {
					Statement predicateS = templateCall.getProperty(SPL.predicate);
					if(predicateS != null && predicateS.getObject().isURIResource()) {
						Property predicate = cls.getModel().getProperty(predicateS.getResource().getURI());
						if(!results.containsKey(predicate)) {
							Statement v = templateCall.getProperty(SPL.defaultValue);
							if(v != null) {
								results.put(predicate, v.getObject());
							}
						}
					}
				}
			}
		}
		
		for(Resource superClass : JenaUtil.getSuperClasses(cls)) {
			if(!reached.contains(superClass)) {
				addDefaultValuesForType(superClass, results, reached);
			}
		}
	}
	
	
	/**
	 * Gets any declared spl:Argument that is attached to the types of a given
	 * subject via spin:constraint, that has a given predicate as its spl:predicate.
	 * @param subject  the instance to get an Argument of
	 * @param predicate  the predicate to match
	 * @return the Argument or null if none found for that type
	 */
	public static Argument getArgument(Resource subject, Property predicate) {
		SPINInstance instance = subject.as(SPINInstance.class);
		for(QueryOrTemplateCall qot : instance.getQueriesAndTemplateCalls(SPIN.constraint)) {
			if(qot.getTemplateCall() != null) {
				Template template = qot.getTemplateCall().getTemplate();
				if(SPL.Argument.equals(template)) {
					Argument a = (Argument) qot.getTemplateCall().as(Argument.class);
					if(predicate.equals(a.getPredicate())) {
						return a;
					}
				}
			}
		}
		return null;
	}

	
	private static RDFNode getDefaultValueForType(Resource cls, Property predicate, Set<Resource> reached) {
		reached.add(cls);
		StmtIterator it = cls.listProperties(SPIN.rule);
		while(it.hasNext()) {
			Statement s = it.nextStatement();
			if(s.getObject().isResource()) {
				Resource templateCall = s.getResource();
				if(templateCall.hasProperty(RDF.type, SPL.InferDefaultValue)) {
					if(templateCall.hasProperty(SPL.predicate, predicate)) {
						Statement v = templateCall.getProperty(SPL.defaultValue);
						if(v != null) {
							it.close();
							return v.getObject();
						}
					}
				}
			}
		}
		
		for(Resource superClass : JenaUtil.getSuperClasses(cls)) {
			if(!reached.contains(superClass)) {
				RDFNode value = getDefaultValueForType(superClass, predicate, reached);
				if(value != null) {
					return value;
				}
			}
		}
		
		return null;
	}
	
	
	/**
	 * Creates a Map from Properties to RDFNodes based on declared
	 * spl:InferDefaultValues.
	 * @param subject
	 * @return a Map from Properties to their default values (no null values)
	 */
	public static Map<Property,RDFNode> getDefaultValues(Resource subject) {
		Map<Property,RDFNode> results = new HashMap<Property,RDFNode>();
		Set<Resource> reached = new HashSet<Resource>();
		for(Resource type : JenaUtil.getTypes(subject)) {
			addDefaultValuesForType(type, results, reached);
		}
		return results;
	}


	/**
	 * Same as <code>getObject(subject, predicate, false)</code>.
	 * @see #getObject(Resource, Property, boolean)
	 */
	public static RDFNode getObject(Resource subject, Property predicate) {
		return getObject(subject, predicate, false);
	}
	

	/**
	 * Gets the (first) value of a subject/predicate combination.
	 * If no value exists, then it checks whether any spl:InferDefaultValue
	 * has been defined for the type(s) of the subject.
	 * No need to run inferences first.
	 * @param subject  the subject to get the object of
	 * @param predicate  the predicate
	 * @param includeSubProperties  true to also check for sub-properties of predicate
	 * @return the object or null
	 */
	public static RDFNode getObject(Resource subject, Property predicate, boolean includeSubProperties) {
		Statement s = subject.getProperty(predicate);
		if(s != null) {
			return s.getObject();
		}
		else {
			Set<Resource> reached = new HashSet<Resource>();
			for(Resource type : JenaUtil.getTypes(subject)) {
				RDFNode object = getDefaultValueForType(type, predicate, reached);
				if(object != null) {
					return object;
				}
			}
			if(includeSubProperties) {
				for(Resource subProperty : JenaUtil.getAllSubProperties(predicate)) {
					Property pred = (Property) subProperty.as(Property.class);
					RDFNode value = getObject(subject, pred, false);
					if(value != null) {
						return value;
					}
				}
			}
			
			return null;
		}
	}
	
	
	/**
	 * Checks whether a given Resource is an instance of spl:Argument (or a subclass
	 * thereof.
	 * @param resource  the Resource to test
	 * @return true if resource is an argument
	 */
	public static boolean isArgument(Resource resource) {
		return JenaUtil.hasIndirectType(resource, (Resource)SPL.Argument.inModel(resource.getModel()));
	}
	
	
	/**
	 * Checks if a given Property is a defined spl:Argument of a given subject Resource.
	 * @param subject  the subject
	 * @param predicate  the Property to test
	 * @return true  if an spl:Argument exists in the type hierarchy of subject
	 */
	public static boolean isArgumentPredicate(Resource subject, Property predicate) {
		if(SP.exists(subject.getModel())) {
			Model model = predicate.getModel();
			StmtIterator args = model.listStatements(null, SPL.predicate, predicate);
			while(args.hasNext()) {
				Resource arg = args.next().getSubject();
				if(arg.hasProperty(RDF.type, SPL.Argument)) {
					StmtIterator classes = model.listStatements(null, SPIN.constraint, arg);
					while(classes.hasNext()) {
						Resource cls = classes.next().getSubject();
						if(JenaUtil.hasIndirectType(subject, cls)) {
							classes.close();
							args.close();
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
