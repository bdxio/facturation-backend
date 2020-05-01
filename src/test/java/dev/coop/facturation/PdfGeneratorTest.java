package dev.coop.facturation;

import dev.coop.facturation.format.pdf.PdfGenerator;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.SocieteCodeKey;
import dev.coop.facturation.persistence.FactureRepository;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Facturation.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PdfGeneratorTest {
    
    @Autowired
    private PdfGenerator.Default pdfGenerator;
    @Autowired
    private DevcoopInitializer dataInitializer;
    @Autowired
    private FactureRepository factureRepository;
    
    @Test
    public void testGeneration() throws IOException {
        dataInitializer.doInit();
        Facture facture = factureRepository.findByIdOrThrow(SocieteCodeKey.create(DevcoopInitializer.DEVCOOP, 132));
        pdfGenerator.generate(facture, new File("target"));
    }
}
