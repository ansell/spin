/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.examples;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
public class KennedysInferencingAndConstraintsTest {

    private OntModel ontModel;
    private Set<Object> validFunctionSources;
    private Model newTriples;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // Initialize system functions and templates
        SPINModuleRegistry.get().reset();
        SPINModuleRegistry.get().init();
        
        // Load main file
        Model baseModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        baseModel.read("http://topbraid.org/examples/kennedysSPIN");
        
        // verify that the test sources were loaded correctly
        Assert.assertEquals("Test sources were not loaded correctly", 392, baseModel.size());
        
        ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
        
        newTriples = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        ontModel.addSubModel(newTriples);

        // Register locally defined functions
        SPINModuleRegistry.get().registerAll(ontModel, "http://topbraid.org/examples/kennedysSPIN");

        validFunctionSources = new HashSet<Object>();
        
        validFunctionSources.add("http://topbraid.org/examples/kennedysSPIN");
        
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        if(ontModel != null)
        {
            ontModel.close();
            ontModel = null;
        }
        
        if(newTriples != null)
        {
            newTriples.close();
            newTriples = null;
        }
        
        validFunctionSources = null;
        
        SPINModuleRegistry.get().reset();
        SPINModuleRegistry.get().init();
    }
    
    @Test
    public void testKennedyInferencing()
    {
		// Run all inferences
		SPINInferences.run(ontModel, SPIN.rule, newTriples, null, null, false, null, validFunctionSources);
		System.out.println("Inferred triples: " + newTriples.size());
		
		Assert.assertEquals(136, newTriples.size());
    }
    
    @Test
    public void testKennedyConstraints()
    {
		// Run all constraints
		List<ConstraintViolation> cvs = SPINConstraints.check(ontModel, new LinkedList<SPINStatistics>(), null, "http://topbraid.org/examples/kennedysSPIN", validFunctionSources);
		System.out.println("Constraint violations:");
		for(ConstraintViolation cv : cvs) {
			System.out.println(" - at " + SPINLabels.get().getLabel(cv.getRoot()) + ": " + cv.getMessage());
		}

		// Without inferencing we expect two constraint violations
		Assert.assertEquals(2, cvs.size());
		
		// Run constraints on a single instance only
		Resource person = cvs.get(0).getRoot();
		List<ConstraintViolation> localCVS = SPINConstraints.check(person, new LinkedList<SPINStatistics>(), null, "http://topbraid.org/examples/kennedysSPIN", validFunctionSources);
		System.out.println("Constraint violations for " + SPINLabels.get().getLabel(person) + ": " + localCVS.size());
		
        Assert.assertEquals(1, localCVS.size());
		
	}
    
    @Test
    public void testKennedyInferencingAndConstraints()
    {
        // Run all inferences
        SPINInferences.run(ontModel, SPIN.rule, newTriples, null, null, false, null, validFunctionSources);
        System.out.println("Inferred triples: " + newTriples.size());
        
        Assert.assertEquals(136, newTriples.size());
        
        // Run all constraints
        List<ConstraintViolation> cvs = SPINConstraints.check(ontModel, new LinkedList<SPINStatistics>(), null, "http://topbraid.org/examples/kennedysSPIN", validFunctionSources);
        System.out.println("Constraint violations:");
        for(ConstraintViolation cv : cvs) {
            System.out.println(" - at " + SPINLabels.get().getLabel(cv.getRoot()) + ": " + cv.getMessage());
        }

        // with inferencing we expect 4 constraint violations
        Assert.assertEquals(4, cvs.size());
        
        // Run constraints on a single instance only
        Resource person = cvs.get(0).getRoot();
        List<ConstraintViolation> localCVS = SPINConstraints.check(person, new LinkedList<SPINStatistics>(), null, "http://topbraid.org/examples/kennedysSPIN", validFunctionSources);
        System.out.println("Constraint violations for " + SPINLabels.get().getLabel(person) + ": " + localCVS.size());
        
        Assert.assertEquals(1, localCVS.size());
        
    }
    
}
