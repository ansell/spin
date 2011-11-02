/**
 * 
 */
package org.topbraid.spin.system;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.topbraid.spin.model.Function;

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
    private SPINModuleRegistry testRegistry;
    
    private Collection<String> testFiles1;
    private Collection<String> testFiles2;
    private Collection<String> testFiles3;
    
    private OntModel loadModelFromTestFile(final Collection<String> nextTestFiles)
    {
        final Model baseModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        
        for(final String nextTestFile : nextTestFiles)
        {
            final InputStream stream = this.getClass().getResourceAsStream(nextTestFile);
            
            Assert.assertNotNull("A test file was not found nextTestFile=" + nextTestFile, stream);
            
            baseModel.read(stream, "http://test.spin.example.org/testbaseuri#");
        }
        
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
        
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // Create a new test registry together with a new standard function registry from ARQ
        this.testRegistry = new SPINModuleRegistry(FunctionRegistry.standardRegistry());
        
        this.testRegistry.init();
        
        this.testFiles1 = new ArrayList<String>(6);
        this.testFiles1.add("/sp.owl");
        this.testFiles1.add("/spl.owl");
        this.testFiles1.add("/spin.owl");
        this.testFiles1.add("/spinowl.owl");
        this.testFiles1.add("/owlrl.owl");
        this.testFiles1.add("/owlrl-all.owl");
        
        this.testFiles2 = new ArrayList<String>(3);
        this.testFiles2.add("/sp.owl");
        this.testFiles2.add("/spl.owl");
        this.testFiles2.add("/spin.owl");
        this.testFiles1.add("/owlrl.owl");
        
        this.testFiles3 = new ArrayList<String>(3);
        this.testFiles3.add("/spinowl.owl");
        
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testRegistry.reset();
        
        this.testRegistry = null;
        this.testFiles1 = null;
        this.testFiles2 = null;
        this.testFiles3 = null;
        
        // reset the current singleton registry to reduce memory leaks if the singleton is still
        // referenced somewhere
        SPINModuleRegistry.get().reset();
        
        // Set and set the singleton registry after each test to restrict sideeffects of tests
        SPINModuleRegistry.set(new SPINModuleRegistry(FunctionRegistry.standardRegistry()));
        SPINModuleRegistry.get().init();
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#getFunction(java.lang.String, com.hp.hpl.jena.rdf.model.Model)}
     * .
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
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#getFunctionsBySource(java.lang.Object)}.
     */
    @Test
    public void testGetFunctionsBySource()
    {
        this.testRegistry.reset();
        
        Collection<Function> functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles1);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());
        
        Assert.assertEquals(0, this.testRegistry.getFunctions().size());
        
        // test for a single source
        this.testRegistry.registerAll(this.loadModelFromTestFile(this.testFiles1), this.testFiles1);
        
        functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles1);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        // reset and check that the registry does not contain any more functions
        this.testRegistry.reset();
        
        functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles1);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());
        
        Assert.assertEquals(0, this.testRegistry.getFunctions().size());
    }
    
    @Test
    public void testGetFunctionsBySourceDummyObject()
    {
        final Object dummyObject = new Object();
        
        this.testRegistry.reset();
        
        Collection<Function> functionsBySource = this.testRegistry.getFunctionsBySource(dummyObject);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());
        
        Assert.assertEquals(0, this.testRegistry.getFunctions().size());
        
        // test load from a different source
        this.testRegistry.registerAll(this.loadModelFromTestFile(this.testFiles2), this.testFiles2);
        
        Assert.assertEquals(60, this.testRegistry.getFunctions().size());
        
        functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles2);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        // then check that the registry does not contain any for the dummy object
        functionsBySource = this.testRegistry.getFunctionsBySource(dummyObject);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());
        
        // reset and check that the registry does not contain any more functions
        this.testRegistry.reset();
        
        Assert.assertEquals(0, this.testRegistry.getFunctions().size());
    }
    
    @Test
    public void testGetFunctionsBySourceMultiple()
    {
        this.testRegistry.reset();
        
        Collection<Function> functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles1);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());
        
        Assert.assertEquals(0, this.testRegistry.getFunctions().size());
        
        // test for multiple sources
        
        // start with one source and verify that it returns results
        this.testRegistry.registerAll(this.loadModelFromTestFile(this.testFiles2), this.testFiles2);
        
        functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles2);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        // now load from another source
        this.testRegistry.registerAll(this.loadModelFromTestFile(this.testFiles3), this.testFiles3);
        
        functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles3);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        // go back and check if testFiles2 were still valid as a source
        functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles2);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        Assert.assertTrue(this.testRegistry.getFunctions().size() > 0);
        
        // reset and check that the registry does not contain any more functions
        this.testRegistry.reset();
        
        functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles2);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());
        
        functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles3);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertEquals(0, functionsBySource.size());
        
        Assert.assertEquals(0, this.testRegistry.getFunctions().size());
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
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#getSource(org.topbraid.spin.model.Function)}
     * .
     */
    @Test
    public void testGetSource()
    {
        // start with one source and verify that it returns results
        this.testRegistry.registerAll(this.loadModelFromTestFile(this.testFiles2), this.testFiles2);
        
        Collection<Function> functionsBySource = this.testRegistry.getFunctionsBySource(this.testFiles2);
        
        Assert.assertNotNull(functionsBySource);
        
        Assert.assertTrue(functionsBySource.size() > 0);
        
        for(Function nextFunction : functionsBySource)
        {
            Set<Object> source = this.testRegistry.getSource(nextFunction);
            
            Assert.assertNotNull(source);
            
            Assert.assertEquals(1, source.size());
            Assert.assertTrue(source.contains(testFiles2));
        }
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#getTemplate(java.lang.String, com.hp.hpl.jena.rdf.model.Model)}
     * .
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
        // testRegistry.init() is performed in setUp so check first that there are functions there
        // from that call
        Assert.assertTrue(this.testRegistry.getFunctions().size() > 0);
        
        // then reset to test that it goes back to 0 before testing init here
        this.testRegistry.reset();
        
        Assert.assertEquals(0, this.testRegistry.getFunctions().size());
        
        // do the actual test on init here
        this.testRegistry.init();
        
        Assert.assertTrue(this.testRegistry.getFunctions().size() > 0);
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#registerAll(com.hp.hpl.jena.rdf.model.Model, java.lang.Object)}
     * .
     */
    @Ignore
    @Test
    public void testRegisterAll()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#registerARQFunction(org.topbraid.spin.model.Function)}
     * .
     */
    @Ignore
    @Test
    public void testRegisterARQFunction()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#registerARQPFunction(org.topbraid.spin.model.Function)}
     * .
     */
    @Ignore
    @Test
    public void testRegisterARQPFunction()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#register(org.topbraid.spin.model.Function, java.lang.Object, boolean)}
     * .
     */
    @Ignore
    @Test
    public void testRegisterFunctionObjectBoolean()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#registerFunctions(com.hp.hpl.jena.rdf.model.Model, java.lang.Object)}
     * .
     */
    @Ignore
    @Test
    public void testRegisterFunctions()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#register(org.topbraid.spin.model.Template)}
     * .
     */
    @Ignore
    @Test
    public void testRegisterTemplate()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#registerTemplates(com.hp.hpl.jena.rdf.model.Model)}
     * .
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
        this.testRegistry.reset();
        
        Assert.assertEquals(0, this.testRegistry.getFunctions().size());
        
        // then registry some functions from testFiles
        this.testRegistry.registerAll(this.loadModelFromTestFile(this.testFiles1), this.testFiles1);
        
        // check that they were registered
        Assert.assertTrue(this.testRegistry.getFunctions().size() > 0);
        
        // then reset again to check that it goes back to 0 again after registerAll
        this.testRegistry.reset();
        
        Assert.assertEquals(0, this.testRegistry.getFunctions().size());
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#set(org.topbraid.spin.system.SPINModuleRegistry)}
     * .
     */
    @Test
    public void testSet()
    {
        // take a reference to the current singleton
        final SPINModuleRegistry currentSingleton = SPINModuleRegistry.get();
        
        // set the singleton to a different object and then test the set method
        SPINModuleRegistry.set(this.testRegistry);
        
        Assert.assertEquals(this.testRegistry, SPINModuleRegistry.get());
        
        Assert.assertNotSame(currentSingleton, SPINModuleRegistry.get());
        
        // set the singleton back to the previous singleton and then test that it was changed back
        SPINModuleRegistry.set(currentSingleton);
        
        Assert.assertEquals(currentSingleton, SPINModuleRegistry.get());
        
        Assert.assertNotSame(this.testRegistry, SPINModuleRegistry.get());
    }
    
    /**
     * Test method for
     * {@link org.topbraid.spin.system.SPINModuleRegistry#SPINModuleRegistry(com.hp.hpl.jena.sparql.function.FunctionRegistry)}
     * .
     */
    @Test
    public void testSPINModuleRegistry()
    {
        Assert.assertNotSame("New test SPINModule registry should not be the same as the singleton",
                SPINModuleRegistry.get(), this.testRegistry);
    }
    
}
