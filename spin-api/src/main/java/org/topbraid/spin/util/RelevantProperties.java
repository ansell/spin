package org.topbraid.spin.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topbraid.spin.internal.ObjectPropertiesGetter;
import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.Command;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.QueryOrTemplateCall;
import org.topbraid.spin.model.SPINInstance;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Control logic that determines "relevant" properties for given classes or instances.
 * 
 * @author Holger Knublauch
 */
public class RelevantProperties {


	private static void addProperties(QueryOrTemplateCall qot, Set<Property> results, SPINModuleRegistry registry) {
		Model model = qot.getCls().getModel();
		if(qot.getTemplateCall() != null) {
			TemplateCall templateCall = qot.getTemplateCall();
			Template template = templateCall.getTemplate(registry);
			if(template != null) {
				Command spinQuery = template.getBody();
				if(spinQuery instanceof Ask || spinQuery instanceof Construct) {
					ObjectPropertiesGetter getter = new ObjectPropertiesGetter(model, ((Query)spinQuery).getWhere(), templateCall.getArgumentsMapByProperties(registry), registry);
					getter.run();
					results.addAll(getter.getResults());
				}
			}
		}
		else if(qot.getQuery() instanceof Ask || qot.getQuery() instanceof Construct) {
			ElementList where = qot.getQuery().getWhere();
			if(where != null) {
				ObjectPropertiesGetter getter = new ObjectPropertiesGetter(model, where, null, registry);
				getter.run();
				results.addAll(getter.getResults());
			}				
		}
	}
	
	
	public static Set<Property> getRelevantPropertiesOfClass(Resource cls, SPINModuleRegistry registry) {
		Set<Property> results = new HashSet<Property>();
		
		StmtIterator it = cls.getModel().listStatements(null, RDFS.domain, cls);
		while (it.hasNext()) {
			Resource subject = it.next().getSubject();
			if (subject.isURIResource()) {
				results.add(cls.getModel().getProperty(subject.getURI()));
				JenaUtil.addDomainlessSubProperties(subject, results, new HashSet<Resource>());
			}
		}
		
		for(Resource superClass : JenaUtil.getSuperClasses(cls)) {
			Statement s = superClass.getProperty(OWL.onProperty);
			if(s != null && s.getObject().isURIResource()) {
				results.add(cls.getModel().getProperty(s.getResource().getURI()));
			}
		}
		
		Set<Property> others = RelevantProperties.getRelevantSPINPropertiesOfClass(cls, registry);
		if(others != null) {
			for(Property other : others) {
				results.add(other);
			}
		}
		
		return results;
	}


	public static Set<Property> getRelevantSPINPropertiesOfInstance(Resource root, SPINModuleRegistry registry) {
		if(SP.exists(root.getModel())) {
			SPINInstance instance = root.as(SPINInstance.class);
			Set<Property> results = new HashSet<Property>();
			for(QueryOrTemplateCall qot : instance.getQueriesAndTemplateCalls(SPIN.constraint, registry)) {
				addProperties(qot, results, registry);
			}
			return results;
		}
		else {
			return null;
		}
	}


	public static Set<Property> getRelevantSPINPropertiesOfClass(Resource cls, SPINModuleRegistry registry) {
		if(SP.exists(cls.getModel())) {
			List<QueryOrTemplateCall> qots = new ArrayList<QueryOrTemplateCall>();
			SPINUtil.addQueryOrTemplateCalls(cls, SPIN.constraint, qots, registry);
			Set<Property> results = new HashSet<Property>();
			for(QueryOrTemplateCall qot : qots) {
				addProperties(qot, results, registry);
			}
			return results;
		}
		else {
			return null;
		}
	}
}
