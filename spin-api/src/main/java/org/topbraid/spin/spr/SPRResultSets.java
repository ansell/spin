package org.topbraid.spin.spr;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.vocabulary.SPR;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Static utilities on SPR tables.
 * 
 * @author Holger Knublauch
 */
public class SPRResultSets {
	
	private static Query cellQuery = QueryFactory.create(
			"SELECT ?result\n" +
			"WHERE {\n" +
			"    LET (?result := <" + SPR.cell.getURI() + ">(?table, ?row, ?col)) .\n" +
			"}", Syntax.syntaxARQ);
	
	private static Query colCountQuery = QueryFactory.create(
			"SELECT ?result\n" +
			"WHERE {\n" +
			"    LET (?result := <" + SPR.colCount.getURI() + ">(?table)) .\n" +
			"}", Syntax.syntaxARQ);
	
	private static Query colNameQuery = QueryFactory.create(
			"SELECT ?result\n" +
			"WHERE {\n" +
			"    LET (?result := <" + SPR.colName.getURI() + ">(?table, ?col)) .\n" +
			"}", Syntax.syntaxARQ);
	
	private static Query rowCountQuery = QueryFactory.create(
			"SELECT ?result\n" +
			"WHERE {\n" +
			"    LET (?result := <" + SPR.rowCount.getURI() + ">(?table)) .\n" +
			"}", Syntax.syntaxARQ);
	
	
	public static RDFNode getCell(Resource table, int row, int col) {
		Model model = table.getModel();
		QueryExecution qexec = ARQFactory.get().createQueryExecution(cellQuery, model);
		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add("table", table);
		bindings.add("row", JenaDatatypes.createInteger(row));
		bindings.add("col", JenaDatatypes.createInteger(col));
		qexec.setInitialBinding(bindings);
		ResultSet rs = qexec.execSelect();
		if(rs.hasNext()) {
			RDFNode result = rs.next().get("result");
			qexec.close();
			return result;
		}
		else {
			return null;
		}
	}

	
	public static int getColCount(Resource table) {
		return getIntFromFunction(table, colCountQuery);
	}
	
	
	public static String getColName(Resource table, int col) {
		Model model = table.getModel();
		QueryExecution qexec = ARQFactory.get().createQueryExecution(colNameQuery, model);
		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add("table", table);
		bindings.add("col", JenaDatatypes.createInteger(col));
		qexec.setInitialBinding(bindings);
		ResultSet rs = qexec.execSelect();
		if(rs.hasNext()) {
			RDFNode result = rs.next().get("result");
			qexec.close();
			if(result.isLiteral()) {
				return ((Literal)result).getString();
			}
		}
		return null;
	}
	

	public static List<String> getColNames(Resource table) {
		List<String> results = new LinkedList<String>();
		int colCount = getColCount(table);
		for(int i = 0; i < colCount; i++) {
			results.add(getColName(table, i));
		}
		return results;
	}
	
	
	private static int getIntFromFunction(Resource table, Query query) {
		Model model = table.getModel();
		QueryExecution qexec = ARQFactory.get().createQueryExecution(query, model);
		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add("table", table);
		qexec.setInitialBinding(bindings);
		ResultSet rs = qexec.execSelect();
		if(rs.hasNext()) {
			RDFNode result = rs.next().get("result");
			qexec.close();
			if(result.isLiteral()) {
				return ((Literal)result).getInt();
			}
		}
		return 0;
	}

	
	public static int getRowCount(Resource table) {
		return getIntFromFunction(table, rowCountQuery);
	}
}
