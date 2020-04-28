package dev.coop.facturation;

import com.google.common.io.ByteStreams;
import dev.coop.facturation.model.Adresse;
import dev.coop.facturation.model.Article;
import dev.coop.facturation.model.Client;
import dev.coop.facturation.model.Devis;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Ligne;
import dev.coop.facturation.model.Montant;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.TVA;
import dev.coop.facturation.model.Unite;
import dev.coop.facturation.persistence.ArticleRepository;
import dev.coop.facturation.persistence.ClientRepository;
import dev.coop.facturation.persistence.DevisRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.persistence.SocieteRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author lfo
 */
@Component 
public class BdxIoInitializer {

    @Autowired
    private SocieteRepository societeRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private FactureRepository factureRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private DevisRepository devisRepository;
    
    public void doInit() throws Exception {
        
        Societe bdxio = new Societe()
                .setAdresse(new Adresse().setRue1("37 rue Jean Moulin").setCodePostal("33140").setVille("Villenave d'Ornon"))
                .setNom(BORDEAUX_DEVELOPER_EXPERIENCE)
                .setNomCourt(BDXIO)
                .setDescription("BDX.IO")
                .setFormeJuridique("Association loi 1901")
                .setWeb("http://www.bdx.io")
                .setMail("team@bdx.io")
                .setLogo(ByteStreams.toByteArray(this.getClass().getClassLoader().getResourceAsStream("bdxio.png")))
                .setBic("CMBRFR2BARK")
                .setIban("FR76 1558 9335 7307 3463 7354 089")
                .setSiret("811 910 520 00014")
                .setDelaiPaiement(0);
        
        societeRepository.save(bdxio);
        ObjectFactory factory = new ObjectFactory(bdxio);
        
        // ARTICLES 
        final Article gold = factory.createArticle(1)
                .setDescription("Sponsoring BDX.IO catégorie Gold ")
                .setMontant(new Montant(4096))
                .setTva(TVA.NONE);
        
        articleRepository.save(gold);
        final Article silver = factory.createArticle(2)
                .setDescription("Sponsoring BDX.IO catégorie Silver ")
                .setMontant(new Montant(2048))
                .setTva(TVA.NONE);
        
        articleRepository.save(silver);
        final Article bronze = factory.createArticle(3)
                .setDescription("Sponsoring BDX.IO catégorie Bronze")
                .setMontant(new Montant(1024))
                .setTva(TVA.NONE);
        articleRepository.save(bronze);
        
        final Article early = factory.createArticle(4)
                .setDescription("Place early Bird 2015")
                .setMontant(new Montant(32))
                .setTva(TVA.NONE);
        articleRepository.save(early);
        
        final Article fraisDossier = factory.createArticle(5)
                .setDescription("Frais de Dossier") 
                .setMontant(new Montant(50))
                .setTva(TVA.NONE);
        articleRepository.save(fraisDossier);
        
        
        // CLIENTS
        int nbCl = 1;
        
        Client doYouDreamUp = factory.createClient(nbCl++).setNom("Do You Dream Up")
                .setNomCourt("DoYouDreamUp")
                .setAdresse(new Adresse().setRue1("17 rue de Cléry").setCodePostal("75002").setVille("Paris"));
        clientRepository.save(doYouDreamUp);
        
        Client ambares = factory.createClient(nbCl++)
                .setNom("Mairie d'Ambarès et Lagrave")
                .setNomCourt("AmbaresEtLagrave")
                .setAdresse(new Adresse()
                        .setRue1("18 Place de la Victoire")
                        .setCodePostal("33440")
                        .setVille("Ambarès-et-Lagrave"));
        clientRepository.save(ambares);
        
        Client onePoint = factory.createClient(nbCl++)
                .setNom("GROUPE ONEPOINT")
                .setNomCourt("OnePoint")
                .setAdresse(new Adresse()
                        .setRue1("235 Avenue le Jour se Lève")
                        .setCodePostal("92100")
                        .setVille("BOULOGNE-BILLANCOURT")
                );
        clientRepository.save(onePoint);
        
        Client cleverAge = factory.createClient(nbCl++)
                .setNom("Clever Age")
                .setNomCourt("CleverAge")
                .setAdresse(new Adresse()
                        .setRue1("34 rue de Saint Pétersbourg")
                        .setCodePostal("75008")
                        .setVille("PARIS")
                );
        clientRepository.save(cleverAge);
        
        Client ezakus = factory.createClient(nbCl++)
                .setNom("Ezakus")
                .setNomCourt("Ezakus")
                .setAdresse(new Adresse()
                        .setRue1("15 avenue de Chavailles")
                        .setCodePostal("33520")
                        .setVille("BRUGES")
                );
        clientRepository.save(ezakus);
        
        Client zenika = factory.createClient(nbCl++)
                .setNom("Zenika")
                .setNomCourt("Zenika")
                .setAdresse(new Adresse()
                        .setRue1("10 rue de Milan")
                        .setCodePostal("75009")
                        .setVille("PARIS")
                );
        clientRepository.save(zenika);
        
        Client sqli = factory.createClient(nbCl++)
                .setNom("SQLI")
                .setNomCourt("SQLI")
                .setAdresse(new Adresse()
                        .setRue1("268 Avenue du président Wilson")
                        .setCodePostal("93210")
                        .setVille("La Plaine Saint-Denis")
                );
        clientRepository.save(sqli);
        
        Client lectra = factory.createClient(nbCl++)
                .setNom("Lectra")
                .setNomCourt("Lectra")
                .setAdresse(new Adresse()
                    .setRue1("23 Chemin de Marticot")
                        .setCodePostal("33610")
                        .setVille("Cestas"))
                ;
        clientRepository.save(lectra);
        
        Client google = factory.createClient(nbCl++)
                .setNom("Google France SARL")
                .setNomCourt("Google")
                .setAdresse(new Adresse()
                        .setRue1("8 Rue de Londres")
                        .setCodePostal("75009")
                        .setVille("Paris"));
        clientRepository.save(google);
        
        Client capgemini = factory.createClient(nbCl++)
                .setNom("Capgemini Technology Services")
                .setNomCourt("Capgemini")
                .setAdresse(new Adresse()
                        .setRue1("5-7 rue Frédéric Clavel")
                        .setCodePostal("92150")
                        .setVille("Suresnes"));
        clientRepository.save(capgemini);
        
        Client crdNicolasBourbaki = factory.createClient(nbCl++)
                .setNom("CRD Nicolas-Bourbaki")
                .setNomCourt("NicolasBourbaki")
                .setAdresse(new Adresse()
                        .setRue1("294 route des Grands Bois")
                        .setCodePostal("74370")
                        .setVille("Villaz"));
        clientRepository.save(capgemini);
        
        Client arcaComputing = factory.createClient(nbCl++)
                .setNom("ARCA Computing")
                .setNomCourt("ARCAComputing")
                .setAdresse(new Adresse()
                        .setRue1("74 rue Georges Bonnac")
                        .setCodePostal("33000")
                        .setVille("Bordeaux"));
        clientRepository.save(arcaComputing);
        
        Client neo4j = factory.createClient(nbCl++)
                .setNom("Network Engine for Objects in Lund AB")
                .setNomCourt("Neo4J")
                .setNumTVAIntracom("SE556713110601")
                .setAdresse(new Adresse()
                        .setRue1("Anckargripsgatan 3,211")
                        .setCodePostal("19")
                        .setVille("Malmö")
                        .setPays("Sweden"));
        clientRepository.save(neo4j);
        
        Client iutBordeaux = factory.createClient(nbCl++)
                .setNom("Université Bordeaux - Service Facturier")
                .setNomCourt("IUTBordeaux")
                .setAdresse(new Adresse()
                        .setRue1("CS 61292")
                        .setRue2("146 rue Léo Saignat")
                        .setCodePostal("33076")
                        .setVille("Bordeaux Cedex"));
        clientRepository.save(iutBordeaux);
                
        
        
        // FACTURES
        Facture facture150701  = factory.createFacture(150701)
                .setClient(doYouDreamUp)
                .setDate(LocalDate.of(2015, 7, 10))
                .addLigne(new Ligne().setArticle(gold).setQuantite(BigDecimal.ONE).setUnite(Unite.JOUR));
        factureRepository.save(facture150701);
        
        Facture facture150801 = factory.createFacture(150801)
                .setClient(onePoint)
                .setDate(LocalDate.of(2015, Month.AUGUST, 6))
                .addLigne(new Ligne().setArticle(silver).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150801);
        
        Facture facture150802 = factory.createFacture(150802)
                .setClient(cleverAge)
                .setDate(LocalDate.of(2015, Month.AUGUST, 6))
                .addLigne(new Ligne().setArticle(bronze).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150802);
        
        Facture facture150803 = factory.createFacture(150803)
                .setClient(ezakus)
                .setDate(LocalDate.of(2015, Month.AUGUST, 6))
                .addLigne(new Ligne().setArticle(bronze).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150803);
        
        Facture facture150804 = factory.createFacture(150804)
                .setClient(zenika)
                .setDate(LocalDate.of(2015, Month.AUGUST, 6))
                .addLigne(new Ligne().setArticle(bronze).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150804);
        
        Facture facture150805 = factory.createFacture(150805)
                .setClient(sqli)
                .setDate(LocalDate.of(2015, Month.AUGUST, 21))
                .addLigne(new Ligne().setArticle(silver).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150805);
        
        Facture facture150806 = factory.createFacture(150806)
                .setClient(lectra)
                .setDate(LocalDate.of(2015,Month.AUGUST,31))
                .addLigne(new Ligne().setArticle(bronze).setQuantite(BigDecimal.ONE)
                        .setDescription("Sponsoring BDX.IO catégorie Bronze.\nBon de commande n° 609981"));
        factureRepository.save(facture150806);
        
        Facture facture150901 = factory.createFacture(150901)
                .setClient(google)
                .setDate(LocalDate.of(2015, Month.SEPTEMBER, 2))
                .addLigne(new Ligne().setArticle(silver).setQuantite(BigDecimal.ONE)
                    .setDescription("Sponsoring BDX.IO catégorie Silver.\nBon de commande n° 24009591"));
        factureRepository.save(facture150901);
                
        Facture facture150902 = factory.createFacture(150902)
                .setClient(ambares)
                .setDate(LocalDate.of(2015, Month.SEPTEMBER, 7))
                .addLigne(new Ligne().setArticle(early).setQuantite(BigDecimal.ONE))
                .addLigne(new Ligne().setArticle(fraisDossier).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150902);
        
        Facture facture150903 = factory.createFacture(150903)
                .setClient(capgemini)
                .setDate(LocalDate.of(2015, Month.SEPTEMBER, 16))
                .addLigne(new Ligne().setArticle(gold).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150903);
        
        Facture facture150904 = factory.createFacture(150904)
                .setClient(crdNicolasBourbaki)
                .setDate(LocalDate.of(2015, Month.SEPTEMBER, 16))
                .addLigne(new Ligne().setArticle(silver).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150904);
        
        Facture facture150905 = factory.createFacture(150905)
                .setClient(arcaComputing)
                .setDate(LocalDate.of(2015, Month.SEPTEMBER, 16))
                .addLigne(new Ligne().setArticle(bronze).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150905);
        
        Facture facture150906 = factory.createFacture(150906)
                .setClient(iutBordeaux)
                .setDate(LocalDate.of(2015, Month.SEPTEMBER, 21))
                .addLigne(new Ligne().setArticle(silver).setQuantite(BigDecimal.ONE));
        factureRepository.save(facture150906);
        
        
        Facture facture151001 = factory.createFacture(151001)
            .setClient(iutBordeaux)
            .setDate(LocalDate.of(2015,10,8))
            .addLigne(
                new Ligne().setDescription("Place BDX IO 2015\nBon de commande N° 52513\nRéférence : 419 B1/4105 INFO/195").setQuantite(40).setMontantUnitaire(new Montant(25)).setTva(TVA.NONE)
                );
        factureRepository.save(facture151001);
        
        // DEVIS
        Devis devis150801 = factory.createDevis(150801);
        devis150801.setClient(ambares);
        devis150801.setDate(LocalDate.of(2015,8,5));
        devis150801.addLigne(new Ligne().setArticle(early).setQuantite(BigDecimal.ONE))
                .addLigne(new Ligne().setArticle(fraisDossier).setQuantite(BigDecimal.ONE));
        devisRepository.save(devis150801);
     
        Devis devis151001 = factory.createDevis(151001);
        devis151001.setClient(iutBordeaux);
        devis151001.setDate(LocalDate.of(2015,10,8));
        devis151001.addLigne(
                new Ligne().setDescription("Place BDX IO 2015").setQuantite(40).setMontantUnitaire(new Montant(25)).setTva(TVA.NONE)
                );
        devisRepository.save(devis151001);
     
    }
    public static final String BDXIO = "BDXIO";
    
    public static final String BORDEAUX_DEVELOPER_EXPERIENCE = "Bordeaux Developer eXperience";
    
}
