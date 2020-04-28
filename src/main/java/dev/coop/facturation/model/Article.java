package dev.coop.facturation.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Article extends HasSocieteCodeKey {

    private String description;
    private Montant montant;
    private Unite unite;
    private TVA tva;

    public Article(Societe societe, int codeValue) {
        super(societe, codeValue);
    }

    @Override
    public CodeFormatter.Prefix getCodePrefix() {
        return CodeFormatter.Prefix.AR;
    }

    public String getDescription() {
        return description;
    }

    public Article setDescription(String description) {
        this.description = description;
        return this;
    }

    public Montant getMontant() {
        return montant;
    }

    public Article setMontant(Montant montant) {
        this.montant = montant;
        return this;
    }

    public Unite getUnite() {
        return unite;
    }

    public Article setUnite(Unite unite) {
        this.unite = unite;
        return this;
    }

    public TVA getTva() {
        return tva;
    }

    public Article setTva(TVA tva) {
        this.tva = tva;
        return this;
    }

}
