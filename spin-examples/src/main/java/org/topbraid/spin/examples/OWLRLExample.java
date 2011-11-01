/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.topbraid.spin.inference.DefaultSPINRuleComparator;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.inference.SPINRuleComparator;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.ReificationStyle;


/**
 * Demonstrates how to efficiently use an external SPIN library, such as OWL RL
 * to run inferences on a given Jena model.
 * 
 * The main trick is that the Query maps are constructed beforehand, so that the
 * actual query model does not need to include the OWL RL model at execution time.
 * 
 * @author Holger Knublauch
 */
public class OWLRLExample {

	public static void main(String[] args) {
		
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();

		// Load domain model with imports
		System.out.println("Loading domain ontology...");
		OntModel queryModel = loadModelWithImports("http://www.co-ode.org/ontologies/pizza/2007/02/12/pizza.owl");
		
		// Create and add Model for inferred triples
		Model newTriples = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
		queryModel.addSubModel(newTriples);
		
		// Load OWL RL library from the web
		System.out.println("Loading OWL RL ontology...");
		OntModel owlrlModel = loadModelWithImports("http://topbraid.org/spin/owlrl-all");

		// Register any new functions defined in OWL RL
		SPINModuleRegistry.get().registerAll(owlrlModel, null);
		
		// Build one big union Model of everything
		MultiUnion multiUnion = new MultiUnion(new Graph[] {
			queryModel.getGraph(),
			owlrlModel.getGraph()
		});
		Model unionModel = ModelFactory.createModelForGraph(multiUnion);
		
		// Collect rules (and template calls) defined in OWL RL
		Map<CommandWrapper, Map<String,RDFNode>> initialTemplateBindings = new HashMap<CommandWrapper, Map<String,RDFNode>>();
		Map<Resource,List<CommandWrapper>> cls2Query = SPINQueryFinder.getClass2QueryMap(unionModel, queryModel, SPIN.rule, true, initialTemplateBindings, false, SPINModuleRegistry.get());
		Map<Resource,List<CommandWrapper>> cls2Constructor = SPINQueryFinder.getClass2QueryMap(queryModel, queryModel, SPIN.constructor, true, initialTemplateBindings, false, SPINModuleRegistry.get());
		SPINRuleComparator comparator = new DefaultSPINRuleComparator(queryModel);

		// Run all inferences
		System.out.println("Running SPIN inferences...");
		SPINInferences.run(queryModel, newTriples, cls2Query, cls2Constructor, initialTemplateBindings, null, null, false, SPIN.rule, comparator, null, SPINModuleRegistry.get());
		System.out.println("Inferred triples: " + newTriples.size());
	}

	
	private static OntModel loadModelWithImports(String url) {
		Model baseModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
		baseModel.read(url);
		return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
	}
}
