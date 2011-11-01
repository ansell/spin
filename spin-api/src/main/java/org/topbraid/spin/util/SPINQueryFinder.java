/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.Command;
import org.topbraid.spin.model.CommandWithWhere;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.model.update.Update;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.update.UpdateRequest;


/**
 * Can be used to search for all queries associated with a class, e.g. via spin:rule.
 *
 * @author Holger Knublauch
 */
public class SPINQueryFinder {
	

	public static void add(Map<Resource, List<CommandWrapper>> class2Query, Statement s,
			Model model,
			boolean withClass,
			Map<CommandWrapper, Map<String, RDFNode>> initialTemplateBindings,
			boolean allowAsk, SPINModuleRegistry registry) {
		if(s.getObject().isResource()) {
			String spinQueryText = null;
			String label = null;
			org.topbraid.spin.model.Command spinCommand = null;
			Template template = null;
			TemplateCall templateCall = SPINFactory.asTemplateCall(s.getResource(), registry);
			if(templateCall != null) {
				template = templateCall.getTemplate(registry);
				if(template != null) {
					Command body = template.getBody();
					if(body instanceof Construct || (allowAsk && body instanceof Ask)) {
						spinCommand = (org.topbraid.spin.model.Query) body;
					}
					else if(body instanceof Update) {
						spinCommand = (org.topbraid.spin.model.update.Update) body;
					}
				}
				spinQueryText = SPINLabels.get().getLabel(templateCall);
				label = spinQueryText;
			}
			else {
				spinCommand = SPINFactory.asCommand(s.getResource());
				if(spinCommand != null) {
					label = spinCommand.getComment();
				}
			}
			
			if(spinCommand != null) {
				String queryString = ARQFactory.get().createCommandString(spinCommand, registry);
				boolean thisUnbound = spinCommand.hasProperty(SPIN.thisUnbound, JenaDatatypes.TRUE);
				if(spinQueryText == null) {
					spinQueryText = queryString;
				}
				if(!thisUnbound && withClass &&
						(spinCommand instanceof Construct || spinCommand instanceof Update) 
						&& SPINUtil.containsThis((CommandWithWhere)spinCommand, registry)) {
					queryString = SPINUtil.addThisTypeClause(queryString);
				}
				CommandWrapper wrapper = null;
				Resource source = templateCall != null ? templateCall : spinCommand;
				if(spinCommand instanceof org.topbraid.spin.model.Query) {
					Query arqQuery = ARQFactory.get().createQuery(queryString);
					if(arqQuery.isConstructType() || (allowAsk && arqQuery.isAskType())) {
						wrapper = new QueryWrapper(arqQuery, source, spinQueryText, (org.topbraid.spin.model.Query)spinCommand, label, s, thisUnbound);
					}
				}
				else if(spinCommand instanceof Update) {
					UpdateRequest updateRequest = ARQFactory.get().createUpdateRequest(queryString);
					com.hp.hpl.jena.update.Update operation = updateRequest.getOperations().get(0);
					wrapper = new UpdateWrapper(operation, source, spinQueryText, (Update)spinCommand, label, s, thisUnbound);
				}
				if(wrapper != null) {
					Resource type = s.getSubject();
					List<CommandWrapper> list = class2Query.get(type);
					if(list == null) {
						list = new LinkedList<CommandWrapper>(); 
						class2Query.put(type, list);
					}
					list.add(wrapper);
				}
				
				if(template != null && wrapper != null) {
					Map<String,RDFNode> bindings = templateCall.getArgumentsMapByVarNames(registry);
					if(!bindings.isEmpty()) {
						initialTemplateBindings.put(wrapper, bindings);
					}
				}
			}
		}
	}

	
	/**
	 * Gets a Map of QueryWrappers with their associated classes. 
	 * @param model  the Model to operate on
	 * @param queryModel  the Model to query on (might be different)
	 * @param predicate  the predicate such as <code>spin:rule</code>
	 * @param withClass  true to also include a SPARQL clause to bind ?this
	 *                   (something along the lines of ?this a ?THIS_CLASS) 
	 * @param initialTemplateBindings  will contain the initial bindings if
	 *                                 QueryWrappers wrap SPIN template calls
	 * @param allowAsk  also return ASK queries
	 * @param registry TODO
	 * @return the result Map, possibly empty but not null
	 */
	public static Map<Resource, List<CommandWrapper>> getClass2QueryMap(Model model, Model queryModel, Property predicate, boolean withClass, Map<CommandWrapper,Map<String,RDFNode>> initialTemplateBindings, boolean allowAsk, SPINModuleRegistry registry) {
		predicate = model.getProperty(predicate.getURI());
		Map<Resource,List<CommandWrapper>> class2Query = new HashMap<Resource,List<CommandWrapper>>();
		List<Statement> ss = JenaUtil.getStatementsList(JenaUtil.listAllProperties(null, predicate));
		for(Statement s : ss) {
			add(class2Query, s, model, withClass, initialTemplateBindings, allowAsk, registry);
		}
		return class2Query;
	}

}
