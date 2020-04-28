package dev.coop.facturation;

import dev.coop.facturation.model.Article;
import dev.coop.facturation.model.Client;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.Utilisateur;
import dev.coop.facturation.persistence.ArticleRepository;
import dev.coop.facturation.persistence.ClientRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.persistence.UtilisateurRepository;
import java.io.IOException;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Facturation.class)
public class DevcoopInitializerTest {

    @Autowired
    private DevcoopInitializer dataInitializer;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private FactureRepository factureRepository;

    @Test
    public void testInitialization() throws IOException {
        dataInitializer.doInit();
        final Utilisateur laurent = utilisateurRepository.findByLogin(DevcoopInitializer.LAURENT);
        final Societe devcoop = laurent.getSociete();
        List<Article> articles = articleRepository.findBySociete(devcoop);
        Assert.assertEquals(1, articles.size());
        List<Client> clients = clientRepository.findBySociete(devcoop);
        Assert.assertEquals(2, clients.size());
        List<Facture> factures = factureRepository.findBySociete(devcoop);
        Assert.assertEquals(1, factures.size());
    }
}
