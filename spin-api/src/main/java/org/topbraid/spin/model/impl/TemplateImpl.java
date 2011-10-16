/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Template;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;


public class TemplateImpl extends ModuleImpl implements Template {

	public TemplateImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}

	
	public String getLabelTemplate() {
		return getString(SPIN.labelTemplate);
	}
}
