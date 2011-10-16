package org.topbraid.spin.inference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.GraphStore;


/**
 * A GraphStore that wraps a given Dataset, so that each updateable
 * graph is wrapped with a ControlledUpdateGraph instead of the default.
 * 
 * @author Holger Knublauch
 */
class ControlledUpdateGraphStore implements GraphStore {
	
	private Map<Graph,ControlledUpdateGraph> cugs = new HashMap<Graph,ControlledUpdateGraph>();
	
	private Dataset dataset;
	
	
	ControlledUpdateGraphStore(Dataset dataset, Iterable<Graph> controlledGraphs) {
		this.dataset = dataset;
		for(Graph graph : controlledGraphs) {
			ControlledUpdateGraph cug = new ControlledUpdateGraph(graph);
			cugs.put(graph, cug);
		}
	}
	
	
	private Graph getControlledUpdateGraph(Graph graph) {
		Graph cug = cugs.get(graph);
		if(cug != null) {
			return cug;
		}
		else {
			return graph;
		}
	}
	
	
	public Iterable<ControlledUpdateGraph> getControlledUpdateGraphs() {
		return cugs.values();
	}


	@Override
	public Graph getDefaultGraph() {
		Model defaultModel = dataset.getDefaultModel();
		if(defaultModel != null) {
			return getControlledUpdateGraph(defaultModel.getGraph());
		}
		else {
			return null;
		}
	}


	@Override
	public Graph getGraph(Node graphNode) {
		Model model = dataset.getNamedModel(graphNode.getURI());
		if(model != null) {
			return getControlledUpdateGraph(model.getGraph());
		}
		else {
			return null;
		}
	}


	@Override
	public boolean containsGraph(Node graphNode) {
		return dataset.containsNamedModel(graphNode.getURI());
	}


	@Override
	public void setDefaultGraph(Graph g) {
	}


	@Override
	public void addGraph(Node graphName, Graph graph) {
	}


	@Override
	public void removeGraph(Node graphName) {
	}


	@Override
	public Iterator<Node> listGraphNodes() {
		List<Node> results = new LinkedList<Node>();
		Iterator<String> it = dataset.listNames();
		while(it.hasNext()) {
			results.add(Node.createURI(it.next()));
		}
		return results.iterator();
	}


	@Override
	public void add(Quad quad) {
	}


	@Override
	public void delete(Quad quad) {
	}


	@Override
	public void deleteAny(Node g, Node s, Node p, Node o) {
	}


	@Override
	public Iterator<Quad> find() {
		return null;
	}


	@Override
	public Iterator<Quad> find(Quad quad) {
		return null;
	}


	@Override
	public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		return null;
	}


	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		return null;
	}


	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
		return false;
	}


	@Override
	public boolean contains(Quad quad) {
		return false;
	}


	@Override
	public boolean isEmpty() {
		return false;
	}


	@Override
	public Lock getLock() {
		return null;
	}


	@Override
	public Context getContext() {
		return null;
	}


	@Override
	public long size() {
		return 0;
	}


	@Override
	public void close() {
	}


	@Override
	public Dataset toDataset() {
		return null;
	}


	@Override
	public void startRequest() {
	}


	@Override
	public void finishRequest() {
	}
}
