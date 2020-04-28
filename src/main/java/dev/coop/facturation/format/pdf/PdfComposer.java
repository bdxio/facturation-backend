package dev.coop.facturation.format.pdf;

import dev.coop.facturation.model.Societe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author lfo
 */
@Component
public class PdfComposer {

    @Autowired
    private PdfGenerator.Default defaultGenerator;
    @Autowired 
    private PdfGenerator.ForAssociation associationGenerator;
    
    public PdfGenerator getComposer(Societe societe) {
        if (societe.getFormeJuridique().toLowerCase().contains("association")) {
            return associationGenerator;
        }
        return defaultGenerator;
    }
}
