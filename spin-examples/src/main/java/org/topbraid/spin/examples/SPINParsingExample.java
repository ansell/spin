/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.examples;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.query.Query;
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
public class SPINParsingExample {

	public static void main(String[] args) {
		
		// Register system functions (such as sp:gt (>))
		SPINModuleRegistry.get().init();
		
		// Create an empty OntModel importing SP
		Model model = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("ex", "http://example.org/demo#");
		
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
		
		System.out.println("SPIN query in Turtle:");
		model.write(System.out, FileUtils.langTurtle);
		
		System.out.println("-----");
		String str = spinQuery.toString();
		System.out.println("SPIN query:\n" + str);
		
		// Now turn it back into a Jena Query
		Query parsedBack = ARQFactory.get().createQuery(spinQuery);
		System.out.println("Jena query:\n" + parsedBack);
	}
}
