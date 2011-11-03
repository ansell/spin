/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.inference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.base.progress.ProgressMonitor;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.QueryWrapper;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.util.SPINUtil;
import org.topbraid.spin.util.UpdateUtil;
import org.topbraid.spin.util.UpdateWrapper;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * A service to execute inference rules based on the spin:rule property.
 * 
 * @author Holger Knublauch
 */
public class SPINInferences { 
	
	
	private static boolean isRootClass(Resource cls) {
		return RDFS.Resource.equals(cls) || OWL.Thing.equals(cls);
	}
	
	
	/**
	 * Checks if a given property is a SPIN rule property.
	 * This is (currently) defined as a property that has type spin:RuleProperty
	 * or is a sub-property of spin:rule.  The latter condition may be removed
	 * at some later stage after people have upgraded to SPIN 1.1 conventions.
	 * @param property  the property to check
	 * @return true if property is a rule property
	 */
	public static boolean isRuleProperty(Property property) {
		if(SPIN.rule.equals(property)) {
			return true;
		}
		else if(JenaUtil.hasSuperProperty(property, property.getModel().getProperty(SPIN.rule.getURI()))) {
			return true;
		}
		else {
			return JenaUtil.hasIndirectType(property, (Resource)SPIN.RuleProperty.inModel(property.getModel())); 
		}
	}
	
	
	/**
	 * See the other run method for help - this is using spin:rule as rulePredicate.
	 * @param queryModel  the Model to query
	 * @param newTriples  the Model to add the new triples to 
	 * @param explanations  an optional object to write explanations to
	 * @param statistics  optional list to add statistics about which queries were slow
	 * @param singlePass  true to just do a single pass (don't iterate)
	 * @param monitor  an optional ProgressMonitor
	 * @return the number of iterations (1 with singlePass)
	 * @see #run(Model, Property, Model, SPINExplanations, List, boolean, ProgressMonitor)
	 */
	public static int run(
			Model queryModel, 
			Model newTriples,
			SPINExplanations explanations,
			List<SPINStatistics> statistics,
			boolean singlePass, 
			ProgressMonitor monitor) {
		return run(queryModel, SPIN.rule, newTriples, explanations, statistics, singlePass, monitor, Collections.emptySet());
	}
	
	
	/**
	 * Iterates over all SPIN rules in a (query) Model and adds all constructed
	 * triples to a given Model (newTriples) until no further changes have been
	 * made within one iteration.
	 * Note that in order to iterate more than single pass, the newTriples Model
	 * must be a sub-model of the queryModel (which likely has to be an OntModel).
	 * The supplied rulePredicate is usually spin:rule, but can also be a sub-
	 * property of spin:rule to exercise finer control over which rules to fire.
	 * @param queryModel  the Model to query
	 * @param rulePredicate  the rule predicate (spin:rule or a sub-property thereof)
	 * @param newTriples  the Model to add the new triples to 
	 * @param explanations  an optional object to write explanations to
	 * @param statistics  optional list to add statistics about which queries were slow
	 * @param singlePass  true to just do a single pass (don't iterate)
	 * @param monitor  an optional ProgressMonitor
	 * @return the number of iterations (1 with singlePass)
	 */
	public static int run(
			Model queryModel,
			Property rulePredicate,
			Model newTriples,
			SPINExplanations explanations,
			List<SPINStatistics> statistics,
			boolean singlePass, 
			ProgressMonitor monitor) {
	    
	    return run(queryModel, rulePredicate, newTriples, explanations, statistics, singlePass, monitor, Collections.emptySet());
	}
	
    /**
     * Iterates over all SPIN rules in a (query) Model and adds all constructed
     * triples to a given Model (newTriples) until no further changes have been
     * made within one iteration.
     * Note that in order to iterate more than single pass, the newTriples Model
     * must be a sub-model of the queryModel (which likely has to be an OntModel).
     * The supplied rulePredicate is usually spin:rule, but can also be a sub-
     * property of spin:rule to exercise finer control over which rules to fire.
     * @param queryModel  the Model to query
     * @param rulePredicate  the rule predicate (spin:rule or a sub-property thereof)
     * @param newTriples  the Model to add the new triples to 
     * @param explanations  an optional object to write explanations to
     * @param statistics  optional list to add statistics about which queries were slow
     * @param singlePass  true to just do a single pass (don't iterate)
     * @param monitor  an optional ProgressMonitor
     * @return the number of iterations (1 with singlePass)
     */
    public static int run(
            Model queryModel,
            Property rulePredicate,
            Model newTriples,
            SPINExplanations explanations,
            List<SPINStatistics> statistics,
            boolean singlePass, 
            ProgressMonitor monitor,
            Set<Object> validFunctionSources) {
		Map<CommandWrapper, Map<String,RDFNode>> initialTemplateBindings = new HashMap<CommandWrapper, Map<String,RDFNode>>();
		Map<Resource,List<CommandWrapper>> cls2Query = SPINQueryFinder.getClass2QueryMap(queryModel, queryModel, rulePredicate, true, initialTemplateBindings, false, validFunctionSources);
		Map<Resource,List<CommandWrapper>> cls2Constructor = SPINQueryFinder.getClass2QueryMap(queryModel, queryModel, SPIN.constructor, true, initialTemplateBindings, false, validFunctionSources);
		SPINRuleComparator comparator = new DefaultSPINRuleComparator(queryModel);
		return run(queryModel, newTriples, cls2Query, cls2Constructor, initialTemplateBindings, explanations, statistics, singlePass, rulePredicate, comparator, monitor, validFunctionSources);
	}

	
	/**
	 * Iterates over a provided collection of SPIN rules and adds all constructed
	 * triples to a given Model (newTriples) until no further changes have been
	 * made within one iteration.
	 * Note that in order to iterate more than single pass, the newTriples Model
	 * must be a sub-model of the queryModel (which likely has to be an OntModel).
	 * @param queryModel  the Model to query
	 * @param newTriples  the Model to add the new triples to 
	 * @param class2Query  the map of queries to run (see SPINQueryFinder)
	 * @param class2Constructor  the map of constructors to run
	 * @param templateBindings  initial template bindings (see SPINQueryFinder)
	 * @param explanations  an optional object to write explanations to
	 * @param statistics  optional list to add statistics about which queries were slow
	 * @param singlePass  true to just do a single pass (don't iterate)
	 * @param rulePredicate  the predicate used (e.g. spin:rule)
	 * @param comparator  optional comparator to determine the order of rule execution
	 * @param monitor  an optional ProgressMonitor
	 * @return the number of iterations (1 with singlePass)
	 */
	public static int run(
			Model queryModel,
			Model newTriples,
			Map<Resource, List<CommandWrapper>> class2Query,
			Map<Resource, List<CommandWrapper>> class2Constructor,
			Map<CommandWrapper, Map<String, RDFNode>> templateBindings,
			SPINExplanations explanations,
			List<SPINStatistics> statistics,
			boolean singlePass,
			Property rulePredicate,
			SPINRuleComparator comparator,
			ProgressMonitor monitor) {
		
	    return run(queryModel, newTriples, class2Query, class2Constructor, templateBindings, explanations, statistics, singlePass, rulePredicate, comparator, monitor, Collections.emptySet());
	}
	
    /**
     * Iterates over a provided collection of SPIN rules and adds all constructed
     * triples to a given Model (newTriples) until no further changes have been
     * made within one iteration.
     * Note that in order to iterate more than single pass, the newTriples Model
     * must be a sub-model of the queryModel (which likely has to be an OntModel).
     * @param queryModel  the Model to query
     * @param newTriples  the Model to add the new triples to 
     * @param class2Query  the map of queries to run (see SPINQueryFinder)
     * @param class2Constructor  the map of constructors to run
     * @param templateBindings  initial template bindings (see SPINQueryFinder)
     * @param explanations  an optional object to write explanations to
     * @param statistics  optional list to add statistics about which queries were slow
     * @param singlePass  true to just do a single pass (don't iterate)
     * @param rulePredicate  the predicate used (e.g. spin:rule)
     * @param comparator  optional comparator to determine the order of rule execution
     * @param monitor  an optional ProgressMonitor
     * @return the number of iterations (1 with singlePass)
     */
    public static int run(
            Model queryModel,
            Model newTriples,
            Map<Resource, List<CommandWrapper>> class2Query,
            Map<Resource, List<CommandWrapper>> class2Constructor,
            Map<CommandWrapper, Map<String, RDFNode>> templateBindings,
            SPINExplanations explanations,
            List<SPINStatistics> statistics,
            boolean singlePass,
            Property rulePredicate,
            SPINRuleComparator comparator,
            ProgressMonitor monitor,
            Set<Object> validFunctionSources) {
		// Get sorted list of Rules and remember where they came from
		List<CommandWrapper> rulesList = new ArrayList<CommandWrapper>();
		Map<CommandWrapper,Resource> rule2Class = new HashMap<CommandWrapper,Resource>();
		for(Resource cls : class2Query.keySet()) {
			List<CommandWrapper> queryWrappers = class2Query.get(cls);
			for(CommandWrapper queryWrapper : queryWrappers) {
				rulesList.add(queryWrapper);
				rule2Class.put(queryWrapper, cls);
			}
		}
		if(comparator != null) {
			Collections.sort(rulesList, comparator);
		}
		
		// Make sure the rulePredicate has a Model attached to it
		if(rulePredicate.getModel() == null) {
			rulePredicate = queryModel.getProperty(rulePredicate.getURI());
		}
		
		// Iterate
		int iteration = 1;
		boolean changed;
		do {
			Set<Statement> newRules = new HashSet<Statement>();
			changed = false;
			for(CommandWrapper arqWrapper : rulesList) {
				
				// Skip rule if needed
				Property predicate = arqWrapper.getStatement().getPredicate();
				Integer maxIterationCount = JenaUtil.getIntegerProperty(predicate, SPIN.rulePropertyMaxIterationCount);
				if(maxIterationCount != null) {
					if(iteration > maxIterationCount) {
						continue;
					}
				}
				
				Resource cls = rule2Class.get(arqWrapper);
					
				if(monitor != null) {
					
					if(monitor.isCanceled()) {
						return iteration - 1;
					}
					
					StringBuffer sb = new StringBuffer("TopSPIN iteration ");
					sb.append(iteration);
					sb.append(" at ");
					sb.append(SPINLabels.get().getLabel(cls));
					sb.append(", rule ");
					sb.append(arqWrapper.getLabel() != null ? arqWrapper.getLabel() : arqWrapper.getText());
					monitor.subTask(sb.toString());
				}

				StringBuffer sb = new StringBuffer();
				sb.append("Inferred by ");
				sb.append(SPINLabels.get().getLabel(rulePredicate));
				sb.append(" at class ");
				sb.append(SPINLabels.get().getLabel(cls));
				sb.append(":\n\n" + arqWrapper.getText());
				String explanationText = sb.toString();
				Map<String,RDFNode> initialBindings = templateBindings.get(arqWrapper);
				boolean thisUnbound = arqWrapper.isThisUnbound();
				changed |= runCommandOnClass(arqWrapper, arqWrapper.getLabel(), queryModel, newTriples, cls, true, class2Constructor, templateBindings, initialBindings, statistics, explanations, explanationText, newRules, thisUnbound, monitor);
				if(!isRootClass(cls) && !thisUnbound) {
					Set<Resource> subClasses = JenaUtil.getAllSubClasses(cls);
					for(Resource subClass : subClasses) {
						changed |= runCommandOnClass(arqWrapper, arqWrapper.getLabel(), queryModel, newTriples, subClass, true, class2Constructor, templateBindings, initialBindings, statistics, explanations, explanationText, newRules, thisUnbound, monitor);
					}
				}
			}
			iteration++;
			
			if(!newRules.isEmpty() && !singlePass) {
				for(Statement s : newRules) {
					SPINQueryFinder.add(class2Query, queryModel.asStatement(s.asTriple()), queryModel, true, templateBindings, false, validFunctionSources);
				}
			}
		}
		while(!singlePass && changed);
		
		return iteration - 1;
	}

	
	private static boolean runCommandOnClass(
			CommandWrapper commandWrapper, 
			String queryLabel, 
			final Model queryModel, 
			Model newTriples, 
			Resource cls, 
			boolean checkContains, 
			Map<Resource, List<CommandWrapper>> class2Constructor,
			Map<CommandWrapper,Map<String,RDFNode>> initialTemplateBindings,
			Map<String,RDFNode> initialBindings, 
			List<SPINStatistics> statistics, 
			SPINExplanations explanations, 
			String explanationText, 
			Set<Statement> newRules, 
			boolean thisUnbound, 
			ProgressMonitor monitor) {
		
		// Check if query is needed at all
		if(thisUnbound || isRootClass(cls) || queryModel.contains(null, RDF.type, cls)) {
			boolean changed = false;
			QuerySolutionMap bindings = new QuerySolutionMap();
			if(!isRootClass(cls) && !thisUnbound) {
				bindings.add(SPINUtil.TYPE_CLASS_VAR_NAME, cls);
			}
			if(initialBindings != null) {
				for(String varName : initialBindings.keySet()) {
					RDFNode value = initialBindings.get(varName);
					bindings.add(varName, value);
				}
			}
			long startTime = System.currentTimeMillis();
			final Map<Resource,Resource> newInstances = new HashMap<Resource,Resource>();
			if(commandWrapper instanceof QueryWrapper) {
				Query arq = ((QueryWrapper)commandWrapper).getQuery();
				QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, queryModel);
				qexec.setInitialBinding(bindings);
				Model cm = qexec.execConstruct();
				StmtIterator cit = cm.listStatements();
				while(cit.hasNext()) {
					Statement s = cit.nextStatement();
					if(!checkContains || !queryModel.contains(s)) {
						changed = true;
						newTriples.add(s);
						if(explanations != null) {
							Resource source = commandWrapper.getStatement().getSubject();
							explanations.put(s.asTriple(), explanationText, source.asNode());
						}
						
						// New rdf:type triple -> run constructors later
						if(RDF.type.equals(s.getPredicate()) && s.getObject().isResource()) {
							Resource subject = (Resource)s.getSubject().inModel(queryModel);
							newInstances.put(subject, s.getResource());
						}
						
						if(SPIN.rule.equals(s.getPredicate())) {
							newRules.add(s);
						}
					}
				}
			}
			else {
				UpdateWrapper updateWrapper = (UpdateWrapper) commandWrapper;
				Map<String,RDFNode> templateBindings = initialTemplateBindings.get(commandWrapper);
				Dataset dataset = ARQFactory.get().getDataset(queryModel);
				Iterable<Graph> updateGraphs = UpdateUtil.getUpdatedGraphs(updateWrapper.getUpdate(), dataset, templateBindings);
				ControlledUpdateGraphStore cugs = new ControlledUpdateGraphStore(dataset, updateGraphs);
				UpdateProcessor up = UpdateExecutionFactory.create(updateWrapper.getUpdate(), cugs, bindings);
				up.execute();
				for(ControlledUpdateGraph cug : cugs.getControlledUpdateGraphs()) {
					changed |= cug.isChanged();
					for(Triple triple : cug.getAddedTriples()) {
						if(RDF.type.asNode().equals(triple.getPredicate()) && !triple.getObject().isLiteral()) {
							Resource subject = (Resource) queryModel.asRDFNode(triple.getSubject());
							newInstances.put(subject, (Resource)queryModel.asRDFNode(triple.getObject()));
						}
					}
				}
			}
			
			if(statistics != null) {
				long endTime = System.currentTimeMillis();
				long duration = (endTime - startTime);
				String queryText = SPINLabels.get().getLabel(commandWrapper.getSPINCommand());
				if(queryLabel == null) {
					queryLabel = queryText;
				}
				statistics.add(new SPINStatistics(queryLabel, queryText, duration, startTime, cls.asNode()));
			}
			
			if(!newInstances.isEmpty()) {
				List<Resource> newRs = new ArrayList<Resource>(newInstances.keySet());
				SPINConstructors.construct(
						queryModel, 
						newRs, 
						newTriples, 
						new HashSet<Resource>(), 
						class2Constructor,
						initialTemplateBindings,
						statistics,
						explanations, 
						monitor);
			}
			
			return changed;
		}
		else {
			return false;
		}
	}

	
	/**
	 * Runs a given Jena Query on a given instance and adds the inferred triples
	 * to a given Model.
	 * @param arq  the CONSTRUCT query to execute
	 * @param queryModel  the query Model
	 * @param newTriples  the Model to write the triples to
	 * @param instance  the instance to run the inferences on
	 * @param checkContains  true to only call add if a Triple wasn't there yet
	 * @param initialBindings  the initial bindings for arq or null
	 * @return true if changes were done (only meaningful if checkContains == true)
	 */
	public static boolean runQueryOnInstance(Query arq, Model queryModel, Model newTriples, Resource instance, boolean checkContains, Map<String,RDFNode> initialBindings) {
		boolean changed = false;
		QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, queryModel);
		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add(SPIN.THIS_VAR_NAME, instance);
		if(initialBindings != null) {
			for(String varName : initialBindings.keySet()) {
				RDFNode value = initialBindings.get(varName);
				bindings.add(varName, value);
			}
		}
		qexec.setInitialBinding(bindings);
		Model cm = qexec.execConstruct();
		StmtIterator cit = cm.listStatements();
		while(cit.hasNext()) {
			Statement s = cit.nextStatement();
			if(!checkContains || !queryModel.contains(s)) {
				changed = true;
				newTriples.add(s);
			}
		}
		return changed;
	}
}
