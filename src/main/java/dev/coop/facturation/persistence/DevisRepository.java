package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Devis;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.SocieteCodeKey;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 * @author lfo
 */
public interface DevisRepository extends MongoRepository<Devis, SocieteCodeKey> {
    
    public List<Devis> findBySociete(Societe societe);
    
}
