package dev.coop.facturation;

import dev.coop.facturation.model.Utilisateur;
import dev.coop.facturation.persistence.UtilisateurRepository;
import java.io.IOException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Facturation.class)
public class LazyTest {
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private DevcoopInitializer dataInitializer;
    
    @Before
    public void setup() throws IOException {
        dataInitializer.doInit();
    }
    
    @Test
    public void test() {
        final Utilisateur laurent = utilisateurRepository.findByLogin("laurent");
        Assert.assertNotNull(laurent.getSociete());
        Assert.assertEquals("DEVCOOP Consulting", laurent.getSociete().getNom());
        
    }
}
