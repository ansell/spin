/**
 * 
 */
package org.topbraid.spin.system;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.function.FunctionRegistry;

/**
 * @author peter
 *
 */
public class SPINModuleRegistryTest
{
    SPINModuleRegistry testRegistry;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // Create a new test registry together with a new standard function registry from ARQ
        testRegistry = new SPINModuleRegistry(FunctionRegistry.standardRegistry());
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        testRegistry = null;
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
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getFunction(java.lang.String, com.hp.hpl.jena.rdf.model.Model)}.
     */
    @Test
    public void testGetFunction()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getFunctions()}.
     */
    @Test
    public void testGetFunctions()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getModels()}.
     */
    @Test
    public void testGetModels()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getSource(org.topbraid.spin.model.Function)}.
     */
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
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getTemplate(java.lang.String, com.hp.hpl.jena.rdf.model.Model)}.
     */
    @Test
    public void testGetTemplate()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#getTemplates()}.
     */
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
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#register(org.topbraid.spin.model.Function, java.lang.Object, boolean)}.
     */
    @Test
    public void testRegisterFunctionObjectBoolean()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#register(org.topbraid.spin.model.Template)}.
     */
    @Test
    public void testRegisterTemplate()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerAll(com.hp.hpl.jena.rdf.model.Model, java.lang.Object)}.
     */
    @Test
    public void testRegisterAll()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerARQFunction(org.topbraid.spin.model.Function)}.
     */
    @Test
    public void testRegisterARQFunction()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerARQPFunction(org.topbraid.spin.model.Function)}.
     */
    @Test
    public void testRegisterARQPFunction()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerFunctions(com.hp.hpl.jena.rdf.model.Model, java.lang.Object)}.
     */
    @Test
    public void testRegisterFunctions()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link org.topbraid.spin.system.SPINModuleRegistry#registerTemplates(com.hp.hpl.jena.rdf.model.Model)}.
     */
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
        Assert.fail("Not yet implemented");
    }
    
}
