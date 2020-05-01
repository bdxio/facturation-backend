package dev.coop.facturation.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.springframework.data.annotation.PersistenceConstructor;

/**
 *
 * @author lforet
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Client extends HasSocieteCodeKey {
        
    private String nom;
    private String nomCourt;
    private String numTVAIntracom;
    
    private Adresse adresse;

    @PersistenceConstructor
    public Client(SocieteCodeKey id) {
        super(id);
    }

    public Client(Societe societe, int codeValue) {
        super(societe, codeValue);
    }
    
    @Override
    public CodeFormatter.Prefix getCodePrefix() {
        return CodeFormatter.Prefix.CL;
    }

    public String getNom() {
        return nom;
    }

    public Client setNom(String nom) {
        this.nom = nom;
        return this;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public String getNumTvaIntracom() {
        return numTVAIntracom;
    }

    public Client setAdresse(Adresse adresse) {
        this.adresse = adresse;
        return this;
    }

    public String getNomCourt() {
        return nomCourt == null ? getNom() : nomCourt;
    }

    public Client setNomCourt(String nomCourt) {
        this.nomCourt = nomCourt;
        return this;
    }

    public Client setNumTVAIntracom(String num) {
        numTVAIntracom = num;
        return this;
    }
}
