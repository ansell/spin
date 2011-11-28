/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.examples;

import java.io.StringWriter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TripleBoundary;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelExtract;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StatementTripleBoundary;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * Converts between textual SPARQL representation and SPIN RDF model.
 * 
 * @author Holger Knublauch
 */
public class SPINParsingTest {

    private Model model;
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // Register system functions (such as sp:gt (>))
        // Initialize system functions and templates
        //SPINModuleRegistry.get().reset();
        //SPINModuleRegistry.get().init();
        
        model = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("ex", "http://example.org/demo#");
        

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        
        if(model != null)
        {
            model.close();
            model = null;
        }
        
        // Initialize system functions and templates
        //SPINModuleRegistry.get().reset();
        //SPINModuleRegistry.get().init();
        
    }   
    
    @Test
    public void testSPINParser() 
    {
		
		String query =
			"SELECT ?person\n" +
			"WHERE {\n" +
			"    ?person a ex:Person .\n" +
			"    ?person ex:age ?age .\n" +
			"    FILTER (?age > 18) .\n" +
			"}";
		
		Query arqQuery = ARQFactory.get().createQuery(model, query);
		ARQ2SPIN arq2SPIN = new ARQ2SPIN(model);
		Select spinQuery = (Select) arq2SPIN.createQuery(arqQuery, null);
		
		StringWriter output = new StringWriter();
		
		System.out.println("SPIN query in Turtle:");
		model.write(output, FileUtils.langTurtle);
		
		String turtleString = output.toString();
		
		System.out.println(turtleString);
		
		Assert.assertTrue(turtleString.contains("sp:Select"));
		
        Assert.assertTrue(turtleString.contains("sp:resultVariables"));

        Assert.assertTrue(turtleString.contains("sp:where"));

        Assert.assertTrue(turtleString.contains("ex:Person"));

        Assert.assertTrue(turtleString.contains("ex:age"));

        Assert.assertTrue(turtleString.contains("sp:Filter"));

        Assert.assertTrue(turtleString.contains("sp:expression"));

        Assert.assertTrue(turtleString.contains("sp:gt"));
        
        Assert.assertTrue(turtleString.contains("18"));
        
        System.out.println("-----");
		String str = spinQuery.toString();
		System.out.println("SPIN query:\n" + str);
		
		Assert.assertTrue(str.contains("?age > 18"));
		
		// Now turn it back into a Jena Query
		Query parsedBack = ARQFactory.get().createQuery(spinQuery);
		System.out.println("Jena query:\n" + parsedBack);
		
		Assert.assertTrue(contains(parsedBack, "?age > 18"));
	}
    
    @Test
    public void testBug()
    {
        String query =
            "SELECT ?person\n" +
            "WHERE {\n" +
            "    ?person a ex:Person .\n" +
            "    ?person ex:age ?age .\n" +
            "    FILTER (?age > 18) .\n" +
            "}";
        
        Query arqQuery = ARQFactory.get().createQuery(model, query);
        ARQ2SPIN arq2SPIN = new ARQ2SPIN(model);
        Select spinQuery = (Select) arq2SPIN.createQuery(arqQuery, "http://example.org/schemas/test/query2");
        
        Model newModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        newModel.setNsPrefix("rdf", RDF.getURI());
        newModel.setNsPrefix("ex", "http://example.org/demo#");
        
        Node n = Node.createURI("http://example.org/schemas/test/query2");
        newModel.add(model.listStatements().toList());
        RDFNode r = newModel.getRDFNode(n);
        //System.out.println(r.asResource().hasProperty(RDF.type, SP.Select));
        org.topbraid.spin.model.Query q = SPINFactory.asQuery(r.asResource());
        
        StatementTripleBoundary s = new StatementTripleBoundary(TripleBoundary.stopNowhere);
        RDFNode resource2 = newModel.getRDFNode(n);
        ModelExtract me = new ModelExtract(s);
        Model m2=me.extract(resource2.asResource(), newModel);
        
        Assert.assertNotNull(q);

        System.out.println("Query: " + q);
        
        String rdfTypeString = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String spinSelectString = "http://spinrdf.org/sp#Select";
        
        Property propRdfTypeNative = RDF.type;
        Property propRdfTypeNativeString = model.getProperty(RDF.type.toString());
        Property propRdfTypeString = model.getProperty(rdfTypeString);
        com.hp.hpl.jena.rdf.model.Resource spsel = SP.Select;
        System.out.println(r.asResource().hasProperty(RDF.type, SP.Select));
        System.out.println(r.asResource().hasProperty(propRdfTypeString, model.getResource(spinSelectString)));
        System.out.println(r.asResource().hasProperty(propRdfTypeNative, SP.Select));
        System.out.println(r.asResource().hasProperty(propRdfTypeNative, spsel));
        System.out.println(r.asResource().hasProperty(propRdfTypeNativeString, model.getResource(SP.Select.toString())));
        System.out.println(r.asResource().hasProperty(RDF.type, model.getResource(spinSelectString)));
        System.out.println(r.asResource().hasProperty(model.getProperty(rdfTypeString), SP.Select));

        Assert.assertTrue(r.asResource().hasProperty(RDF.type, SP.Select));
        Assert.assertTrue(r.asResource().hasProperty(propRdfTypeString, model.getResource(spinSelectString)));
        Assert.assertTrue(r.asResource().hasProperty(propRdfTypeNative, SP.Select));
        Assert.assertTrue(r.asResource().hasProperty(propRdfTypeNative, spsel));
        Assert.assertTrue(r.asResource().hasProperty(propRdfTypeNativeString, model.getResource(SP.Select.toString())));
        Assert.assertTrue(r.asResource().hasProperty(RDF.type, model.getResource(spinSelectString)));
        Assert.assertTrue(r.asResource().hasProperty(model.getProperty(rdfTypeString), SP.Select));
    }
    
    private boolean contains(Query nextQuery, String expected)
    {
        return nextQuery.toString(Syntax.syntaxSPARQL_11).contains(expected);
    }
}
