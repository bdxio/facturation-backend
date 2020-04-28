package dev.coop.facturation;

import com.google.common.io.ByteStreams;
import dev.coop.facturation.model.Adresse;
import dev.coop.facturation.model.Article;
import dev.coop.facturation.model.Client;
import dev.coop.facturation.model.Ligne;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Montant;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.TVA;
import dev.coop.facturation.model.Unite;
import dev.coop.facturation.model.Utilisateur;
import dev.coop.facturation.persistence.ArticleRepository;
import dev.coop.facturation.persistence.ClientRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.persistence.SocieteRepository;
import dev.coop.facturation.persistence.UtilisateurRepository;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DevcoopInitializer {

    @Autowired
    private SocieteRepository societeRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private FactureRepository factureRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void doInit() throws IOException {
        
        clean();
        
        Societe devcoop = new Societe()
                .setNom(DEVCOOP_CONSULTING)
                .setNomCourt(DEVCOOP)
                .setDescription("SCOP sarl à capital variable")
                .setCapital(new Montant(22600))
                .setNaf("6201Z")
                .setNumTvaIntracom("FR66517865655")
                .setSiret("51786565500024")
                .setAdresse(new Adresse()
                        .setRue1("228 avenue de Cadaujac")
                        .setCodePostal("33850")
                        .setVille("LÉOGNAN"))
                .setTel("+33663285980")
                .setMail("contact@devcoop.fr")
                .setWeb("http://www.dev.coop")
                .setLogo(ByteStreams.toByteArray(this.getClass().getClassLoader().getResourceAsStream("devcoop.jpg")));
        societeRepository.save(devcoop);

        Utilisateur utilisateur = new Utilisateur()
                .setLogin(LAURENT)
                .setPassword(passwordEncoder.encodePassword("password", null))
                .setSociete(devcoop);
        utilisateurRepository.save(utilisateur);
        
        
        Client intitek = new Client(devcoop, 12)
                .setNom("Intitek")
                .setNomCourt("Intitek")
                .setAdresse(new Adresse().setRue1("20 boulevard Eugène de Ruelle").setCodePostal("69003").setVille("LYON"));
        clientRepository.save(intitek);

        Client onePoint = new Client(devcoop, 13)
                .setNom("Groupe One Point")
                .setNomCourt("OnePoint")
                .setAdresse(new Adresse().setRue1("235 Avenue le jour se lève").setCodePostal("92100").setVille("BOULOGNE BILLANCOURT"));
        clientRepository.save(onePoint);
        
        Article article = new Article(devcoop, 1)
                .setDescription("Prestation de développement")
                .setMontant(new Montant(360))
                .setTva(TVA.CURRENT_20)
                .setUnite(Unite.JOUR);
        articleRepository.save(article);

        Facture facture = new Facture(devcoop, 132)
                .setClient(intitek)
                .setDate(LocalDate.of(2013, 7, 13))
                .addLigne(new Ligne()
                        .setArticle(article)
                        .setDescription("Prestation de développement de Laurent Forêt pour le mois de juillet 2013.")
                        .setQuantite(19))
                .addLigne(new Ligne()
                        .setArticle(article)
                        .setDescription("Prestation d'architecture de Laurent Forêt pour le mois de juillet 2013.")
                        .setQuantite(1)
                        .setMontantUnitaire(new Montant(500)));
        factureRepository.save(facture);
    }
    
    public void clean() {
        articleRepository.deleteAll();
        clientRepository.deleteAll();
        factureRepository.deleteAll();
        societeRepository.deleteAll();
        utilisateurRepository.deleteAll();
    }
    
    public static final String LAURENT = "laurent";
    public static final String DEVCOOP = "DEVCOOP";
    public static final String DEVCOOP_CONSULTING = "DEVCOOP Consulting";
    
}
