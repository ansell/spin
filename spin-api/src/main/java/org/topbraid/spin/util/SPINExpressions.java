package org.topbraid.spin.util;

import java.util.Set;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Aggregation;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.impl.AbstractSPINResourceImpl;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.StringPrintContext;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.util.FmtUtils;


/**
 * Static utilities on SPIN Expressions.
 * 
 * @author Holger Knublauch
 */
public class SPINExpressions {
	
	public final static PrefixMapping emptyPrefixMapping = new PrefixMappingImpl();
	
	
	public static String checkExpression(String str, Model model) {
		String queryString = "ASK WHERE { LET (?xqoe := (" + str + ")) }";
		try {
			ARQFactory.get().createQuery(model, queryString);
			return null;
		}
		catch(QueryParseException ex) {
			String s = ex.getMessage();
			int startIndex = s.indexOf("at line ");
			int endIndex = s.indexOf('.', startIndex);
			StringBuffer sb = new StringBuffer();
			sb.append(s.substring(0, startIndex));
			sb.append("at column ");
			sb.append(ex.getColumn() - 27);
			sb.append(s.substring(endIndex));
			return sb.toString();
		}
	}
	
	
	/**
	 * Evaluates a given SPIN expression.
	 * Prior to calling this, the caller must make sure that the expression has the
	 * most specific Java type, e.g. using SPINFactory.asExpression().
	 * @param expression  the expression (must be cast into the best possible type)
	 * @param queryModel  the Model to query
	 * @param bindings  the initial bindings
	 * @return the result RDFNode or null
	 */
	public static RDFNode evaluate(Resource expression, Model queryModel, QuerySolution bindings) {
		if(expression instanceof Variable) {
			// Optimized case if the expression is just a variable
			String varName = ((Variable)expression).getName();
			return bindings.get(varName);
		}
		else if(expression.isURIResource()) {
			return expression;
		}
		else {
			Query arq = ARQFactory.get().createExpressionQuery(expression);
			QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, queryModel);
			qexec.setInitialBinding(bindings);
			ResultSet rs = qexec.execSelect();
			if(rs.hasNext()) {
				String varName = rs.getResultVars().get(0);
				RDFNode result = rs.next().get(varName);
				qexec.close();
				return result;
			}
			else {
				return null;
			}
		}
	}
	
	
	public static String getExpressionString(RDFNode expression) {
		return getExpressionString(expression, true);
	}
	
	
	public static String getExpressionString(RDFNode expression, boolean usePrefixes) {
		if(usePrefixes) {
			StringPrintContext p = new StringPrintContext();
			p.setUsePrefixes(usePrefixes);
			SPINExpressions.printExpressionString(p, expression, false, false, expression.getModel().getGraph().getPrefixMapping());
			return p.getString();
		}
		else {
			return ARQFactory.get().createExpressionString(expression);
		}
	}
	
    /**
     * Checks whether a given RDFNode is an expression.
     * In order to be regarded as expression it must be a well-formed
     * function call, aggregation or variable.
     * @param node  the RDFNode
     * @return true if node is an expression
     */
    public static boolean isExpression(RDFNode node) {
        return isExpression(node, null);
    }
    
    /**
	 * Checks whether a given RDFNode is an expression.
	 * In order to be regarded as expression it must be a well-formed
	 * function call, aggregation or variable.
	 * @param node  the RDFNode
	 * @param validSources A set of objects that correspond to objects given in SPINModuleRegistry.registerAll calls or null to include all sources
	 * @return true if node is an expression
	 */
	public static boolean isExpression(RDFNode node, Set<Object> validSources) {
		if(node instanceof Resource && SP.exists(((Resource)node).getModel())) {
			RDFNode expr = SPINFactory.asExpression(node);
			if(expr instanceof Variable) {
				return true;
			}
			else if(!node.isAnon()) {
				return false;
			}
			if(expr instanceof FunctionCall) {
				Resource function = ((FunctionCall)expr).getFunction();
				if(function.isURIResource()) {
					if(SPINModuleRegistry.get().getFunction(function.getURI(), ((Resource)node).getModel(), validSources) != null) {
						return true;
					}
					if(FunctionRegistry.get().isRegistered(function.getURI())) {
						return true;
					}
				}
			}
			else {
				return expr instanceof Aggregation;
			}
		}
		return false;
	}


	public static Expr parseARQExpression(String str, Model model) {
		String queryString = "ASK WHERE { LET (?xqoe := (" + str + ")) }";
		Query arq = ARQFactory.get().createQuery(model, queryString);
		ElementGroup group = (ElementGroup) arq.getQueryPattern();
		ElementAssign assign = (ElementAssign) group.getElements().get(0);
		Expr expr = assign.getExpr();
		return expr;
	}
	
	
	public static RDFNode parseExpression(String str, Model model) {
		Expr expr = parseARQExpression(str, model);
		return parseExpression(expr, model);
	}
	
	
	public static RDFNode parseExpression(Expr expr, Model model) {
		ARQ2SPIN a2s = new ARQ2SPIN(model);
		return a2s.createExpression(expr);
	}
	

	public static void printExpressionString(PrintContext p, RDFNode node, boolean nested, boolean force, PrefixMapping prefixMapping) {
		if(node instanceof Resource && SPINFactory.asVariable(node) == null) {
			Resource resource = (Resource) node;
			
			Aggregation aggr = SPINFactory.asAggregation(resource);
			if(aggr != null) {
				PrintContext pc = p.clone();
				pc.setNested(nested);
				aggr.print(pc);
				return;
			}
			
			FunctionCall call = SPINFactory.asFunctionCall(resource);
			if(call != null) {
				PrintContext pc = p.clone();
				pc.setNested(nested);
				call.print(pc);
				return;
			}
		}
		if(force) {
			p.print("(");
		}
		if(node instanceof Resource) {
			AbstractSPINResourceImpl.printVarOrResource(p, (Resource)node);
		}
		else {
			PrefixMapping pm = p.getUsePrefixes() ? prefixMapping : emptyPrefixMapping;
			String str = FmtUtils.stringForNode(node.asNode(), pm);
			p.print(str);
		}
		if(force) {
			p.print(")");
		}
	}
}
