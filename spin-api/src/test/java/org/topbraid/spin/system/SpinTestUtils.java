package org.topbraid.spin.system;

import java.io.InputStream;
import java.util.Collection;

import org.junit.Assert;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.ReificationStyle;

public class SpinTestUtils
{

    public static OntModel loadModelFromTestFiles(final Collection<String> nextTestFiles)
    {
        final Model baseModel = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
        
        for(final String nextTestFile : nextTestFiles)
        {
            final InputStream stream = SpinTestUtils.class.getResourceAsStream(nextTestFile);
            
            Assert.assertNotNull("A test file was not found nextTestFile=" + nextTestFile, stream);
            
            baseModel.read(stream, "http://test.spin.example.org/testbaseuri#");
        }
        
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
        
    }
    
}
