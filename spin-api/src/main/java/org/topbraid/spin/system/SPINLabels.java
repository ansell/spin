/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.system;

import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * A singleton that is used to render resources into strings.
 * By default this displays qnames (if possible). 
 * Can be changed, for example, to switch to displaying rdfs:labels
 * instead of qnames etc.
 * 
 * @author Holger Knublauch
 */
public class SPINLabels {
	
	private static SPINLabels singleton = new SPINLabels();
	

	/**
	 * Gets the singleton instance of this class.
	 * @return the singleton
	 */
	public static SPINLabels get() {
		return singleton;
	}
	
	
	/**
	 * Replaces the singleton to a subclass with different behavior.
	 * This is used by TopBraid, which has its own rendering engine. 
	 * @param value  the new engine
	 */
	public static void set(SPINLabels value) {
		SPINLabels.singleton = value;
	}
	
	
	/**
	 * Gets a "human-readable" label for a given Resource.
	 * This checks for any existing rdfs:label, otherwise falls back to
	 * <code>getLabel()</code>.
	 * @param resource
	 * @return the label (never null)
	 */
	public String getCustomizedLabel(Resource resource) {
		String label = JenaUtil.getStringProperty(resource, RDFS.label);
		if(label != null) {
			return label;
		}
		else {
			return getLabel(resource);
		}
	}
	

	/**
	 * Gets the label for a given Resource.
	 * @param resource  the Resource to get the label of
	 * @return the label (never null)
	 */
	public String getLabel(Resource resource) {
		if(resource.isURIResource()) {
			String qname = resource.getModel().qnameFor(resource.getURI());
			if(qname != null) {
				return qname;
			}
			else {
				return "<" + resource.getURI() + ">";
			}
		}
		else {
			return resource.toString();
		}
	}
}
