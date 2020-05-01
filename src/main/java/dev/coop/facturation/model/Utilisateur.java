package dev.coop.facturation.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Objects;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Utilisateur {

    @Id
    private String login;
    private String password;
    @DBRef()
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Utilisateur that = (Utilisateur) o;
        return Objects.equals(getLogin(), that.getLogin()) &&
                Objects.equals(getPassword(), that.getPassword()) &&
                Objects.equals(getSociete(), that.getSociete());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLogin(), getPassword(), getSociete());
    }
}
