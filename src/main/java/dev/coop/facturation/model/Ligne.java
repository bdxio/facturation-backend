package dev.coop.facturation.model;

import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;

@ToString
public class Ligne {

    private Article article;
    private String description;
    private Montant montantUnitaire;
    private Unite unite;
    private TVA tva;
    private BigDecimal quantite;

    public Article getArticle() {
        return article;
    }

    public Ligne setArticle(Article article) {
        this.article = article;
        return this;
    }

    public String getDescription() {
        return (description != null) ? description : article.getDescription();
    }

    public Ligne setDescription(String description) {
        this.description = description;
        return this;
    }

    public Montant getMontantUnitaire() {
        return montantUnitaire != null ? montantUnitaire : article.getMontant();
    }

    public Ligne setMontantUnitaire(Montant montant) {
        this.montantUnitaire = montant;
        return this;
    }

    public Unite getUnite() {
        return unite != null ? unite : article.getUnite();
    }

    public Ligne setUnite(Unite unite) {
        this.unite = unite;
        return this;
    }

    public TVA getTva() {
        return tva != null ? tva : article.getTva();
    }

    public Ligne setTva(TVA tva) {
        this.tva = tva;
        return this;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public Ligne setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
        return this;
    }
    
    public Ligne setQuantite(int quantite) {
        return setQuantite(new BigDecimal(quantite));
    }
    public Montant getMontantHT() {
        return getMontantUnitaire().multiply(quantite);
    }
    
    public Montant getMontantTtc() {
        return getMontantHT().add(getMontantTva());
    }
    
    public Montant getMontantTva() {
        return getMontantHT().multiply(getTva().getTaux());
    }
    
}
