package org.topbraid.spin.model.update.impl;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.Clear;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;


public class ClearImpl extends UpdateImpl implements Clear {

	public ClearImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public void print(PrintContext p, SPINModuleRegistry registry) {
		p.printKeyword("CLEAR");
		p.print(" ");
		printSilent(p);
		printGraphDefaultNamedOrAll(p);
	}
}
