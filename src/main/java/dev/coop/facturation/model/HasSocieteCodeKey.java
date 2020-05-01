package dev.coop.facturation.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 *
 * @author lfo
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class HasSocieteCodeKey extends HasCode {

    @Id
    @JsonIgnore
    private final SocieteCodeKey id;
   
    private int codeValue;
    @DBRef
    private Societe societe;

    @PersistenceConstructor
    public HasSocieteCodeKey(SocieteCodeKey id) {
        this.id = id;
        setCode(codeValue);
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
   
    
}
