package dev.coop.facturation.service;

import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import dev.coop.facturation.model.*;
import dev.coop.facturation.persistence.ArticleRepository;
import dev.coop.facturation.persistence.ClientRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.persistence.SocieteRepository;
import dev.coop.facturation.google.GoogleSheets;
import dev.coop.facturation.google.GsException;
import dev.coop.facturation.persistence.DevisRepository;
import dev.coop.facturation.tools.Dates;
import dev.coop.facturation.tools.Numbers;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tomcat.util.http.fileupload.IOUtils;
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
        final Spreadsheet spreadsheet = googleSheets.fetchSpreadsheet(worksheetId);
        importSocietes(spreadsheet);
        importClients(spreadsheet);
        importArticles(spreadsheet);
        importFactures(spreadsheet);
        importDevis(spreadsheet);
    }

    public void importSocietes(Spreadsheet spreadsheet) {
        final ValueRange valueRange = googleSheets.findSheetDataBySheetTitle(spreadsheet, SOCIETE);

        valueRange.getValues().stream()
                .skip(1)
                .filter(objects -> !objects.isEmpty())
                .forEach(cells -> {
                    String nomCourt = extractStringValue(NOM_COURT, cells, SOCIETE_COLUMNS);
                    if (nomCourt == null || nomCourt.isEmpty()) {
                        return;
                    }
                    final Societe societe = new Societe();
                    societe.setNom(extractStringValue(NOM, cells, SOCIETE_COLUMNS))
                            .setNomCourt(nomCourt)
                            .setFormeJuridique(extractStringValue(FORME_JURIDIQUE, cells, SOCIETE_COLUMNS))
                            .setDescription(extractStringValue(DESCRIPTION, cells, SOCIETE_COLUMNS))
                            .setAdresse(importAdresse(cells, SOCIETE_COLUMNS))
                            .setTel(extractStringValue(TEL, cells, SOCIETE_COLUMNS))
                            .setFax(extractStringValue(FAX, cells, SOCIETE_COLUMNS))
                            .setWeb(extractStringValue(WEB, cells, SOCIETE_COLUMNS))
                            .setMail(extractStringValue(MAIL, cells, SOCIETE_COLUMNS))
                            .setSiret(extractStringValue(SIRET, cells, SOCIETE_COLUMNS))
                            .setNaf(extractStringValue(NAF, cells, SOCIETE_COLUMNS))
                            .setNumTvaIntracom(extractStringValue(NUM_TVA_INTRACOM, cells, SOCIETE_COLUMNS))
                            .setBic(extractStringValue(BIC, cells, SOCIETE_COLUMNS))
                            .setIban(extractStringValue(IBAN, cells, SOCIETE_COLUMNS))
                            .setCapital(new Montant(extractBigDecimalValue(CAPITAL, cells, SOCIETE_COLUMNS)))
                            .setDelaiPaiement(extractIntegerValue(DELAI_PAIEMENT, cells, SOCIETE_COLUMNS));

                    String logoUrl = extractStringValue(LOGO, cells, SOCIETE_COLUMNS);

                    Preconditions.checkNotNull(logoUrl, "Logo url is null");
                    try (InputStream logoInput = new URL(logoUrl).openStream()) {
                        societe.setLogo(ByteStreams.toByteArray(logoInput));
                    } catch (IOException ex) {
                        throw new GsException(ex);
                    }
                    societeRepository.save(societe);
                });
    }

    private static Adresse importAdresse(final List<Object> cells, Map<String, Integer> columns) {
        return new Adresse()
                .setRue1(extractStringValue(RUE1, cells, columns))
                .setRue2(extractStringValue(RUE2, cells, columns))
                .setRue3(extractStringValue(RUE3, cells, columns))
                .setCodePostal(extractStringValue(CODE_POSTAL, cells, columns))
                .setVille(extractStringValue(VILLE, cells, columns))
                .setPays(extractStringValue(PAYS, cells, columns));
    }

    public void importArticles(Spreadsheet spreadsheet) {
        final ValueRange valueRange = googleSheets.findSheetDataBySheetTitle(spreadsheet, ARTICLE);

        valueRange.getValues().stream()
                .skip(1)
                .filter(objects -> !objects.isEmpty())
                .forEach(cells -> {
                    String idValue = extractStringValue(ID, cells, ARTICLE_COLUMNS);
                    if (idValue == null) {
                        return;
                    }

                    Societe societe = societeRepository.findByIdOrThrow(extractStringValue(SOCIETEREF, cells, ARTICLE_COLUMNS));
                    int id = Numbers.toInt(idValue);

                    Article article = new Article(societe, id)
                            .setDescription(extractStringValue(DESCRIPTION, cells, ARTICLE_COLUMNS))
                            .setMontant(new Montant(extractBigDecimalValue(MONTANT, cells, ARTICLE_COLUMNS)))
                            .setTva(TVA.valueOf(extractStringValue(TVA_, cells, ARTICLE_COLUMNS)));

                    String unite = extractStringValue(UNITE, cells, ARTICLE_COLUMNS);
                    if (!Strings.isNullOrEmpty(unite)) {
                        article.setUnite(Unite.valueOf(unite));
                    }
                    articleRepository.save(article);
                });
    }

    public void importClients(Spreadsheet spreadsheet) {
        final ValueRange valueRange = googleSheets.findSheetDataBySheetTitle(spreadsheet, CLIENT);

        valueRange.getValues().stream()
                .skip(1)
                .filter(objects -> !objects.isEmpty())
                .forEach(cells -> {
                    Societe societe = societeRepository.findByIdOrThrow(extractStringValue(SOCIETEREF, cells, CLIENT_COLUMNS));

                    int id = extractIntegerValue(ID, cells, CLIENT_COLUMNS);

                    Client client = new Client(societe, id)
                            .setAdresse(importAdresse(cells, CLIENT_COLUMNS))
                            .setNom(extractStringValue(NOM, cells, CLIENT_COLUMNS))
                            .setNomCourt(extractStringValue(NOM_COURT, cells, CLIENT_COLUMNS))
                            .setNumTVAIntracom(extractStringValue(NUM_TVA_INTRACOM, cells, CLIENT_COLUMNS));
                    clientRepository.save(client);
                });
    }

    public void importFactures(Spreadsheet spreadsheet) {
        final ValueRange valueRange = googleSheets.findSheetDataBySheetTitle(spreadsheet, FACTURE);
        final Map<Integer, Facture> factureMap = new HashMap<>();

        valueRange.getValues().stream()
                .skip(1)
                .filter(objects -> !objects.isEmpty())
                .forEach(cells -> {
                    final Societe societe = societeRepository.findByIdOrThrow(extractStringValue(SOCIETEREF, cells, FACTURE_COLUMNS));
                    final Client client = clientRepository.findByIdOrThrow(SocieteCodeKey.create(societe, extractIntegerValue(CLIENTREF, cells, FACTURE_COLUMNS)));
                    final Article article = articleRepository.findByIdOrThrow(SocieteCodeKey.create(societe, extractIntegerValue(ARTICLEREF, cells, FACTURE_COLUMNS)));
                    final LocalDate date = extractLocalDateValue(DATE, cells, FACTURE_COLUMNS);
                    final Integer acompte = extractIntegerValue(ACOMPTE, cells, FACTURE_COLUMNS);

                    String description = extractStringValue(DESCRIPTION, cells, FACTURE_COLUMNS);
                    description = description == null ? article.getDescription() : description;
                    description = description.replace("${date}", date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE)));
                    if (acompte != null && acompte > 0) {
                        description = String.format("Acompte de %d%% - %s", acompte, description);
                    }
                    final Integer factureId = extractIntegerValue(ID, cells, FACTURE_COLUMNS);
                    Facture facture = factureMap.get(factureId);
                    if (facture == null) {
                        facture = new Facture(societe, factureId)
                                .setDate(date)
                                .setClient(client)
                                .setAcompte(acompte);
                        factureMap.put(factureId, facture);
                    }
                    facture.addLigne(new Ligne()
                            .setArticle(article)
                            .setQuantite(extractBigDecimalValue(QUANTITE, cells, FACTURE_COLUMNS))
                            .setDescription(description));
                    factureRepository.save(facture);
                });
    }


    public void importDevis(Spreadsheet spreadsheet) {
        final ValueRange valueRange = googleSheets.findSheetDataBySheetTitle(spreadsheet, DEVIS);
        final Map<Integer, Devis> devisMap = new HashMap<>();

        valueRange.getValues().stream()
                .skip(1)
                .filter(objects -> !objects.isEmpty())
                .forEach(cells -> {
                    final Societe societe = societeRepository.findByIdOrThrow(extractStringValue(SOCIETEREF, cells, DEVIS_COLUMNS));
                    final Client client = clientRepository.findByIdOrThrow(SocieteCodeKey.create(societe, extractIntegerValue(CLIENTREF, cells, DEVIS_COLUMNS)));
                    final Article article = articleRepository.findByIdOrThrow(SocieteCodeKey.create(societe, extractIntegerValue(ARTICLEREF, cells, DEVIS_COLUMNS)));
                    final LocalDate date = extractLocalDateValue(DATE, cells, DEVIS_COLUMNS);

                    String description = extractStringValue(DESCRIPTION, cells, DEVIS_COLUMNS);
                    description = description == null ? article.getDescription() : description;
                    description = description.replace("${date}", date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE)));

                    final Integer devisId = extractIntegerValue(ID, cells, DEVIS_COLUMNS);
                    Devis devis = devisMap.get(devisId);
                    if (devis == null) {
                        devis = new Devis(societe, devisId);
                        devis.setDate(date);
                        devis.setClient(client);
                        devisMap.put(devisId, devis);
                    }
                    devis.addLigne(new Ligne()
                            .setArticle(article)
                            .setQuantite(extractBigDecimalValue(QUANTITE, cells, DEVIS_COLUMNS))
                            .setDescription(description));
                    devisRepository.save(devis);
                });
    }

    private static String extractStringValue(String key, List<Object> cells, Map<String, Integer> columns) {
        Integer columnIndex = Preconditions.checkNotNull(columns.get(key), String.format("Cannot find index for column %s", key));

        if (cells.size() <= columnIndex) {
            return null;
        }

        return (String) cells.get(columnIndex);
    }

    private static BigDecimal extractBigDecimalValue(String key, List<Object> cells, Map<String, Integer> columns) {
        return Numbers.toBigDecimal(extractStringValue(key, cells, columns));
    }

    private static Integer extractIntegerValue(String key, List<Object> cells, Map<String, Integer> columns) {
        String stringValue = extractStringValue(key, cells, columns);
        if (stringValue == null || stringValue.isEmpty()) {
            return 0;
        }
        return Numbers.toInt(stringValue);
    }

    private static LocalDate extractLocalDateValue(String key, List<Object> cells, Map<String, Integer> columns) {
        return Dates.parse(extractStringValue(key, cells, columns));
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
    private static final String ACOMPTE = "acompte";

    private static final Map<String, Integer> SOCIETE_COLUMNS = ImmutableMap.<String, Integer>builder()
            .put(NOM, 0)
            .put(NOM_COURT, 1)
            .put(DESCRIPTION, 2)
            .put(FORME_JURIDIQUE, 3)
            .put(RUE1, 4)
            .put(RUE2, 5)
            .put(RUE3, 6)
            .put(CODE_POSTAL, 7)
            .put(VILLE, 8)
            .put(PAYS, 9)
            .put(TEL, 10)
            .put(FAX, 11)
            .put(WEB, 12)
            .put(MAIL, 13)
            .put(SIRET, 14)
            .put(NAF, 15)
            .put(NUM_TVA_INTRACOM, 16)
            .put(CAPITAL, 17)
            .put(IBAN, 18)
            .put(BIC, 19)
            .put(LOGO, 20)
            .put(DELAI_PAIEMENT, 21)
            .build();

    private static final Map<String, Integer> CLIENT_COLUMNS = ImmutableMap.<String, Integer>builder()
            .put(ID, 0)
            .put(SOCIETEREF, 1)
            .put(NOM, 2)
            .put(NOM_COURT, 3)
            .put(RUE1, 4)
            .put(RUE2, 5)
            .put(RUE3, 6)
            .put(CODE_POSTAL, 7)
            .put(VILLE, 8)
            .put(PAYS, 9)
            .put(NUM_TVA_INTRACOM, 10)
            .build();

    private static final Map<String, Integer> ARTICLE_COLUMNS = ImmutableMap.<String, Integer>builder()
            .put(ID, 0)
            .put(SOCIETEREF, 1)
            .put(DESCRIPTION, 2)
            .put(MONTANT, 3)
            .put(TVA_, 4)
            .put(UNITE, 5)
            .build();

    private static final Map<String, Integer> FACTURE_COLUMNS = ImmutableMap.<String, Integer>builder()
            .put(ID, 0)
            .put(SOCIETEREF, 1)
            .put(ARTICLEREF, 2)
            .put(CLIENTREF, 3)
            .put(DATE, 4)
            .put(QUANTITE, 5)
            .put(DESCRIPTION, 6)
            .put(ACOMPTE, 7)
            .build();

    private static final Map<String, Integer> DEVIS_COLUMNS = ImmutableMap.<String, Integer>builder()
            .put(ID, 0)
            .put(SOCIETEREF, 1)
            .put(ARTICLEREF, 2)
            .put(CLIENTREF, 3)
            .put(DATE, 4)
            .put(QUANTITE, 5)
            .put(DESCRIPTION, 6)
            .build();
}
