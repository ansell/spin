/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.examples;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.hp.hpl.jena.util.FileManager;


/**
 * Demonstrates how to efficiently use an external SPIN library, such as OWL RL
 * to run inferences on a given Jena model.
 * 
 * The main trick is that the Query maps are constructed beforehand, so that the
 * actual query model does not need to include the OWL RL model at execution time.
 * 
 * @author Holger Knublauch
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class OWLRLTest {

    private static final Logger log = LoggerFactory.getLogger(OWLRLTest.class);

    private Model unionModel;
    private OntModel queryModel;
    private Model newTriples;
    private Set<Object> validFunctionSources;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // Initialize system functions and templates
        SPINModuleRegistry.get().reset();
        SPINModuleRegistry.get().init();
        
        // Load domain model with imports
        log.info("Loading domain ontology...");
        queryModel = loadModelWithImports("http://www.co-ode.org/ontologies/pizza/2007/02/12/pizza.owl");
        
        Assert.assertEquals("Test resource was not loaded correctly", 2332, queryModel.size());
        
        newTriples = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        queryModel.addSubModel(newTriples);
        
        // Load OWL RL library from the web
        log.info("Loading OWL RL ontology...");
        OntModel owlrlModel = loadModelWithImports("http://topbraid.org/spin/owlrl-all");

        Assert.assertEquals("OWL RL ontology was not loaded correctly", 3324, owlrlModel.size());

        // Register any new functions defined in OWL RL
        SPINModuleRegistry.get().registerAll(owlrlModel, "http://topbraid.org/spin/owlrl-all");
        
        // Build one big union Model of everything
        MultiUnion multiUnion = new MultiUnion(new Graph[] {
            queryModel.getGraph(),
            owlrlModel.getGraph()
        });
        unionModel = ModelFactory.createModelForGraph(multiUnion);
        
        validFunctionSources = new HashSet<Object>();
        
        validFunctionSources.add("http://topbraid.org/spin/owlrl-all");
        
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        if(newTriples != null)
        {
            newTriples.close();
            newTriples = null;
        }

        if(queryModel != null)
        {
            queryModel.close();
            queryModel = null;
        }
        
        if(unionModel != null)
        {
            unionModel.close();
            unionModel = null;
        }
        
        validFunctionSources = null;
        SPINModuleRegistry.get().reset();
        SPINModuleRegistry.get().init();
    }
    
    @Test
    public void testOWLRL()
    {
		// Collect rules (and template calls) defined in OWL RL
		Map<CommandWrapper, Map<String,RDFNode>> initialTemplateBindings = new HashMap<CommandWrapper, Map<String,RDFNode>>();
		Map<Resource,List<CommandWrapper>> cls2Query = SPINQueryFinder.getClass2QueryMap(unionModel, queryModel, SPIN.rule, true, initialTemplateBindings, false, validFunctionSources);
		Map<Resource,List<CommandWrapper>> cls2Constructor = SPINQueryFinder.getClass2QueryMap(queryModel, queryModel, SPIN.constructor, true, initialTemplateBindings, false, validFunctionSources);
		SPINRuleComparator comparator = new DefaultSPINRuleComparator(queryModel);

		// Run all inferences
		log.info("Running SPIN inferences...");
		SPINInferences.run(queryModel, newTriples, cls2Query, cls2Constructor, initialTemplateBindings, null, null, false, SPIN.rule, comparator, null, validFunctionSources);
		log.info("Inferred triples: " + newTriples.size());
		
		Assert.assertEquals(5130, newTriples.size());
	}

	
	private static OntModel loadModelWithImports(String url) {
        Model baseModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        baseModel.add(FileManager.get().loadModel(url));
        
        // TODO: make the OntModelSpec here configurable
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
	}
}
