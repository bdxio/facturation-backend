package dev.coop.facturation.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Utilisateur {

    @Id
    private String login;
    private String password;
    @DBRef(lazy = false) 
    @JsonIgnore
    private Societe societe;

    public String getLogin() {
        return login;
    }

    public Utilisateur setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Utilisateur setPassword(String password) {
        this.password = password;
        return this;
    }
    
    public Societe getSociete() {
        return societe;
    }
    
    public Utilisateur setSociete(Societe societe) {
        this.societe = societe;
        return this;
    }
}
