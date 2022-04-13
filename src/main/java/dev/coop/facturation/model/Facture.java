package dev.coop.facturation.model;

import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lforet
 */
@ToString
public class Facture extends HasSocieteCodeKey {

    private Client client;
    private LocalDate date;
    private final List<Ligne> lignes;
    private final List<Ligne> ristournes;

    public Facture(SocieteCodeKey id, List<Ligne> lignes, List<Ligne> ristournes) {
        super(id);
        this.lignes = lignes;
        this.ristournes = ristournes;
    }

    public Facture(Societe societe, int codeValue) {
        super(societe, codeValue);
        this.lignes = new ArrayList<>();
        this.ristournes = new ArrayList<>();
    }

    @Override
    public void updateCode() {
        this.setCode(this.getCodeValue());
        for(Ligne l : lignes){
            l.updateArticleCode();
        }
        for(Ligne l : ristournes){
            l.updateArticleCode();
        }
    }

    @Override
    public CodeFormatter.Prefix getCodePrefix() {
        return CodeFormatter.Prefix.FA;
    }

    public Client getClient() {
        return client;
    }

    public Facture setClient(Client client) {
        this.client = client;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDate getDateEcheance() {
        return date.plusDays(getSociete().getDelaiPaiement());
    }
    
    public Facture setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public List<Ligne> getLignes() {
        return lignes;
    }

    public Facture addLigne(Ligne ligne) {
        lignes.add(ligne);
        return this;
    }

    public List<Ligne> getRistournes() {
        return ristournes;
    }

    public Facture addRistourne(Ligne ristourne) {
        ristournes.add(ristourne);
        return this;
    }

    public Montant getTotalHT() {
        Montant total = new Montant(BigDecimal.ZERO);
        for (Ligne ligne : lignes) {
            total = total.add(ligne.getMontantHT());
        }
        return total;
    }

    public Montant getTotalTtc() {
        Montant total = new Montant(BigDecimal.ZERO);
        for (Ligne ligne : lignes) {
            total = total.add(ligne.getMontantTtc());
        }
        return total;
    }

    public Montant getTotalTva() {
        Montant total = new Montant(BigDecimal.ZERO);
        for (Ligne ligne : lignes) {
            total = total.add(ligne.getMontantTva());
        }
        return total;
    }

}
