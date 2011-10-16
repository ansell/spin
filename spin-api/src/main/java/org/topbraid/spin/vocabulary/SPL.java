/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.vocabulary;

import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.ReificationStyle;



/**
 * Vocabulary of the SPIN Standard Modules Library (SPL).
 * 
 * @author Holger Knublauch
 */
public class SPL {

	public final static String BASE_URI = "http://spinrdf.org/spl";
	
	public final static String NS = BASE_URI + "#";
	
	public final static String PREFIX = "spl";
	

    public final static Resource Argument = ResourceFactory.createResource(NS + "Argument");
    
	public final static Resource Attribute = ResourceFactory.createResource(NS + "Attribute");
    
	public final static Resource InferDefaultValue = ResourceFactory.createResource(NS + "InferDefaultValue");
    
	public final static Resource SPINOverview = ResourceFactory.createResource(NS + "SPINOverview");

	
	public final static Resource objectCount = ResourceFactory.createResource(NS + "objectCount");

	
	public final static Property defaultValue = ResourceFactory.createProperty(NS + "defaultValue");
	
	public final static Property hasValue = ResourceFactory.createProperty(NS + "hasValue");
	
	public final static Property maxCount = ResourceFactory.createProperty(NS + "maxCount");
	
	public final static Property minCount = ResourceFactory.createProperty(NS + "minCount");
    
    public final static Property optional = ResourceFactory.createProperty(NS + "optional");
    
	public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");
	
	public final static Property valueType = ResourceFactory.createProperty(NS + "valueType");
	
	static {
		// Force initialization
		SP.getURI();
	}
	
	
	private static Model model;
	

	/**
	 * Gets a Model with the content of the SPL namespace, from a file
	 * that is bundled with this API.
	 * @return the namespace Model
	 */
	public static Model getModel() {
		if(model == null) {
			model = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
			InputStream is = SPL.class.getResourceAsStream("/etc/spl.spin.rdf");
			if(is == null) {
				model.read(SPL.BASE_URI);
			}
			else {
				model.read(is, "http://dummy");
			}
		}
		return model;
	}
}
