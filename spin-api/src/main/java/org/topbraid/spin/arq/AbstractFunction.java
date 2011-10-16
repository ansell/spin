/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionEnv;


/**
 * Base implementation of Function comparable to Jena's FunctionBase.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractFunction implements Function {

	public void build(String uri, ExprList args) {
	}

	
	public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		Node[] nodes = new Node[args.size()];
		for(int i = 0; i < args.size(); i++) {
            Expr e = args.get(i);
            try {
            	if(e != null && (!e.isVariable() || (e.isVariable() && binding.get(e.asVar()) != null)) ) {
	            	NodeValue x = e.eval(binding, env);
	            	if (x != null) {
						nodes[i] = x.asNode();
					} 
            	}
            }
            catch(ExprEvalException ex) {
            	throw ex;
            }
            catch(Exception ex) {
            	throw new IllegalArgumentException("Error during function evaluation", ex);
            }
        }
        
        return exec(nodes, env);
	}
	
	
	protected abstract NodeValue exec(Node[] nodes, FunctionEnv env);
}
