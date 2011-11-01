package org.topbraid.spin.model.update.impl;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.DeleteWhere;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;


public class DeleteWhereImpl extends UpdateImpl implements DeleteWhere {

	public DeleteWhereImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public void print(PrintContext p, SPINModuleRegistry registry) {
		p.printKeyword("DELETE");
		p.print(" ");
		printWhere(p, registry);
	}
}
