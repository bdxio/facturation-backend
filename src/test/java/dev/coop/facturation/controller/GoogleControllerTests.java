package dev.coop.facturation.controller;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.common.io.BaseEncoding;
import dev.coop.facturation.configuration.GoogleConfiguration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations = "classpath:/google.properties") // use application.yml in test resources.
/*
  TODO:
  - cleanup le test :-)
  - il y a des warn PDFBox indiquant que l'on ne ferme pas le document : "org.apache.pdfbox.cos.COSDocument        : Warning: You did not close a PDF Document"



  Actuellement le test ne peut pas être exécuté car lors de la création du contexte Spring la configuration Google échoue.
  Les solutions possibles sont :
  - fournir une propriété google.account correctement valorisée (DONE)
  - créer une configuration de test créant le bean en le marquant @Primary
  - utiliser un mock bean

  Aucune des solutions ne semble être applicable car c'est une classe de configuration que l'on ne souhaite pas instancier.
  Ceci étant dit l'utilisation d'une classe de configuration pour ce qui est fait semble être complètement à côté de la plaque : on utilise cette classe comme un bean que l'on va ensuite
  injecter dans le "client" GoogleDrive, pourquoi ne pas directement faire cela dans le service GoogleSheets.

  NOTE : toute l'architecture est à revoir :/
 */
class GoogleControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GoogleConfiguration googleConfiguration;

    private Sheets sheetsService;

    private Drive driveService;

    @BeforeEach
    void setUp() {
        sheetsService = new Sheets.Builder(googleConfiguration.getTransport(), googleConfiguration.getJsonFactory(), googleConfiguration.getHttpRequestInitializer())
                .setApplicationName(GoogleConfiguration.APPLICATION_NAME)
                .build();
        driveService = new Drive.Builder(googleConfiguration.getTransport(), googleConfiguration.getJsonFactory(), googleConfiguration.getHttpRequestInitializer())
                .setApplicationName(GoogleConfiguration.APPLICATION_NAME)
                .build();
    }

    @Test
    void shouldGeneratePDFsFromSpreadsheet() {
        String spreadsheetId = null;
        String folderId = null;

        try {
            spreadsheetId = createSpreadsheet();
            System.out.println("Create spreadsheet with ID: " + spreadsheetId);
            var response = restTemplate.getForEntity("/google/importInMongo/{worksheetId}", Void.class, spreadsheetId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            var folderMetadata = new File();
            folderMetadata.setName(UUID.randomUUID().toString());
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderId = driveService.files().create(folderMetadata).setFields("id").execute().getId();
            response = restTemplate.getForEntity("/google/generateInDrive/{societeName}/{folderId}", Void.class, "BDXIO", folderId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            var list = driveService.files().list().setQ(String.format("parents in '%s'", folderId)).execute();
            var generatedPDFs = list.getFiles().stream()
                    .filter(file -> file.getName().endsWith(".pdf"))
                    .map(file -> new GeneratedPDF(file.getName(), getPDFHash(file)))
                    .collect(Collectors.toList());
            assertThat(generatedPDFs).containsExactlyInAnyOrder(
                    new GeneratedPDF("DE00210001-BDXIO-Client1.pdf", "d1d97ddfa7ad8bfa2535a41f71beb63d2511b8a538e49280f0637884bd42d62f"),
                    new GeneratedPDF("FA00210002-BDXIO-Client2.pdf", "524017f351090a9a53c1ffe3029394d0677a497b76dfb09b840f01dcdee92658"),
                    new GeneratedPDF("FA00210001-BDXIO-Client1.pdf", "d45af87e152fb2df42d6af0dd99463e484d347c617431b288f9f35403e78efcf")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            deleteFile(folderId);
            deleteFile(spreadsheetId);
        }
    }

    private String createSpreadsheet() {
        try {
            // TODO: REMOVE ME !
            System.out.println("DRIVE FILES:");
            driveService.files().list().execute().getFiles().forEach(System.out::println);
            deleteFile("1OO6T2K0oE74bvqXDbspPq66n-P7GHDmMBXfRdbUh2hs");
            System.out.println("============");

            var spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle("Facturation"));
            var spreadsheetId = sheetsService.spreadsheets().create(spreadsheet).setFields("spreadsheetId").execute().getSpreadsheetId();

            var requests = Stream.of("Societe", "Client", "Article", "Facture", "Devis")
                    .map(title -> new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle(title))))
                    .collect(Collectors.toList());
            // Default sheet id is always 0.
            requests.add(new Request().setDeleteSheet(new DeleteSheetRequest().setSheetId(0)));
            sheetsService.spreadsheets().batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests)).execute();

            var societeSheet = new ValueRange()
                    .setValues(List.of(
                            List.of("nom", "nomCourt", "description", "formeJuridique", "rue1", "rue2", "rue3", "codePostal", "ville", "pays", "tel", "fax", "web", "mail", "siret", "naf", "numTvaIntracom", "capital", "iban", "bic", "logo", "delaiPaiement"),
                            List.of("Bordeaux Developer eXperience", "BDXIO", "BDX.IO", "Association loi 1901", "Appartement 310 - Villa Morelle", "13 rue Jean Pommiès", "", "33520", "Bruges", "FRANCE", "", "", "https://www.bdx.io", "team@bdx.io", "811 910 520 00014", "", "", "0", "FR76 1558 9335 7307 3463 7354 089", "CMBRFR2BARK", "https://docs.google.com/uc?id=18sFY1m15r9UwPZvOZ_GnXs43IWnmJvR0", "0")
                    ));
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, "Societe", societeSheet)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            var clientSheet = new ValueRange()
                    .setValues(List.of(
                            List.of("ID", "Societe_REF", "nom", "nomCourt", "rue1", "rue2", "rue3", "codePostal", "ville", "pays", "numTvaIntracom"),
                            List.of("1", "BDXIO", "Client 1", "Client1", "1 rue du Centre", "", "", "33000", "Bordeaux", "", ""),
                            List.of("2", "BDXIO", "Client 2", "Client2", "2 rue du Centre", "Immeuble A", "Bâtiment 2", "F-33000", "Bordeaux", "", "")
                    ));
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, "Client", clientSheet)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            var articleSheet = new ValueRange()
                    .setValues(List.of(
                            List.of("ID", "Societe_REF", "description", "montant", "tva", "unite"),
                            List.of("1", "BDXIO", "Sponsoring Gold", "4096", "NONE", ""),
                            List.of("2", "BDXIO", "Sponsoring Silver", "2048", "NONE", ""),
                            List.of("3", "BDXIO", "Sponsoring Bronze", "1024", "NONE", "")
                    ));
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, "Article", articleSheet)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            var factureSheet = new ValueRange()
                    .setValues(List.of(
                            List.of("ID", "Societe_REF", "Article_REF", "Client_REF", "date", "quantite", "description", "Commentaire"),
                            List.of("210001", "BDXIO", "1", "1", "01/01/2021", "1", "Sponsoring catégorie Gold", "Sponsoring Gold CLIENT 1"),
                            List.of("210002", "BDXIO", "2", "2", "31/12/2021", "2", "Sponsoring catégorie Silver", "Sponsoring Silver CLIENT 2")
                    ));
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, "Facture", factureSheet)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            var devisSheet = new ValueRange()
                    .setValues(List.of(
                            List.of("ID", "Societe_REF", "Article_REF", "Client_REF", "date", "quantite", "description", "Commentaire"),
                            List.of("210001", "BDXIO", "1", "1", "30/06/2021", "3", "Sponsoring catégorie Bronze", "Sponsoring Bronze CLIENT 1")
                    ));
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, "Devis", devisSheet)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

            return spreadsheetId;
        } catch (IOException e) {
            throw new RuntimeException("Unable to create spreadsheet", e);
        }
    }

    private String getPDFHash(File file) {
        try {
            var outputStream = new ByteArrayOutputStream();
            driveService.files().get(file.getId()).executeMediaAndDownloadTo(new FileOutputStream(file.getName()));
            driveService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);


            try (var doc = PDDocument.load(outputStream.toByteArray())) {
                if (doc.getNumberOfPages() > 1) {
                    throw new RuntimeException(String.format("PDF should only have one page, %s got %d pages", file.getName(), doc.getNumberOfPages()));
                }

                var renderer = new PDFRenderer(doc);
                var image = renderer.renderImageWithDPI(0, 300);
                var digest = MessageDigest.getInstance("SHA-256");
                var bytesStream = new ByteArrayOutputStream();
                var imageStream = new DigestOutputStream(bytesStream, digest);
                ImageIO.write(image, "jpeg", imageStream);

                return BaseEncoding.base16().lowerCase().encode(digest.digest());
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to create SHA-256 message digest", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to download file " + file, e);
        }
    }

    private void deleteFile(String id) {
        if (id == null) {
            return;
        }

        try {
            driveService.files().delete(id).execute();
        } catch (IOException ignored) {
        }
    }
}
