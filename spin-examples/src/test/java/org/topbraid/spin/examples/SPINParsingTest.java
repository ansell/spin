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
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
        SPINModuleRegistry.get().reset();
        SPINModuleRegistry.get().init();
        
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
        SPINModuleRegistry.get().reset();
        SPINModuleRegistry.get().init();
        
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
    
    private boolean contains(Query nextQuery, String expected)
    {
        return nextQuery.toString(Syntax.syntaxSPARQL_11).contains(expected);
    }
}
