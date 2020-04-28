package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Utilisateur;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UtilisateurRepository extends MongoRepository<Utilisateur, String> {
    
    public Utilisateur findByLogin(String login);
    
}