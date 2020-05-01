package dev.coop.facturation.controller;

import dev.coop.facturation.format.pdf.PdfComposer;
import dev.coop.facturation.format.pdf.PdfGenerator;
import dev.coop.facturation.google.GoogleDrive;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.persistence.DevisRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.persistence.SocieteRepository;
import dev.coop.facturation.service.SpreadsheetImporter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author lfo
 */
@RestController
@RequestMapping("google")
public class GoogleController {

    @Autowired
    private SpreadsheetImporter importer;
    @Autowired
    private SocieteRepository societeRepository;
    @Autowired
    private FactureRepository factureRepository;
    @Autowired
    private DevisRepository devisRepository;
    @Autowired
    private PdfComposer composer;
    @Autowired
    private GoogleDrive googleDrive;

    @RequestMapping("importInMongo/{worksheetId}")
    public void importSheet(@PathVariable String worksheetId) {
        societeRepository.deleteAll();
        factureRepository.deleteAll();
        importer.importAll(worksheetId);
    }

    @RequestMapping("generateInDrive/{societeName}/{folderId}")
    public void generateInDrive(@PathVariable String societeName, @PathVariable String folderId) {
        Societe societe = societeRepository.findById(societeName)
                .orElseThrow(() -> new IllegalStateException(String.format("La société avec le nom %s est inconnue", societeName)));

        List<Facture> factures = factureRepository.findBySociete(societe);
        factures.addAll(devisRepository.findBySociete(societe));
        factures.forEach(facture -> {
            PdfGenerator generator = composer.getComposer(societe);
            String fileName = PdfGenerator.getFileName(facture);
            try {
                if (googleDrive.getFile(fileName, folderId) == null) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    generator.generate(facture, out);
                    googleDrive.upload(folderId, fileName, "application/pdf", out.toByteArray());
                }
            } catch (IOException io) {
                throw new RuntimeException(io);
            }
        });
    }

}
