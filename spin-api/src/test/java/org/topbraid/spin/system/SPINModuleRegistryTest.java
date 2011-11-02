/**
 * 
 */
package org.topbraid.spin.system;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;

/**
 * Tests the Singleton and non-Singleton functionality for the SPINModuleRegistry class
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class SPINModuleRegistryTest
{
    SPINModuleRegistry testRegistry;
    private Collection<String> testFiles1;
    private ArrayList<String> testFiles2;
    private ArrayList<String> testFiles3;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // Create a new test registry together with a new standard function registry from ARQ
        testRegistry = new SPINModuleRegistry(FunctionRegistry.standardRegistry());
        
        testRegistry.init();

        testFiles1 = new ArrayList<String>(6);
        testFiles1.add("/sp.owl");
        testFiles1.add("/spl.owl");
        testFiles1.add("/spin.owl");
        testFiles1.add("/spinowl.owl");
        testFiles1.add("/owlrl.owl");
        testFiles1.add("/owlrl-all.owl");
    
        testFiles2 = new ArrayList<String>(3);
        testFiles2.add("/sp.owl");
        testFiles2.add("/spl.owl");
        testFiles2.add("/spin.owl");
        testFiles1.add("/owlrl.owl");

        testFiles3 = new ArrayList<String>(3);
        testFiles3.add("/spinowl.owl");
    
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        testRegistry.reset();
        
        testRegistry = null;
        testFiles1 = null;
        testFiles2 = null;
        testFiles3 = null;
        
        // reset the current registry to reduce memory leaks if the singleton is still referenced somewhere
        SPINModuleRegistry.get().reset();
        
        // Set and reset the singleton registry after each test to restrict sideeffects of tests
        SPINModuleRegistry.set(new SPINModuleRegistry(FunctionRegistry.standardRegistry()));
        SPINModuleRegistry.get().init();
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#SPINModuleRegistry(com.hp.hpl.jena.sparql.function.FunctionRegistry)}.
     */
    @Test
    public void testSPINModuleRegistry()
    {
        Assert.assertNotSame("New SPINModule registry should not be the same as the singleton", SPINModuleRegistry.get(), testRegistry);
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#set(org.topbraid.spin.system.SPINModuleRegistry)}.
     */
    @Test
    public void testSet()
    {
        // take a reference to the current singleton
        SPINModuleRegistry currentSingleton = SPINModuleRegistry.get();
        
        // set the singleton to a different object and then test the set method
        SPINModuleRegistry.set(testRegistry);
        
        Assert.assertEquals(testRegistry, SPINModuleRegistry.get());
        
        Assert.assertNotSame(currentSingleton, SPINModuleRegistry.get());
        
        // set the singleton back to the previous singleton and then test that it was changed back
        SPINModuleRegistry.set(currentSingleton);
        
        Assert.assertEquals(currentSingleton, SPINModuleRegistry.get());
        
        Assert.assertNotSame(testRegistry, SPINModuleRegistry.get());
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getFunction(java.lang.String, com.hp.hpl.jena.rdf.model.Model)}.
     */
    @Ignore
    @Test
    public void testGetFunction()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getFunctions()}.
     */
    @Ignore
    @Test
    public void testGetFunctions()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getModels()}.
     */
    @Ignore
    @Test
    public void testGetModels()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getSource(org.topbraid.spin.model.Function)}.
     */
    @Ignore
    @Test
    public void testGetSource()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getFunctionsBySource(java.lang.Object)}.
     */
    @Test
    public void testGetFunctionsBySource()
    {
        testRegistry.reset();

        Collection<Node> functionsBySource = testRegistry.getFunctionsBySource(testFiles1);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());

        Assert.assertEquals(0, testRegistry.getFunctions().size());

        // test for a single source
        testRegistry.registerAll(loadModelFromTestFile(testFiles1), testFiles1);
        
        functionsBySource = testRegistry.getFunctionsBySource(testFiles1);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        // reset and check that the registry does not contain any more functions
        testRegistry.reset();
        
        functionsBySource = testRegistry.getFunctionsBySource(testFiles1);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());

        Assert.assertEquals(0, testRegistry.getFunctions().size());
    }
    
    @Test
    public void testGetFunctionsBySourceMultiple()
    {
        testRegistry.reset();

        Collection<Node> functionsBySource = testRegistry.getFunctionsBySource(testFiles1);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());

        Assert.assertEquals(0, testRegistry.getFunctions().size());

        // test for multiple sources
        
        // start with one source and verify that it returns results
        testRegistry.registerAll(loadModelFromTestFile(testFiles2), testFiles2);
        
        functionsBySource = testRegistry.getFunctionsBySource(testFiles2);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        // now load from another source
        testRegistry.registerAll(loadModelFromTestFile(testFiles3), testFiles3);
        
        functionsBySource = testRegistry.getFunctionsBySource(testFiles3);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        // go back and check if testFiles2 were still valid as a source
        // FIXME: this fails after we load from testFiles3 for some reason, 
        // indicating that something is wrong with the system somewhere
        functionsBySource = testRegistry.getFunctionsBySource(testFiles2);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);

        
        Assert.assertTrue(testRegistry.getFunctions().size() > 0);

        
        // reset and check that the registry does not contain any more functions
        testRegistry.reset();
        
        functionsBySource = testRegistry.getFunctionsBySource(testFiles2);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());

        functionsBySource = testRegistry.getFunctionsBySource(testFiles3);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());

        Assert.assertEquals(0, testRegistry.getFunctions().size());
    }
    
    @Test
    public void testGetFunctionsBySourceDummyObject()
    {
        Object dummyObject = new Object();
        
        testRegistry.reset();

        Collection<Node> functionsBySource = testRegistry.getFunctionsBySource(dummyObject);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());

        Assert.assertEquals(0, testRegistry.getFunctions().size());

        // test load from a different source
        testRegistry.registerAll(loadModelFromTestFile(testFiles2), testFiles2);

        Assert.assertEquals(60, testRegistry.getFunctions().size());
        
        functionsBySource = testRegistry.getFunctionsBySource(testFiles2);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        // then check that the registry does not contain any for the dummy object
        functionsBySource = testRegistry.getFunctionsBySource(dummyObject);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());

        // reset and check that the registry does not contain any more functions
        testRegistry.reset();
        
        Assert.assertEquals(0, testRegistry.getFunctions().size());
    }

    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getTemplate(java.lang.String, com.hp.hpl.jena.rdf.model.Model)}.
     */
    @Ignore
    @Test
    public void testGetTemplate()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getTemplates()}.
     */
    @Ignore
    @Test
    public void testGetTemplates()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#init()}.
     */
    @Test
    public void testInit()
    {
        // testRegistry.init() is performed in setUp so check first that there are functions there from that call
        Assert.assertTrue(testRegistry.getFunctions().size() > 0);
        
        // then reset to test that it goes back to 0 before testing init here
        testRegistry.reset();

        Assert.assertEquals(0, testRegistry.getFunctions().size());
    
        // do the actual test on init here
        testRegistry.init();
        
        Assert.assertTrue(testRegistry.getFunctions().size() > 0);
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#register(org.topbraid.spin.model.Function, java.lang.Object, boolean)}.
     */
    @Ignore
    @Test
    public void testRegisterFunctionObjectBoolean()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#register(org.topbraid.spin.model.Template)}.
     */
    @Ignore
    @Test
    public void testRegisterTemplate()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerAll(com.hp.hpl.jena.rdf.model.Model, java.lang.Object)}.
     */
    @Ignore
    @Test
    public void testRegisterAll()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerARQFunction(org.topbraid.spin.model.Function)}.
     */
    @Ignore
    @Test
    public void testRegisterARQFunction()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerARQPFunction(org.topbraid.spin.model.Function)}.
     */
    @Ignore
    @Test
    public void testRegisterARQPFunction()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerFunctions(com.hp.hpl.jena.rdf.model.Model, java.lang.Object)}.
     */
    @Ignore
    @Test
    public void testRegisterFunctions()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerTemplates(com.hp.hpl.jena.rdf.model.Model)}.
     */
    @Ignore
    @Test
    public void testRegisterTemplates()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#reset()}.
     */
    @Test
    public void testReset()
    {
        // Reset the registry, as it contains functions from the testRegistry.init() call in setup
        testRegistry.reset();

        Assert.assertEquals(0, testRegistry.getFunctions().size());

        // then registry some functions from testFiles
        testRegistry.registerAll(loadModelFromTestFile(testFiles1), testFiles1);
        
        // check that they were registered
        Assert.assertTrue(testRegistry.getFunctions().size() > 0);
        
        // then reset again to check that it goes back to 0 again after registerAll
        testRegistry.reset();

        Assert.assertEquals(0, testRegistry.getFunctions().size());
    }
    
    private OntModel loadModelFromTestFile(Collection<String> nextTestFiles)
    {
        Model baseModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        
        for(String nextTestFile : nextTestFiles)
        {
            InputStream stream = this.getClass().getResourceAsStream(nextTestFile);
            
            Assert.assertNotNull("A test file was not found nextTestFile="+nextTestFile, stream);
            
            baseModel.read(stream, "http://test.spin.example.org/testbaseuri#");
        }
        
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
        
    }
    
}
