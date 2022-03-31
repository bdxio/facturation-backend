package dev.coop.facturation.model;

import lombok.ToString;

import java.util.List;

/**
 *
 * @author lfo
 */
@ToString
public class Devis extends Facture {


    public Devis(SocieteCodeKey id, List<Ligne> lignes, List<Ligne> ristournes) {
        super(id, lignes, ristournes);
    }

    public Devis(Societe societe, int codeValue) {
        super(societe, codeValue);
    }

    @Override
    public CodeFormatter.Prefix getCodePrefix() {
        return CodeFormatter.Prefix.DE;
    }
    
}
