package dev.coop.facturation;

import com.google.common.io.ByteStreams;
import dev.coop.facturation.model.*;
import dev.coop.facturation.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;

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

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    Utilisateur utilisateur = new Utilisateur()
            .setLogin(LAURENT)
            .setPassword(passwordEncoder.encode("password"))
            .setSociete(devcoop);

    Client intitek = new Client(devcoop, 12)
            .setNom("Intitek")
            .setNomCourt("Intitek")
            .setAdresse(new Adresse().setRue1("20 boulevard Eugène de Ruelle").setCodePostal("69003").setVille("LYON"));

    Client onePoint = new Client(devcoop, 13)
            .setNom("Groupe One Point")
            .setNomCourt("OnePoint")
            .setAdresse(new Adresse().setRue1("235 Avenue le jour se lève").setCodePostal("92100").setVille("BOULOGNE BILLANCOURT"));

    Article article = new Article(devcoop, 1)
            .setDescription("Prestation de développement")
            .setMontant(new Montant(360))
            .setTva(TVA.CURRENT_20)
            .setUnite(Unite.JOUR);

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

    public DevcoopInitializer() throws IOException {
    }

    public void doInit() throws IOException {
        
        clean();
        societeRepository.save(devcoop);
        utilisateurRepository.save(utilisateur);
        clientRepository.save(intitek);
        clientRepository.save(onePoint);
        articleRepository.save(article);
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
