package dev.coop.facturation.service;

import dev.coop.facturation.BdxIoInitializer;
import dev.coop.facturation.Facturation;
import dev.coop.facturation.format.pdf.PdfComposer;
import dev.coop.facturation.format.pdf.PdfGenerator;
import dev.coop.facturation.google.GoogleDrive;
import dev.coop.facturation.model.Devis;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.persistence.DevisRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.persistence.SocieteRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author lfo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Facturation.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpreadsheetImporterTest {

    @Autowired
    private SpreadsheetImporter importer;
    @Autowired
    private SocieteRepository societeRepository;
    @Autowired
    private FactureRepository factureRepository;
    @Autowired
    private DevisRepository devisRepository;
    @Autowired
    private PdfComposer pdfComposer;
    @Autowired
    private GoogleDrive googleDrive;

    @Test
    public void test() {
        importer.importAll("1S6hdN75AYQqdk9cfC2Ot6dORWZu6u3NfwWzUiPylUdU");

        final Societe bdxio = societeRepository.findByIdOrThrow(BdxIoInitializer.BDXIO);
        
        final List<Facture> factures = factureRepository.findBySociete(bdxio);
        final List<Devis> devis = devisRepository.findBySociete(bdxio);
        String parentId = "1-Owr1AUhBm9E00NdhiK0z_7A06J6WAmY";

        factures.forEach(facture -> {
            try {
                PdfGenerator pdfGenerator = pdfComposer.getComposer(bdxio);
                String fileName = PdfGenerator.getFileName(facture);

                if (googleDrive.getFile(fileName, parentId) == null) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    pdfGenerator.generate(facture, out);
                    googleDrive.upload(parentId, fileName, "application/pdf", out.toByteArray());
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

    }

}
