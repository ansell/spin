/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import java.util.ArrayList;
import java.util.List;

import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;


/**
 * An ARQ function that delegates its functionality into a user-defined
 * SPIN function. 
 * 
 * @author Holger Knublauch
 */
public class SPINARQFunction implements com.hp.hpl.jena.sparql.function.Function, SPINFunctionFactory {
	
	private com.hp.hpl.jena.query.Query arqQuery;
	
	private List<String> argNames = new ArrayList<String>();
	
	private List<Node> argNodes = new ArrayList<Node>();
	
	private String queryString;
	

	/**
	 * Constructs a new SPINARQFunction based on a given SPIN Function.
	 * The spinFunction model be associated with the Model containing
	 * the triples of its definition.
	 * @param spinFunction  the SPIN function
	 */
	public SPINARQFunction(Function spinFunction) {
		try {
			Query spinQuery = (Query) spinFunction.getBody();
			queryString = spinQuery.toString();
			arqQuery = ARQFactory.get().createQuery(spinFunction.getModel(), queryString);
			
			for(Argument arg : spinFunction.getArguments(true)) {
				String varName = arg.getVarName();
				if(varName == null) {
					throw new IllegalStateException("Argument " + arg + " of " + spinFunction + " does not have a valid predicate");
				}
				argNames.add(varName);
				argNodes.add(arg.getPredicate().asNode());
			}
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Function " + spinFunction.getURI() + " does not define a valid body", ex);
		}
	}
	

	public void build(String uri, ExprList args) {
	}

	
	public com.hp.hpl.jena.sparql.function.Function create(String uri) {
		return this;
	}

	
	public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
		QuerySolutionMap bindings = new QuerySolutionMap();
		Node t = binding.get(Var.alloc(SPIN.THIS_VAR_NAME));
		if(t != null) {
			bindings.add(SPIN.THIS_VAR_NAME, model.asRDFNode(t));
		}
		for(int i = 0; i < args.size(); i++) {
			Expr expr = args.get(i);
			if(!expr.isVariable() || binding.contains(expr.asVar())) {
	        	NodeValue x = expr.eval(binding, env);
	        	if(x != null) {
	        		String argName;
	        		if(i < argNames.size()) {
	        			argName = argNames.get(i);
	        		}
	        		else {
	        			argName = "arg" + (i + 1);
	        		}
	        		bindings.add(argName, model.asRDFNode(x.asNode()));
	        	}
			}
		}
		return executeBody(model, bindings);
	}


	public NodeValue executeBody(Model model, QuerySolution bindings) {
		QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, model);
		qexec.setInitialBinding(bindings);
		if(arqQuery.isAskType()) {
			boolean result = qexec.execAsk();
			qexec.close();
			return NodeValue.makeBoolean(result);
		}
		else if(arqQuery.isSelectType()) {
			ResultSet rs = qexec.execSelect();
			if(rs.hasNext()) {
				QuerySolution s = rs.nextSolution();
				List<String> resultVars = rs.getResultVars();
				String varName = resultVars.get(0);
				RDFNode resultNode = s.get(varName);
				qexec.close();
				if(resultNode != null) {
					return NodeValue.makeNode(resultNode.asNode());
				}
			}
			else {
				qexec.close();
			}
			throw new ExprEvalException("Empty result set for SPIN function " + queryString);
		}
		else {
			throw new ExprEvalException("Body must be ASK or SELECT query");
		}
	}
	
	
	/**
	 * Gets the names of the declared arguments, in order from left to right.
	 * @return the arguments
	 */
	public String[] getArgNames() {
		return argNames.toArray(new String[0]);
	}
	
	
	public Node[] getArgPropertyNodes() {
		return argNodes.toArray(new Node[0]);
	}
	

	/**
	 * Gets the Jena Query object for execution.
	 * @return the Jena Query
	 */
	public com.hp.hpl.jena.query.Query getBodyQuery() {
		return arqQuery;
	}
}
