package dev.coop.facturation;

import dev.coop.facturation.model.Article;
import dev.coop.facturation.model.Client;
import dev.coop.facturation.model.Devis;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;

/**
 *
 * @author lfo
 */
public class ObjectFactory {

    private final Societe societe;

    public ObjectFactory(Societe societe) {
        this.societe = societe;
    }
    
    public Client createClient(int code) {
        return new Client(societe, code);
    }
    
    public Article createArticle(int code) {
        return new Article(societe, code);
    }
    
    public Facture createFacture(int code) {
        return new Facture(societe, code);
    }
    
    public Devis createDevis(int code) {
        return new Devis(societe, code);
    }
}
