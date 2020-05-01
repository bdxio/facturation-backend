package dev.coop.facturation.service;

import com.google.common.io.ByteStreams;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import dev.coop.facturation.model.Adresse;
import dev.coop.facturation.model.Article;
import dev.coop.facturation.model.Client;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Ligne;
import dev.coop.facturation.model.Montant;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.SocieteCodeKey;
import dev.coop.facturation.model.TVA;
import dev.coop.facturation.model.Unite;
import dev.coop.facturation.persistence.ArticleRepository;
import dev.coop.facturation.persistence.ClientRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.persistence.SocieteRepository;
import dev.coop.facturation.google.GoogleSheets;
import dev.coop.facturation.google.GsException;
import dev.coop.facturation.model.Devis;
import dev.coop.facturation.persistence.DevisRepository;
import dev.coop.facturation.tools.Dates;
import dev.coop.facturation.tools.Numbers;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author lfo
 */
@Service
public class SpreadsheetImporter {

    @Autowired
    private GoogleSheets googleSheets;
    @Autowired
    private SocieteRepository societeRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private FactureRepository factureRepository;
    @Autowired
    private DevisRepository devisRepository;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void importAll(String worksheetId) {
        final WorksheetFeed worksheets = googleSheets.getWorksheetFeed(worksheetId);
        importSocietes(worksheets);
        importClients(worksheets);
        importArticles(worksheets);
        importFactures(worksheets);
        importDevis(worksheets);

    }

    public void importSocietes(WorksheetFeed feed) {
        try {
            final WorksheetEntry worksheet = googleSheets.getWorksheetEntry(feed, SOCIETE);

            URL listFeedUrl = worksheet.getListFeedUrl();
            ListFeed listFeed = googleSheets.getService().getFeed(listFeedUrl, ListFeed.class);

            listFeed.getEntries().stream().forEach((listEntry) -> {
                final CustomElementCollection cel = listEntry.getCustomElements();
                String nomCourt = cel.getValue(NOM_COURT);
                if (nomCourt == null) {
                    return;
                }
                final Societe societe = new Societe();
                societe.setNom(cel.getValue(NOM))
                        .setNomCourt(nomCourt)
                        .setFormeJuridique(cel.getValue(FORME_JURIDIQUE))
                        .setDescription(cel.getValue(DESCRIPTION))
                        .setAdresse(importAdresse(cel))
                        .setTel(cel.getValue(TEL))
                        .setFax(cel.getValue(FAX))
                        .setWeb(cel.getValue(WEB))
                        .setMail(cel.getValue(MAIL))
                        .setSiret(cel.getValue(SIRET))
                        .setNaf(cel.getValue(NAF))
                        .setNumTvaIntracom(cel.getValue(NUM_TVA_INTRACOM))
                        .setBic(cel.getValue(BIC))
                        .setIban(cel.getValue(IBAN))
                        .setCapital(new Montant(Numbers.toBigDecimal(cel.getValue(CAPITAL))))
                        .setDelaiPaiement(Numbers.toInt(cel.getValue(DELAI_PAIEMENT)));
                String logoUrl = cel.getValue(LOGO);

                try (InputStream logoInput = new URL(logoUrl).openStream()) {
                    societe.setLogo(ByteStreams.toByteArray(logoInput));
                } catch (MalformedURLException ex) {
                    throw new GsException(ex);
                } catch (IOException ex) {
                    throw new GsException(ex);
                }
                societeRepository.save(societe);
            });
        } catch (IOException | ServiceException ex) {
            throw new GsException(ex);
        }
    }

    private static Adresse importAdresse(final CustomElementCollection cel) {
        return new Adresse()
                .setRue1(cel.getValue(RUE1))
                .setRue2(cel.getValue(RUE2))
                .setRue3(cel.getValue(RUE3))
                .setCodePostal(cel.getValue(CODE_POSTAL))
                .setVille(cel.getValue(VILLE))
                .setPays(cel.getValue(PAYS));
    }

    public void importArticles(WorksheetFeed feed) {
        try {
            final WorksheetEntry worksheet = googleSheets.getWorksheetEntry(feed, ARTICLE);

            URL listFeedUrl = worksheet.getListFeedUrl();
            ListFeed listFeed = googleSheets.getService().getFeed(listFeedUrl, ListFeed.class);

            listFeed.getEntries().stream().forEach((listEntry) -> {
                final CustomElementCollection cel = listEntry.getCustomElements();
                String idValue = cel.getValue(ID);
                if (idValue == null) {
                    return;
                }

                Societe societe = societeRepository.findByIdOrThrow(cel.getValue(SOCIETEREF));
                int id = Numbers.toInt(idValue);

                Article article = new Article(societe, id)
                        .setDescription(cel.getValue(DESCRIPTION))
                        .setMontant(new Montant(Numbers.toBigDecimal(cel.getValue(MONTANT))))
                        .setTva(TVA.valueOf(cel.getValue(TVA_)));

                if (cel.getValue(UNITE) != null) {
                    article.setUnite(Unite.valueOf(cel.getValue(UNITE)));
                }
                articleRepository.save(article);

            });
        } catch (IOException | ServiceException ex) {
            throw new GsException(ex);
        }
    }

    public void importClients(WorksheetFeed feed) {
        try {
            final WorksheetEntry worksheet = googleSheets.getWorksheetEntry(feed, CLIENT);

            URL listFeedUrl = worksheet.getListFeedUrl();
            ListFeed listFeed = googleSheets.getService().getFeed(listFeedUrl, ListFeed.class);

            listFeed.getEntries().stream().forEach((listEntry) -> {
                final CustomElementCollection cel = listEntry.getCustomElements();

                Societe societe = societeRepository.findByIdOrThrow(cel.getValue(SOCIETEREF));
                int id = Numbers.toInt(cel.getValue(ID));

                Client client = new Client(societe, id)
                        .setAdresse(importAdresse(cel))
                        .setNom(cel.getValue(NOM))
                        .setNomCourt(cel.getValue(NOM_COURT))
                        .setNumTVAIntracom(cel.getValue(NUM_TVA_INTRACOM));
                clientRepository.save(client);

            });
        } catch (IOException | ServiceException ex) {
            throw new GsException(ex);
        }
    }

    public void importFactures(WorksheetFeed feed) {
        try {
            final WorksheetEntry worksheet = googleSheets.getWorksheetEntry(feed, FACTURE);

            URL listFeedUrl = worksheet.getListFeedUrl();
            ListFeed listFeed = googleSheets.getService().getFeed(listFeedUrl, ListFeed.class);

            final Map<Integer, Facture> factureMap = new HashMap<>();
            listFeed.getEntries().stream().forEach((listEntry) -> {
                final CustomElementCollection cel = listEntry.getCustomElements();
                final Societe societe = societeRepository.findByIdOrThrow(cel.getValue(SOCIETEREF));
                final Client client = clientRepository.findByIdOrThrow(SocieteCodeKey.create(societe, Numbers.toInt(cel.getValue(CLIENTREF))));
                final Article article = articleRepository.findByIdOrThrow(SocieteCodeKey.create(societe, Numbers.toInt(cel.getValue(ARTICLEREF))));
                final LocalDate date = Dates.parse(cel.getValue(DATE));

                String description = cel.getValue(DESCRIPTION);
                description = description == null ? article.getDescription() : description;
                description = description.replace("${date}", date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE)));

                final Integer factureId = Numbers.toInt(cel.getValue(ID));
                Facture facture = factureMap.get(factureId);
                if (facture == null) {
                    facture = new Facture(societe, factureId)
                            .setDate(date)
                            .setClient(client);
                    factureMap.put(factureId, facture);
                }
                facture.addLigne(new Ligne()
                        .setArticle(article)
                        .setQuantite(Numbers.toBigDecimal(cel.getValue(QUANTITE)))
                        .setDescription(description));
                factureRepository.save(facture);
            });

        } catch (IOException | ServiceException ex) {
            throw new GsException(ex);
        }
    }
    
    
    public void importDevis(WorksheetFeed feed) {
        try {
            final WorksheetEntry worksheet = googleSheets.getWorksheetEntry(feed, DEVIS);

            URL listFeedUrl = worksheet.getListFeedUrl();
            ListFeed listFeed = googleSheets.getService().getFeed(listFeedUrl, ListFeed.class);

            final Map<Integer, Devis> devisMap = new HashMap<>();
            listFeed.getEntries().stream().forEach((listEntry) -> {
                final CustomElementCollection cel = listEntry.getCustomElements();
                final Societe societe = societeRepository.findByIdOrThrow(cel.getValue(SOCIETEREF));
                final Client client = clientRepository.findByIdOrThrow(SocieteCodeKey.create(societe, Numbers.toInt(cel.getValue(CLIENTREF))));
                final Article article = articleRepository.findByIdOrThrow(SocieteCodeKey.create(societe, Numbers.toInt(cel.getValue(ARTICLEREF))));
                final LocalDate date = Dates.parse(cel.getValue(DATE));

                String description = cel.getValue(DESCRIPTION);
                description = description == null ? article.getDescription() : description;
                description = description.replace("${date}", date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE)));

                final Integer devisId = Numbers.toInt(cel.getValue(ID));
                Devis devis = devisMap.get(devisId);
                if (devis == null) {
                    devis = new Devis(societe, devisId);
                            devis.setDate(date);
                            devis.setClient(client);
                    devisMap.put(devisId, devis);
                }
                devis.addLigne(new Ligne()
                        .setArticle(article)
                        .setQuantite(Numbers.toBigDecimal(cel.getValue(QUANTITE)))
                        .setDescription(description));
                devisRepository.save(devis);
            });

        } catch (IOException | ServiceException ex) {
            throw new GsException(ex);
        }
    }
    
    private static final String FACTURE = "Facture";
    private static final String DEVIS = "Devis";
    private static final String DATE = "date";
    private static final String QUANTITE = "quantite";
    private static final String ARTICLEREF = "articleref";
    private static final String CLIENTREF = "clientref";
    private static final String CLIENT = "Client";
    private static final String SOCIETE = "Societe";
    private static final String NAF = "naf";
    private static final String NUM_TVA_INTRACOM = "numTvaIntracom";
    private static final String SIRET = "siret";
    private static final String MAIL = "mail";
    private static final String WEB = "web";
    private static final String FAX = "fax";
    private static final String TEL = "tel";
    private static final String PAYS = "pays";
    private static final String VILLE = "ville";
    private static final String CODE_POSTAL = "codePostal";
    private static final String RUE3 = "rue3";
    private static final String RUE2 = "rue2";
    private static final String RUE1 = "rue1";
    private static final String LOGO = "logo";
    private static final String FORME_JURIDIQUE = "formeJuridique";
    private static final String NOM_COURT = "nomCourt";
    private static final String NOM = "nom";
    private static final String TVA_ = "tva";
    private static final String MONTANT = "montant";
    private static final String DESCRIPTION = "description";
    private static final String ARTICLE = "Article";
    private static final String ID = "ID";
    private static final String SOCIETEREF = "societeref";
    private static final String UNITE = "unite";
    private static final String CAPITAL = "capital";
    private static final String IBAN = "iban";
    private static final String BIC = "bic";
    private static final String DELAI_PAIEMENT = "delaiPaiement";

}
