package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.SocieteCodeKey;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FactureRepository extends MongoRepository<Facture, SocieteCodeKey> {
    
    public List<Facture> findBySociete(Societe societe);
    
}
