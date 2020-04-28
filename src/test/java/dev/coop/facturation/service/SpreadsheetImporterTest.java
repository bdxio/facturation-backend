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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author lfo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Facturation.class)
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
        importer.importAll("1chuyY5g2sU7kWEvZkZb8jygKda7Pw9aTNTYPcXntWc8");

        final Societe bdxio = societeRepository.findOne(BdxIoInitializer.BDXIO);

        
        final List<Facture> factures = factureRepository.findBySociete(bdxio);
        final List<Devis> devis = devisRepository.findBySociete(bdxio);
        String parentId = "0B9IB8KKHBgPeNnF1bFc2Z2dTczQ";
//        final File target = new File("target");
        factures.stream().forEach(facture -> {
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
