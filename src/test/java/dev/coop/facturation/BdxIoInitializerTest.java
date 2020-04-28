package dev.coop.facturation;

import dev.coop.facturation.format.pdf.PdfGenerator;
import dev.coop.facturation.model.Devis;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.persistence.DevisRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.persistence.SocieteRepository;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
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
public class BdxIoInitializerTest {
    
    @Autowired
    private BdxIoInitializer bdxIoInitializer;
    @Autowired
    private SocieteRepository societeRepository;
    @Autowired
    private FactureRepository factureRepository;
    @Autowired
    private DevisRepository devisRepository;
    
    @Autowired
    private PdfGenerator.ForAssociation pdfGenerator;
    
    @Test
    public void doInit() throws Exception {
        bdxIoInitializer.doInit();
        final Societe bdxio = societeRepository.findOne(BdxIoInitializer.BDXIO);
        
        final List<Facture> factures = factureRepository.findBySociete(bdxio);
        final File target = new File("target");
        factures.stream().forEach(facture -> {
            try {
                pdfGenerator.generate(facture, target);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        
        final Stream<Devis> devisStream = devisRepository.findBySociete(bdxio).stream();
        devisStream.forEach(devis -> {
            try {
                pdfGenerator.generate(devis, target);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            
        }); 
        
    }
}
