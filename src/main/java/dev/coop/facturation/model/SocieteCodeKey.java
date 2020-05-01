package dev.coop.facturation.model;

import java.io.Serializable;

/**
 *
 * @author lfo
 */
public class SocieteCodeKey implements Serializable {

    private String societe;
    private int codeValue;

    private SocieteCodeKey() {
    }

    protected SocieteCodeKey(String societe, int codeValue) {
        this.societe = societe;
        this.codeValue = codeValue;
    }

    protected SocieteCodeKey(Societe societe, int codeValue) {
        this.societe = societe.getNomCourt();
        this.codeValue = codeValue;
    }

    public int getCodeValue() {
        return codeValue;
    }

    public String getSociete() {
        return societe;
    }

    public void setSociete(String societe) {
        this.societe = societe;
    }

    public void setCodeValue(int codeValue) {
        this.codeValue = codeValue;
    }

    public static SocieteCodeKey create(Societe societe, String code) {
        return create(societe, CodeFormatter.INSTANCE.parseNumber(code));
    }

    public static SocieteCodeKey create(Societe societe, int codeValue) {
        return new SocieteCodeKey(societe, codeValue);
    }
    
    public static SocieteCodeKey create(String societe, int codeValue) {
        return new SocieteCodeKey(societe, codeValue);
    }
    
    public static SocieteCodeKey create(String societe, HasSocieteCodeKey hasCodeKey) {
        return new SocieteCodeKey(societe, hasCodeKey.getCodeValue());
    }
    
    public static SocieteCodeKey create(Societe societe, HasSocieteCodeKey hasCodeKey) {
        return new SocieteCodeKey(societe, hasCodeKey.getCodeValue());
    }

    @Override
    public String toString() {
        return "SocieteCodeKey{" +
                "societe='" + societe + '\'' +
                ", codeValue=" + codeValue +
                '}';
    }
}
