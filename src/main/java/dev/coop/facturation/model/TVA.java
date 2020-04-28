package dev.coop.facturation.model;

import java.math.BigDecimal;

/**
 *
 * @author lforet
 */
public enum TVA {

    CURRENT_20(new BigDecimal(20).divide(new BigDecimal(100))), 
    NONE(new BigDecimal(0)) ;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100); 
    private final BigDecimal taux;
    
    private TVA(BigDecimal taux) {
        this.taux = taux;
    }

    public BigDecimal getTaux() {
        return taux;
    }

    public BigDecimal getPercentage() {
        return taux.multiply(ONE_HUNDRED);
    }
    
//    public final static TVA CURRENT_20 = new TVA(new BigDecimal(20).divide(ONE_HUNDRED)); 
    
//    public final static TVA NONE = new TVA(new BigDecimal(0)); 
    
    
    public Montant getTTC(Montant ht) {
        return ht.multiply(getTaux());
    }
}
