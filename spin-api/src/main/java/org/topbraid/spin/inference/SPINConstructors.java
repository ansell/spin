/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.inference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.base.progress.ProgressMonitor;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.AbstractGraphListener;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.QueryWrapper;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.util.UpdateUtil;
import org.topbraid.spin.util.UpdateWrapper;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Static methods to find and execute spin:constructors for a given
 * set of Resources.
 * 
 * @author Holger Knublauch
 */
public class SPINConstructors {
	 
	/**
	 * Runs the constructors on a List of Resources.
	 * @param queryModel  the model to query over
	 * @param instances  the instances to run the constructors of
	 * @param targetModel  the model that shall receive the new triples
	 * @param monitor  an optional progress monitor
	 */
	public static void construct(Model queryModel, List<Resource> instances, Model targetModel, ProgressMonitor monitor) {
	    construct(queryModel, instances, targetModel, monitor, Collections.emptySet());
	}
	
    /**
     * Runs the constructors on a List of Resources.
     * @param queryModel  the model to query over
     * @param instances  the instances to run the constructors of
     * @param targetModel  the model that shall receive the new triples
     * @param monitor  an optional progress monitor
     * @param validFunctionSources a set of objects given in SPINModuleRegistry.registerAll that are valid in this case
     */
    public static void construct(Model queryModel, List<Resource> instances, Model targetModel, ProgressMonitor monitor, Set<Object> validFunctionSources) {
	    Map<CommandWrapper,Map<String,RDFNode>> initialTemplateBindings = new HashMap<CommandWrapper,Map<String,RDFNode>>();
		Map<Resource,List<CommandWrapper>> class2Constructor = SPINQueryFinder.getClass2QueryMap(queryModel, queryModel, SPIN.constructor, true, initialTemplateBindings, false, validFunctionSources);
		construct(queryModel, instances, targetModel, new HashSet<Resource>(), class2Constructor, initialTemplateBindings, monitor);
	}

	
	/**
	 * Runs the constructors on a List of Resources.
	 * @param queryModel  the model to query over
	 * @param instances  the instances to run the constructors of
	 * @param targetModel  the model that shall receive the new triples
	 * @param reached  the Set of already reached Resources
	 * @param monitor  an optional progress monitor
	 */
	public static void construct(
			Model queryModel, 
			List<Resource> instances, 
			Model targetModel,
			Set<Resource> reached,
			Map<Resource,List<CommandWrapper>> class2Constructor,
			Map<CommandWrapper,Map<String,RDFNode>> initialTemplateBindings,
			ProgressMonitor monitor) {
		construct(queryModel, instances, targetModel, reached, class2Constructor, initialTemplateBindings, null, null, monitor);
	}
	
	
	/**
	 * Runs the constructors on a List of Resources.
	 * @param queryModel  the model to query over
	 * @param instances  the instances to run the constructors of
	 * @param targetModel  the model that shall receive the new triples
	 * @param reached  the Set of already reached Resources 
	 * @param explanations  an (optional) explanations object
	 * @param monitor  an optional progress monitor
	 */
	public static void construct(
			Model queryModel, 
			List<Resource> instances, 
			Model targetModel, 
			Set<Resource> reached, 
			Map<Resource, List<CommandWrapper>> class2Constructor,
			Map<CommandWrapper,Map<String,RDFNode>> initialTemplateBindings,
			List<SPINStatistics> statistics,
			SPINExplanations explanations, 
			ProgressMonitor monitor) {
		if(!instances.isEmpty()) {
			List<Resource> newResources = new ArrayList<Resource>();
			for(Resource instance : instances) {
				if(!reached.contains(instance)) {
					reached.add(instance);
					constructInstance(queryModel, instance, targetModel, newResources, class2Constructor, initialTemplateBindings, statistics, explanations, monitor);
				}
			}
			construct(queryModel, newResources, targetModel, reached, class2Constructor, initialTemplateBindings, statistics, explanations, monitor);
		}
	}
	

	/**
	 * Runs constructors for a single instance.
	 * @param queryModel  the model to query
	 * @param instance  the instance to run the constructors of
	 * @param targetModel  the model that will receive the new triples
	 * @param newResources  will hold the newly constructed instances
	 * @param monitor  an optional progress monitor
	 */
	public static void constructInstance(
			Model queryModel, 
			Resource instance,
			Model targetModel, 
			List<Resource> newResources, 
			Map<Resource, List<CommandWrapper>> class2Constructor,
			Map<CommandWrapper,Map<String,RDFNode>> initialTemplateBindings,
			List<SPINStatistics> statistics,
			SPINExplanations explanations, 
			ProgressMonitor monitor) {
		for(Statement s : JenaUtil.getStatementsList(instance.listProperties(RDF.type))) {
			Resource type = s.getResource();
			constructInstance(queryModel, instance, type, targetModel, newResources, new HashSet<Resource>(), class2Constructor, initialTemplateBindings, statistics, explanations, monitor);
		}
	}

	
	/**
	 * Runs all constructors defined for a given type on a given instance.
	 * @param queryModel  the model to query
	 * @param instance  the instance to run the constructors of
	 * @param type  the class to run the constructors of
	 * @param targetModel  the model that will receive the new triples
	 * @param newResources  will hold the newly constructed instances
	 * @param reachedTypes  contains the already reached types
	 * @param monitor  an optional progress monitor
	 */
	public static void constructInstance(Model queryModel, Resource instance, Resource type, Model targetModel, List<Resource> newResources, Set<Resource> reachedTypes, ProgressMonitor monitor) {
		constructInstance(queryModel, instance, type, targetModel, newResources, reachedTypes, monitor);
	}
	

	/**
	 * Runs all constructors defined for a given type on a given instance.
	 * @param queryModel  the model to query
	 * @param instance  the instance to run the constructors of
	 * @param type  the class to run the constructors of
	 * @param targetModel  the model that will receive the new triples
	 * @param newResources  will hold the newly constructed instances
	 * @param reachedTypes  contains the already reached types
	 * @param explanations  the explanations (optional)
	 * @param monitor  an optional progress monitor
	 */
	public static void constructInstance(
			Model queryModel, 
			Resource instance, 
			Resource type, 
			Model targetModel, 
			List<Resource> newResources, 
			Set<Resource> reachedTypes,
			Map<Resource, List<CommandWrapper>> class2Constructor,
			Map<CommandWrapper,Map<String,RDFNode>> initialTemplateBindings,
			List<SPINStatistics> statistics,
			SPINExplanations explanations, 
			ProgressMonitor monitor) {
		
		// Run superclass constructors first
		for(Statement s : JenaUtil.getStatementsList(type.listProperties(RDFS.subClassOf))) {
			Resource superClass = s.getResource();
			if(!reachedTypes.contains(superClass)) {
				reachedTypes.add(superClass);
				constructInstance(
						queryModel, 
						instance, 
						superClass, 
						targetModel, 
						newResources, 
						reachedTypes, 
						class2Constructor,
						initialTemplateBindings,
						statistics,
						explanations, 
						monitor);
			}
		}

		List<CommandWrapper> commandWrappers = class2Constructor.get(type);
		if(commandWrappers != null) {
		
			for(CommandWrapper commandWrapper : commandWrappers) {
				
				QuerySolutionMap bindings = new QuerySolutionMap();
				if(instance != null) {
					bindings.add(SPIN.THIS_VAR_NAME, instance);
				}
				Map<String,RDFNode> initialBindings = initialTemplateBindings.get(commandWrapper);
				if(initialBindings != null) {
					for(String varName : initialBindings.keySet()) {
						RDFNode value = initialBindings.get(varName);
						bindings.add(varName, value);
					}
				}
				
				if(monitor != null) {
					monitor.subTask("TopSPIN constructor at " + SPINLabels.get().getLabel(instance) + ": " + commandWrapper.getText());
				}
				
				long startTime = System.currentTimeMillis();
				
				if(commandWrapper instanceof QueryWrapper) {
					
					final List<Triple> triples = new LinkedList<Triple>();
					AbstractGraphListener listener = new AbstractGraphListener() {
						
						@Override
						public void notifyAddTriple(Graph g, Triple t) {
							triples.add(t);
						}
						
						@Override
						public void notifyDeleteTriple(Graph g, Triple t) {
						}
						
						@Override
						protected void notifyRemoveAll(Graph source, Triple pattern) {
						}
					};
					
					QueryWrapper queryWrapper = (QueryWrapper) commandWrapper;
					Query arqQuery = queryWrapper.getQuery();
					if(arqQuery.isConstructType()) {
						
						QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, queryModel);
						qexec.setInitialBinding(bindings);
						
						// Execute construct and remember the order in which triples were inserted
						// Note that this does not work yet since Jena appears to have random order
						Model resultModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
						resultModel.getGraph().getEventManager().register(listener);
						qexec.execConstruct(resultModel);
						
						StringBuffer sb = new StringBuffer();
						sb.append("Inferred by SPIN constructor at class ");
						sb.append(SPINLabels.get().getLabel(type));
						sb.append(":\n\n" + commandWrapper.getText());
						String explanationText = sb.toString();
		
						// Add all new triples and any new resources
						for(Triple triple : triples) {
							Statement rs = queryModel.asStatement(triple);
							if(!targetModel.contains(rs)) {
								targetModel.add(rs);
								if(RDF.type.equals(rs.getPredicate())) {
									Resource subject = rs.getSubject();
									if(!newResources.contains(subject)) {
										newResources.add(subject);
									}
								}
								if(explanations != null) {
									Resource source = commandWrapper.getStatement().getSubject();
									explanations.put(triple, explanationText, source.asNode());
								}
							}
						}
					}
				}
				else if(commandWrapper instanceof UpdateWrapper) {
					Update update = ((UpdateWrapper)commandWrapper).getUpdate();
					Dataset dataset = ARQFactory.get().getDataset(queryModel);
					Collection<Graph> updateGraphs = UpdateUtil.getUpdatedGraphs(update, dataset, initialBindings);
					ControlledUpdateGraphStore cugs = new ControlledUpdateGraphStore(dataset, updateGraphs);
					UpdateProcessor up = UpdateExecutionFactory.create(update, cugs, bindings);
					up.execute();
					for(ControlledUpdateGraph cug : cugs.getControlledUpdateGraphs()) {
						for(Triple triple : cug.getAddedTriples()) {
							Statement rs = queryModel.asStatement(triple);
							if(RDF.type.equals(rs.getPredicate())) {
								Resource subject = rs.getSubject();
								if(!newResources.contains(subject)) {
									newResources.add(subject);
								}
							}
						}
					}
				}
				
				long endTime = System.currentTimeMillis();
				if(statistics != null) {
					String queryText = SPINLabels.get().getLabel(commandWrapper.getSPINCommand());
					String label = commandWrapper.getLabel();
					if(label == null) {
						label = queryText;
					}
					statistics.add(new SPINStatistics(label, queryText, endTime - startTime, startTime, instance.asNode()));
				}
			}
		}
	}
	
	
	/**
	 * Runs all constructors on all instances in a given model.
	 * @param queryModel  the query model
	 * @param targetModel  the model to write the new triples to
	 * @param monitor  an optional progress monitor
	 */
	public static void constructAll(Model queryModel, Model targetModel, ProgressMonitor monitor) {
	    constructAll(queryModel, targetModel, monitor, Collections.emptySet());
	}
	
    /**
     * Runs all constructors on all instances in a given model.
     * @param queryModel  the query model
     * @param targetModel  the model to write the new triples to
     * @param monitor  an optional progress monitor
     * @param validFunctionSources a set of objects defined using SPINModuleRegistry.registerAll that define valid sources for functions
     */
    public static void constructAll(Model queryModel, Model targetModel, ProgressMonitor monitor, Set<Object> validFunctionSources) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, queryModel);
        
        constructAll(ontModel, targetModel, monitor, validFunctionSources);
    }
    
    /**
     * Construct using an OntModel
     * 
     * This method is necessary to enable custom LocationMappers, which are derived through OntModelSpec instances.
     * OntModelSpec.OWL_MEM was hardcoded previously, which used the default Jena LocationMapper
     * 
     * @param ontModel
     * @param targetModel
     * @param monitor
     * @param validFunctionSources
     */
    public static void constructAll(OntModel ontModel, Model targetModel, ProgressMonitor monitor, Set<Object> validFunctionSources) {
        Set<Resource> classes = getClassesWithConstructor(ontModel);
		List<Resource> instances = new ArrayList<Resource>(getInstances(classes));
		if(targetModel != ontModel) {
			ontModel.addSubModel(targetModel);
		}
		Map<CommandWrapper,Map<String,RDFNode>> initialTemplateBindings = new HashMap<CommandWrapper,Map<String,RDFNode>>();
		Map<Resource,List<CommandWrapper>> class2Constructor = SPINQueryFinder.getClass2QueryMap(ontModel, ontModel, SPIN.constructor, true, initialTemplateBindings, false, validFunctionSources);
		construct(ontModel, instances, targetModel, new HashSet<Resource>(), class2Constructor, initialTemplateBindings, monitor);
	}
	

	/**
	 * Finds all classes that directly have a spin:constructor attached
	 * to it.
	 * @param model  the Model to operate on
	 * @return a Set of classes
	 */
	public static Set<Resource> getClassesWithConstructor(Model model) {
		Set<Resource> results = new HashSet<Resource>();
		for(Property property : getConstructorProperties(model)) {
			StmtIterator it = model.listStatements(null, property, (RDFNode)null);
			while(it.hasNext()) {
				results.add(it.next().getSubject());
			}
		}
		return results;
	}
	
	
	private static Iterable<Property> getConstructorProperties(Model model) {
		List<Property> results = new ArrayList<Property>();
		for(Resource r : JenaUtil.getAllSubProperties(model.getProperty(SPIN.constructor.getURI()))) {
			results.add(model.getProperty(r.getURI()));
		}
		results.add(model.getProperty(SPIN.constructor.getURI()));
		return results;
	}
	
	
	private static Set<Resource> getInstances(Collection<Resource> classes) {
		Set<Resource> results = new HashSet<Resource>();
		for(Resource cls : classes) {
			results.addAll(JenaUtil.getAllInstances(cls));
		}
		return results;
	}
	
	
	/**
	 * Checks whether a given class or a superclass thereof has a
	 * constructor.
	 * @param cls  the class to check
	 * @return true if cls has a constructor
	 */
	public static boolean hasConstructor(Resource cls) {
		for(Property property : getConstructorProperties(cls.getModel())) {
			if(cls.hasProperty(property)) {
				return true;
			}
			else {
				for(Resource superClass : JenaUtil.getAllSuperClasses(cls)) {
					if(superClass.hasProperty(property)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
