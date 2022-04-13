package dev.coop.facturation;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Facturation.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LazyTest {

    @Autowired
    private DevcoopInitializer dataInitializer;
    
    @Before
    public void setup() throws IOException {
        dataInitializer.doInit();
    }
    
    @Test
    public void test() throws IOException {
        Assert.assertNotNull(dataInitializer.getSociete());
        Assert.assertEquals("DEVCOOP Consulting", dataInitializer.getSociete().getNom());
        
    }
}
