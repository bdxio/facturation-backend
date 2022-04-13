package dev.coop.facturation.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author lfo
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class HasSocieteCodeKey extends HasCode {

    @JsonIgnore
    private final SocieteCodeKey id;
   
    private int codeValue;

    private Societe societe;

    public HasSocieteCodeKey(SocieteCodeKey id) {
        this.id = id;
        setCode(id.getCodeValue());
    }
   
    public HasSocieteCodeKey(Societe societe, int codeValue) {
        this.societe = societe;
        this.codeValue = codeValue;
        this.id = new SocieteCodeKey(societe, codeValue);
    }

    public int getCodeValue() {
        return codeValue;
    }
    
    public final Societe getSociete() {
        return societe;
    }

    public final void setSociete(Societe societe) {
        id.setSociete(societe.getNomCourt());
        this.societe = societe;
    }

    public SocieteCodeKey getId() {
        return id;
    }

    public abstract void updateCode();
}
