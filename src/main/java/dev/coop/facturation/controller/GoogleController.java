package dev.coop.facturation.controller;

import dev.coop.facturation.format.pdf.PdfComposer;
import dev.coop.facturation.format.pdf.PdfGenerator;
import dev.coop.facturation.google.GoogleDrive;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.persistence.ArticleRepository;
import dev.coop.facturation.persistence.ClientRepository;
import dev.coop.facturation.persistence.DevisRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.persistence.SocieteRepository;
import dev.coop.facturation.service.SpreadsheetImporter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author lfo
 */
@Slf4j
@RestController
@RequestMapping("google")
public class GoogleController {

    @Autowired
    private SpreadsheetImporter importer;
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
    @Autowired
    private PdfComposer composer;
    @Autowired
    private GoogleDrive googleDrive;

    @RequestMapping("importInMongo/{worksheetId}")
    public void importSheet(@PathVariable String worksheetId) {
        log.info("google/importInMongo/{}", worksheetId);
        societeRepository.deleteAll();
        clientRepository.deleteAll();
        articleRepository.deleteAll();
        factureRepository.deleteAll();
        devisRepository.deleteAll();
        importer.importAll(worksheetId);
    }

    @RequestMapping("generateInDrive/{societeName}/{folderId}")
    public void generateInDrive(@PathVariable String societeName, @PathVariable String folderId) {
        try{
            List<Facture> factures = getFacturesBySociety(societeName);
            Map<String, byte[]> facturesToUpload = createFileToUpload(folderId, factures);
            uploadFileToDrive(folderId, facturesToUpload);
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    private void uploadFileToDrive(final String folderId, final Map<String, byte[]> facturesToUpload) throws IOException {
        for(Map.Entry<String, byte[]> facture : facturesToUpload.entrySet()){
            googleDrive.upload(folderId, facture.getKey(), "application/pdf", facture.getValue());
            log.info("{} - Write {}", folderId, facture.getKey());
        }
    }

    Map<String, byte[]> createFileToUpload(final String folderId, final List<Facture> factures) {
        Map<String, byte[]> facturesToUpload = new HashMap<>();
        for (Facture facture : factures) {
            try {
                String fileName = PdfGenerator.getFileName(facture);
                if (googleDrive.getFile(fileName, folderId) == null) {
                    byte[] file = createFile(facture);
                    facturesToUpload.put(fileName, file);

                }
            } catch (IOException io) {
                throw new RuntimeException(io);
            }
        }
        return facturesToUpload;
    }

    List<Facture> getFacturesBySociety(final String societeName) {
        Societe societe = societeRepository.findByIdOrThrow(societeName);

        List<Facture> factures = factureRepository.findBySociete(societe);
        factures.addAll(devisRepository.findBySociete(societe));
        return factures;
    }

    byte[] createFile(final Facture facture) {
        PdfGenerator generator = composer.getComposer(facture.getSociete());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generate(facture, out);
        byte[] file = out.toByteArray();
        return file;
    }


    /**
     * For testing purpose!
     * @param googleDrive
     */
    void setGoogleDrive(final GoogleDrive googleDrive) {
        this.googleDrive = googleDrive;
    }
}
