/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import org.topbraid.spin.model.Command;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;


/**
 * Wraps a (pre-compiled) Jena Query or UpdateRequest with its source SPIN object and
 * a human-readable string representation. 
 * 
 * Also needed to work around the issue of Query.equals/hashCode: Otherwise
 * multiple distinct template calls will be merged into one in HashMaps.
 * 
 * @author Holger Knublauch
 */
public abstract class CommandWrapper {
	
	private boolean thisUnbound;
	
	private String label;
	
	private Resource source;
	
	private Statement statement;
	
	private String text;
	
	
	public CommandWrapper(Resource source, String text, String label, Statement statement, boolean thisUnbound) {
		this.label = label;
		this.statement = statement;
		this.source = source;
		this.text = text;
		this.thisUnbound = thisUnbound;
	}
	
	
	public String getLabel() {
		return label;
	}
	
	
	public abstract Command getSPINCommand();
	
	
	public Statement getStatement() {
		return statement;
	}
	
	
	/**
	 * Gets the SPIN Query or template call that has created this QueryWrapper. 
	 * @return the source
	 */
	public Resource getSource() {
		return source;
	}
	
	
	public String getText() {
		return text;
	}
	
	
	public boolean isThisUnbound() {
		return thisUnbound;
	}
}
