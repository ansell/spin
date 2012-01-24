/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.system.MagicPropertyPolicy;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory;
import com.hp.hpl.jena.sparql.util.IterLib;

/**
 * An ARQ PropertyFunction based on a spin:MagicProperty.
 * For convenience, also implements PropertyFunctionFactory.
 *
 * @author Holger Knublauch
 */
public class SPINARQPFunction extends PropertyFunctionBase implements PropertyFunctionFactory {
	
	private com.hp.hpl.jena.query.Query arqQuery;

	private String queryString;
	
	private List<String> objectVarNames = new ArrayList<String>();

	
	public SPINARQPFunction(Function functionCls) {
		try {
			Select spinQuery = (Select) functionCls.getBody();
			List<Resource> resultVariables = spinQuery.getResultVariables();
			if(resultVariables == null) {
				throw new IllegalArgumentException("SELECT * not supported in magic properties");
			}
			for(Resource var : resultVariables) {
				if(var instanceof Variable) {
					objectVarNames.add(((Variable)var).getName());
				}
				else {
					throw new IllegalArgumentException("SELECT with expressions not supported in magic properties");
				}
			}
			queryString = spinQuery.toString();
			int selectStart = queryString.indexOf("SELECT ");
			int eol = queryString.indexOf('\n', selectStart);
			
			StringBuffer sb = new StringBuffer(queryString.substring(0, eol));
			for(Argument arg : functionCls.getArguments(true)) {
				sb.append(" ?");
				sb.append(arg.getVarName());
			}
			sb.append(queryString.substring(eol));
			
			arqQuery = ARQFactory.get().createQuery(functionCls.getModel(), sb.toString());
		}
		catch(Throwable t) {
			t.printStackTrace();
			throw new IllegalArgumentException("Function definition does not contain a valid body", t);
		}
	}

	
	public PropertyFunction create(String arg0) {
		return this;
	}

	
	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate,
			PropFuncArg argObject, ExecutionContext context) {

		argObject = Substitute.substitute(argObject, binding);
		argSubject = Substitute.substitute(argSubject, binding);
		
		ExprList subjectExprList = argSubject.asExprList(argSubject);
		ExprList objectExprList = argObject.asExprList(argObject);
		
		QueryIterConcat existingValues = null;
		MagicPropertyPolicy.Policy policy = MagicPropertyPolicy.Policy.QUERY_RESULTS_ONLY;
		// Handle cases with one argument on both sides (S, P, O)
		if(objectExprList.size() == 1 && subjectExprList.size() == 1) {
			Expr subject = subjectExprList.get(0);
			Expr object = objectExprList.get(0);
			if(subject.isVariable() || object.isVariable()) {
				
				Node matchSubject = null;
				if(subject.isConstant()) {
					Node n = subject.getConstant().asNode();
					if(n.isURI() || n.isBlank()) {
						matchSubject = n;
					}
				}
				
				Node matchObject = null;
				if(object.isConstant()) {
					matchObject = object.getConstant().asNode();
				}
				
				Graph queryGraph = context.getActiveGraph();
				policy = MagicPropertyPolicy.get().getPolicy(predicate.getURI(), queryGraph, matchSubject, matchObject);

				if(policy != MagicPropertyPolicy.Policy.QUERY_RESULTS_ONLY) {
					Iterator<Triple> it = queryGraph.find(matchSubject, predicate, matchObject);
					while(it.hasNext()) {
						Triple triple = it.next();
						BindingMap map = new BindingHashMap(binding);
						if(subject.isVariable()) {
							map.add(subject.asVar(), triple.getSubject());
						}
						if(object.isVariable()) {
							map.add(object.asVar(), triple.getObject());
						}
						if(existingValues == null) {
							existingValues = new QueryIterConcat(context);
						}
						QueryIterator nested = IterLib.result(map, context);
						existingValues.add(nested);
					}
				}
			}
		}
		
		if(policy != MagicPropertyPolicy.Policy.TRIPLES_ONLY) {
			
			Model model = ModelFactory.createModelForGraph(context.getActiveGraph());
			Node t = binding.get(Var.alloc(SPIN.THIS_VAR_NAME));
			QuerySolutionMap bindings = new QuerySolutionMap();
			if(t != null) {
				bindings.add(SPIN.THIS_VAR_NAME, model.asRDFNode(t));
			}
	
			// Map object expressions to original objectVarNames
			Map<String,Var> vars = new HashMap<String,Var>();
			for(int i = 0; i < objectVarNames.size() && i < objectExprList.size(); i++) {
				Expr expr = objectExprList.get(i);
				String objectVarName = objectVarNames.get(i);
				if(expr.isVariable() && !binding.contains(expr.asVar())) {
					Var var = expr.asVar();
					vars.put(objectVarName, var);
				}
				else {
		        	NodeValue x = expr.eval(binding, context);
		        	if(x != null) {
		        		bindings.add(objectVarName, model.asRDFNode(x.asNode()));
		        	}
				}
			}
			
			// Map subject expressions to arg1 etc
			for(int i = 0; i < subjectExprList.size(); i++) {
				String subjectVarName = "arg" + (i + 1);
				Expr expr = subjectExprList.get(i);
				if(expr.isVariable() && !binding.contains(expr.asVar())) {
					Var var = expr.asVar();
					vars.put(subjectVarName, var);
				}
				else {
		        	NodeValue x = expr.eval(binding, context);
		        	if(x != null) {
		        		bindings.add(subjectVarName, model.asRDFNode(x.asNode()));
		        	}
				}
			}
			
			// Execute SELECT query and wrap it with a custom iterator
			QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, model);
			qexec.setInitialBinding(bindings);
			ResultSet rs = qexec.execSelect();
			QueryIterator it = new PFunctionQueryIterator(rs, qexec, vars, binding);
			if(existingValues != null) {
				existingValues.add(it);
				return existingValues;
			}
			else {
				return it;
			}
		}
		else if(existingValues != null) {
			return existingValues;
		}
		else {
			return IterLib.result(binding, context);
		}
	}
}
