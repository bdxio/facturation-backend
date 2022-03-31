package dev.coop.facturation.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author lforet
 */
public class Montant {

    public enum Monnaie {
        EUR("€");

        Monnaie(String representation) {
            this.representation = representation;
        }
        
        private String representation;

        public String getRepresentation() {
            return representation;
        }
        
    }
    
    private static NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    
    private BigDecimal valeur;
    private final Monnaie monnaie = Monnaie.EUR;

    private Montant() {
    }
       
    public Montant(int valeur) {
        this(new BigDecimal(valeur));
    }
    
    public Montant(String valeur) {
        this(new BigDecimal(valeur));
    }

    public Montant(BigDecimal valeur) {
        this.valeur = valeur == null ? BigDecimal.ZERO : valeur;
        this.valeur.setScale(2,RoundingMode.UP);
    }

    public BigDecimal getValeur() {
        return valeur == null ? BigDecimal.ZERO : valeur;
    }

    public Monnaie getMonnaie() {
        return monnaie;
    }
    
    public Montant add(Montant montant) {
        return new Montant(getValeur().add(montant.getValeur()));
    }
    
    public Montant substract(Montant montant) {
        return new Montant(getValeur().subtract(montant.valeur));
    }
    
    public Montant multiply(BigDecimal number) {
        return new Montant(getValeur().multiply(number == null ? BigDecimal.ZERO : number));
    }

    @Override
    public String toString() {
        return formatter.format(getValeur());
    }
    
    
}
