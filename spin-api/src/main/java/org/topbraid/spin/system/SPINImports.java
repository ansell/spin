/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.system;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * A singleton managing spin:imports.
 * 
 * Subclasses can be installed that implement different loaders or
 * otherwise change the default behavior.
 * 
 * @author Holger Knublauch
 */
public class SPINImports {
	
	private Set<String> registeredURIs = new HashSet<String>();

	public static SPINImports singleton = new SPINImports();
	
	
	/**
	 * Gets the singleton instance of this class.
	 * @return the singleton
	 */
	public static SPINImports get() {
		return singleton;
	}
	
	
	/**
	 * Attempts to load a graph with a given URI.
	 * 
	 * Uses the default Jena OntModelSpec to load imports.
	 * 
	 * To use a custom OntModelSpec to load imports, use:
	 * 
	 *    getImportedGraph(String, OntModelSpec)
	 * 
	 * @param uri  the base URI of the graph to load
	 * @return the Graph or null to ignore this
	 */
	protected Graph getImportedGraph(String uri) throws IOException {
	    return getImportedGraph(uri, OntModelSpec.OWL_MEM);
	}
	
	/**
	 * Attempts to load a graph with the given URI using the given OntModelSpec for import mapping
	 * 
	 * FIXME: The document manager in the given OntModelSpec is used to cache the results, so it may be a source of memory leaks!
	 * 
	 * @param uri   the base URI of the graph to load
	 * @param nextOntModelSpec The OntModelSpec to use when loading the imports
	 * @return  the Graph or null to ignore this
	 * @throws IOException
	 */
    protected Graph getImportedGraph(String uri, OntModelSpec nextOntModelSpec) throws IOException {
	    
        // Remove use of singleton OntDocumentManager in favour of the more extensible (and non-Singleton!!) OntModelSpec method
        Model model = nextOntModelSpec.getDocumentManager().getModel(uri);
		if(model == null) {
			Model baseModel = JenaUtil.createDefaultModel();
			baseModel.read(uri);
			model = ModelFactory.createOntologyModel(nextOntModelSpec, baseModel);
			nextOntModelSpec.getDocumentManager().addModel(uri, model);
		}
		return model.getGraph();
	}
	
	
	/**
	 * Checks if spin:imports have been declared and adds them to a union model.
	 * Will also register any SPIN modules defined in those imports that haven't
	 * been loaded before.
	 * @param model  the base Model to operate on
	 * @return either model or the union of model and its spin:imports
	 */
	public Model getImportsModel(Model model) throws IOException {
        return getImportsModel(model, null, OntModelSpec.OWL_MEM);
	}
	
	/**
     * Checks if spin:imports have been declared and adds them to a union model.
     * Will also register any SPIN modules defined in those imports that haven't
     * been loaded before.
	 * 
	 * @param model  the base Model to operate on
	 * @param source  the source object to use for any discovered functions, to enable fetching of functions based on this object
	 * @param nextOntModelSpec  the OntModelSpec to use when loading imports, as this defines the LocationMapper etc to use, through OntDocumentManager and FileManager
     * @return either model or the union of model and its spin:imports
	 * @throws IOException
	 */
    public Model getImportsModel(Model model, Object source, OntModelSpec nextOntModelSpec) throws IOException {
	    Set<String> uris = new HashSet<String>();
		StmtIterator it = model.listStatements(null, SPIN.imports, (RDFNode)null);
		while(it.hasNext()) {
			Statement s = it.nextStatement();
			if(s.getObject().isURIResource()) {
				uris.add(s.getResource().getURI());
			}
		}
		if(uris.isEmpty()) {
			return model;
		}
		else {
			Graph baseGraph = model.getGraph();
			
			MultiUnion union = new MultiUnion();
			union.addGraph(baseGraph);
			union.setBaseGraph(baseGraph);
			
			boolean needsRegistration = false;
			for(String uri : uris) {
				Graph graph = getImportedGraph(uri, nextOntModelSpec);
				if(graph != null) {
					union.addGraph(graph);
					if(!registeredURIs.contains(uri)) {
						registeredURIs.add(uri);
						needsRegistration = true;
					}
				}
			}
			
			// Ensure that SP, SPIN and SPL are present
			ensureImported(union, SP.BASE_URI, SP.getModel());
			ensureImported(union, SPL.BASE_URI, SPL.getModel());
			ensureImported(union, SPIN.BASE_URI, SPIN.getModel());
			
			Model unionModel = ModelFactory.createModelForGraph(union);
			if(needsRegistration) {
				SPINModuleRegistry.get().registerAll(unionModel, source);
			}
			return unionModel;
		}
	}
	
	
	private void ensureImported(MultiUnion union, String baseURI, Model model) {
		if(!union.contains(Triple.create(Node.createURI(baseURI), RDF.type.asNode(), OWL.Ontology.asNode()))) {
			union.addGraph(model.getGraph());
		}
	}
	

	/**
	 * Installs a different SPINImports singleton.
	 * @param value  the new singleton
	 */
	public static void set(SPINImports value) {
		SPINImports.singleton = value;
	}
}
