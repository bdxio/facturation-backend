package dev.coop.facturation.model;

import org.springframework.data.annotation.PersistenceConstructor;

import java.util.List;

/**
 *
 * @author lfo
 */
public class Devis extends Facture {

    @PersistenceConstructor
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
