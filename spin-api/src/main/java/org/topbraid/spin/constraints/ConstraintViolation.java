/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.constraints;

import java.util.Collection;

import org.topbraid.spin.model.TemplateCall;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * An object representing a failure of a SPIN constraint.
 * 
 * @author Holger Knublauch
 */
public class ConstraintViolation {
	
	private Collection<TemplateCall> fixes;
	
	private String message;
	
	private Collection<SimplePropertyPath> paths;
	
	private Resource root;
	
	private Resource source;
	
	
	/**
	 * Constructs a new ConstraintViolation.
	 * @param root  the root resource of the violation
	 * @param paths  the paths (may be empty)
	 * @param fixes  potential fixes for the violations (may be empty)
	 * @param message  the message explaining the error
	 * @param source  the SPIN Query or template call that has caused this violation
	 *                (may be null)
	 */
	public ConstraintViolation(Resource root, 
				Collection<SimplePropertyPath> paths,
				Collection<TemplateCall> fixes,
				String message,
				Resource source) {
		this.root = root;
		this.paths = paths;
		this.fixes = fixes;
		this.message = message;
		this.source = source;
	}
	
	
	public Collection<TemplateCall> getFixes() {
		return fixes;
	}
	
	
	public String getMessage() {
		return message;
	}
	
	
	public Collection<SimplePropertyPath> getPaths() {
		return paths;
	}
	

	public Resource getRoot() {
		return root;
	}
	
	
	/**
	 * Gets the SPIN Query or template call that has caused this violation.
	 * @return the source (code should be robust against null values)
	 */
	public Resource getSource() {
		return source;
	}
}
