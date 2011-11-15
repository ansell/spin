/**
 * 
 */
package org.topbraid.spin.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class SPINQueryFinderUnitTest
{
    static final Logger log = LoggerFactory.getLogger(SPINQueryFinderUnitTest.class);

    private SPINModuleRegistry testRegistry = SPINModuleRegistry.get();
    
    
    private ArrayList<String> testFiles1;


    private Map<CommandWrapper, Map<String,RDFNode>> testInitialTemplateBindings;


    private Set<Object> testValidFunctionSources;

    private Set<Object> testInvalidFunctionSources;

    private Model testQueryModel;


    private Model testUnionModel;


    private Object testObject;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testObject = new Object();
        
        this.testRegistry.reset();
        this.testRegistry.init();
        
        this.testFiles1 = new ArrayList<String>(5);
        this.testFiles1.add("/test/sp");
        this.testFiles1.add("/test/spl");
        this.testFiles1.add("/test/spin");
        this.testFiles1.add("/test/owlrl");
        this.testFiles1.add("/test/owlrl-all");
        
        OntModel testOntologyModel = SpinTestUtils.loadModelFromTestFiles(testFiles1);
        
        Assert.assertEquals(7113, testOntologyModel.size());
        
        this.testRegistry.registerAll(testOntologyModel, this.testObject);
        
        this.testRegistry.init();
        
        this.testValidFunctionSources = new HashSet<Object>();
        this.testValidFunctionSources.add(this.testObject);
        
        testInvalidFunctionSources = new HashSet<Object>();
        testInvalidFunctionSources.add(new Object());
        
        this.testInitialTemplateBindings = new HashMap<CommandWrapper, Map<String,RDFNode>>();
        
        List<com.hp.hpl.jena.rdf.model.Statement> jenaStatements = new ArrayList<com.hp.hpl.jena.rdf.model.Statement>(3);
        
        com.hp.hpl.jena.rdf.model.Statement testJenaStatement1 = ResourceFactory.createStatement(ResourceFactory.createResource("http://my.example.org/test/uri/1"), ResourceFactory.createProperty("http://other.example.org/test/property/a1"), ResourceFactory.createTypedLiteral(42));
        com.hp.hpl.jena.rdf.model.Statement testJenaStatement2 = ResourceFactory.createStatement(ResourceFactory.createResource("http://my.example.org/test/uri/1"), RDF.type, ResourceFactory.createResource("http://my.example.org/test/uri/testType"));
        com.hp.hpl.jena.rdf.model.Statement testJenaStatement3 = ResourceFactory.createStatement(ResourceFactory.createResource("http://my.example.org/test/uri/testType"), OWL2.equivalentClass, ResourceFactory.createResource("http://vocab.org/test/equivalentToRuleType1"));

        jenaStatements.add(testJenaStatement1);
        jenaStatements.add(testJenaStatement2);
        jenaStatements.add(testJenaStatement3);
        
        this.testQueryModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        
        testQueryModel.add(jenaStatements);
        
        Graph[] graphs = new Graph[2]; 
        
        graphs[0] = testQueryModel.getGraph();
        graphs[1] = testOntologyModel.getGraph();
        
        MultiUnion multiUnion = new MultiUnion(graphs);
        
        this.testUnionModel = ModelFactory.createModelForGraph(multiUnion);
        
        
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testFiles1 = null;
        
        this.testInitialTemplateBindings = null;
        
        this.testValidFunctionSources = null;
        
        this.testUnionModel = null;
        this.testQueryModel = null;
        
        this.testRegistry.reset();
        this.testRegistry.init();
        
    }
    
    /**
     * Test method for {@link org.topbraid.spin.util.SPINQueryFinder#add(java.util.Map, com.hp.hpl.jena.rdf.model.Statement, com.hp.hpl.jena.rdf.model.Model, boolean, java.util.Map, boolean)}.
     */
    @Ignore
    @Test
    public void testAdd()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link org.topbraid.spin.util.SPINQueryFinder#add(java.util.Map, com.hp.hpl.jena.rdf.model.Statement, com.hp.hpl.jena.rdf.model.Model, boolean, java.util.Map, boolean, java.util.Set)}.
     */
    @Ignore
    @Test
    public void testAddWithValidFunctionSources()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link org.topbraid.spin.util.SPINQueryFinder#getClass2QueryMap(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.rdf.model.Property, boolean, java.util.Map, boolean)}.
     */
    @Ignore
    @Test
    public void testGetClass2QueryMap()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link org.topbraid.spin.util.SPINQueryFinder#getClass2QueryMap(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.rdf.model.Property, boolean, java.util.Map, boolean, java.util.Set)}.
     */
    @Test
    public void testGetClass2QueryMapWithFunctionSources()
    {
        Map<Resource,List<CommandWrapper>> cls2Query = SPINQueryFinder.getClass2QueryMap(this.testUnionModel, this.testQueryModel, SPIN.rule, true, testInitialTemplateBindings, false, this.testValidFunctionSources);
        
        Assert.assertEquals(1, cls2Query.size());
        
        for(Resource nextResource : cls2Query.keySet())
        {
            Assert.assertEquals("http://www.w3.org/2002/07/owl#Thing", nextResource.toString());
            
            Assert.assertEquals(51, cls2Query.get(nextResource).size());
            
            for(CommandWrapper nextCommandWrapper : cls2Query.get(nextResource))
            {
                log.info(nextCommandWrapper.getSPINCommand().toString());
            }
            
        }
        
        Map<Resource,List<CommandWrapper>> emptyCls2Query = SPINQueryFinder.getClass2QueryMap(this.testUnionModel, this.testQueryModel, SPIN.rule, true, testInitialTemplateBindings, false, this.testInvalidFunctionSources);
        
        Assert.assertEquals(1, emptyCls2Query.size());
        
        for(Resource nextResource : cls2Query.keySet())
        {
            Assert.assertEquals("http://www.w3.org/2002/07/owl#Thing", nextResource.toString());
            
            Assert.assertEquals(51, cls2Query.get(nextResource).size());
            
            for(CommandWrapper nextCommandWrapper : cls2Query.get(nextResource))
            {
                log.info(nextCommandWrapper.getSPINCommand().toString());
            }
            
        }

        Map<Resource,List<CommandWrapper>> cls2Constructor = SPINQueryFinder.getClass2QueryMap(this.testQueryModel, this.testQueryModel, SPIN.constructor, true, testInitialTemplateBindings, false, this.testValidFunctionSources);
        
        Assert.assertEquals(0, cls2Constructor.size());
    }
    
}
