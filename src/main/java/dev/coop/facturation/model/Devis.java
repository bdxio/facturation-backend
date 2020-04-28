package dev.coop.facturation.model;

/**
 *
 * @author lfo
 */
public class Devis extends Facture {

    public Devis(Societe societe, int codeValue) {
        super(societe, codeValue);
    }

    @Override
    public CodeFormatter.Prefix getCodePrefix() {
        return CodeFormatter.Prefix.DE;
    }
    
}
