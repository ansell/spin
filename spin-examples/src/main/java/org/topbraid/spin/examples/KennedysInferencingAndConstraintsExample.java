/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.examples;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.topbraid.spin.constraints.ConstraintViolation;
import org.topbraid.spin.constraints.SPINConstraints;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.ReificationStyle;


/**
 * Loads the Kennedys SPIN ontology and runs inferences and then
 * constraint checks on it.
 * 
 * @author Holger Knublauch
 */
public class KennedysInferencingAndConstraintsExample {

	public static void main(String[] args) {
		
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();

		// Load main file
		Model baseModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
		baseModel.read("http://topbraid.org/examples/kennedysSPIN");
		
		// Create OntModel with imports
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
		
		// Create and add Model for inferred triples
		Model newTriples = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
		ontModel.addSubModel(newTriples);

		// Register locally defined functions
		SPINModuleRegistry.get().registerAll(ontModel, "http://topbraid.org/examples/kennedysSPIN");

        Set<Object> validFunctionSources = new HashSet<Object>();
        
        validFunctionSources.add("http://topbraid.org/examples/kennedysSPIN");
        
		// Run all inferences
		SPINInferences.run(ontModel, SPIN.rule, newTriples, null, null, false, null, validFunctionSources);
		System.out.println("Inferred triples: " + newTriples.size());

		// Run all constraints
		List<ConstraintViolation> cvs = SPINConstraints.check(ontModel, new LinkedList<SPINStatistics>(), null, "http://topbraid.org/examples/kennedysSPIN", validFunctionSources);
		System.out.println("Constraint violations:");
		for(ConstraintViolation cv : cvs) {
			System.out.println(" - at " + SPINLabels.get().getLabel(cv.getRoot()) + ": " + cv.getMessage());
		}

		// Run constraints on a single instance only
		Resource person = cvs.get(0).getRoot();
		List<ConstraintViolation> localCVS = SPINConstraints.check(person, new LinkedList<SPINStatistics>(), null, "http://topbraid.org/examples/kennedysSPIN", validFunctionSources);
		System.out.println("Constraint violations for " + SPINLabels.get().getLabel(person) + ": " + localCVS.size());
	}
}
