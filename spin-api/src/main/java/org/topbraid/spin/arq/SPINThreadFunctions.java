package org.topbraid.spin.arq;

import java.util.HashMap;
import java.util.Map;

import org.topbraid.spin.model.Function;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.function.FunctionFactory;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory;

/**
 * A helper object that can be used to register SPARQL functions
 * (and property functions) per thread, e.g. per servlet request.
 * 
 * @author Holger Knublauch
 */
public class SPINThreadFunctions {
	
	private Map<String,FunctionFactory> functionsCache = new HashMap<String,FunctionFactory>();
	
	private Map<String,PropertyFunctionFactory> pfunctionsCache = new HashMap<String,PropertyFunctionFactory>();

	private Model model;
	
	
	SPINThreadFunctions(Model model) {
		this.model = model;
	}
	
	
	FunctionFactory getFunctionFactory(String uri, SPINModuleRegistry registry) {
		FunctionFactory old = functionsCache.get(uri);
		if(old != null) {
			return old;
		}
		else if(functionsCache.containsKey(uri)) {
			return null;
		}
		else {
			return getFunctionFactoryFromModel(uri, registry);
		}
	}
	
	
	PropertyFunctionFactory getPFunctionFactory(String uri, SPINModuleRegistry registry) {
		PropertyFunctionFactory old = pfunctionsCache.get(uri);
		if(old != null) {
			return old;
		}
		else if(pfunctionsCache.containsKey(uri)) {
			return null;
		}
		else {
			return getPropertyFunctionFactoryFromModel(uri, registry);
		}
	}


	private FunctionFactory getFunctionFactoryFromModel(String uri, SPINModuleRegistry registry) {
		Function spinFunction = model.getResource(uri).as(Function.class);
		if(JenaUtil.hasIndirectType(spinFunction, (Resource)SPIN.Function.inModel(spinFunction.getModel()))) {
			FunctionFactory arqFunction = SPINFunctionDrivers.get().create(spinFunction, registry);
			if(arqFunction != null) {
				functionsCache.put(uri, arqFunction);
				return arqFunction;
			}
		}
		// Remember failed attempt for future
		functionsCache.put(uri, null);
		return null;
	}


	private PropertyFunctionFactory getPropertyFunctionFactoryFromModel(String uri, SPINModuleRegistry registry) {
		Function spinFunction = (Function) model.getResource(uri).as(Function.class);
		if(JenaUtil.hasIndirectType(spinFunction, (Resource)SPIN.MagicProperty.inModel(spinFunction.getModel()))) {
			if(spinFunction.hasProperty(SPIN.body)) {
				final SPINARQPFunction arqFunction = new SPINARQPFunction(spinFunction, registry);
				pfunctionsCache.put(uri, arqFunction);
				return arqFunction;
			}
		}
		// Remember failed attempt for future
		pfunctionsCache.put(uri, null);
		return null;
	}
}
