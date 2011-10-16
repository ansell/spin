/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.util.SPINExpressions;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.function.FunctionFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * The SPARQL function spin:eval.
 * 
 * The first argument is a SPIN expression, e.g. a function call or variable.
 * All other arguments must come in pairs, alternating between an argument property
 * and its value, e.g.
 * 
 *  	spin:eval(ex:myInstance, sp:arg3, "value")
 *  
 * The expression will be evaluated with all bindings from the property-value pairs above.
 * 
 * @author Holger Knublauch
 */
public class EvalFunction extends AbstractFunction implements FunctionFactory {

	@Override
	public Function create(String uri) {
		return this;
	}

	
	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		
		Model baseModel = ModelFactory.createModelForGraph(env.getActiveGraph());
		Node exprNode = nodes[0];
		if(exprNode == null) {
			throw new ExprEvalException("No expression specified");
		}
		else if(exprNode.isLiteral()) {
			return NodeValue.makeNode(exprNode);
		}
		else {
			Model model = baseModel;
			if(!model.contains(SPIN._arg1, RDF.type, SP.Variable)) {
				MultiUnion multiUnion = new MultiUnion(new Graph[] {
						env.getActiveGraph(),
						SPIN.getModel().getGraph()
				});
				model = ModelFactory.createModelForGraph(multiUnion);
			}
			Resource exprRDFNode = (Resource) model.asRDFNode(exprNode);
			QuerySolutionMap bindings = getBindings(nodes, model);
			org.topbraid.spin.model.Query spinQuery = SPINFactory.asQuery((Resource)exprRDFNode);
			if(spinQuery instanceof Select || spinQuery instanceof Ask) {
				Query query = ARQFactory.get().createQuery((org.topbraid.spin.model.Query)spinQuery);
				QueryExecution qexec = ARQFactory.get().createQueryExecution(query, model, bindings);
				if(query.isAskType()) {
					boolean result = qexec.execAsk();
					return NodeValue.makeBoolean(result);
				}
				else {
					ResultSet rs = qexec.execSelect();
					String var = rs.getResultVars().get(0);
					if(rs.hasNext()) {
						RDFNode r = rs.next().get(var);
						qexec.close();
						if(r != null) {
							return NodeValue.makeNode(r.asNode());
						}
					}
				}
			}
			else {
				RDFNode expr = SPINFactory.asExpression(exprRDFNode);
				RDFNode result = SPINExpressions.evaluate((Resource) expr, model, bindings);
				if(result != null) {
					return NodeValue.makeNode(result.asNode());
				}
			}
			throw new ExprEvalException("Expression has no result");
		}
	}


	private QuerySolutionMap getBindings(Node[] nodes, Model model) {
		QuerySolutionMap bindings = new QuerySolutionMap();
		for(int i = 1; i < nodes.length - 1; i += 2) {
			Node property = nodes[i];
			Node value = nodes[i + 1];
			if(value != null) {
				bindings.add(property.getLocalName(), model.asRDFNode(value));
			}
		}
		return bindings;
	}
}
