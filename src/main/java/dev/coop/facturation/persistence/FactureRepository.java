package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Article;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.SocieteCodeKey;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FactureRepository extends MongoRepository<Facture, SocieteCodeKey> {
    
    List<Facture> findBySociete(Societe societe);

    default Facture findByIdOrThrow(SocieteCodeKey id) {
        return this.findById(id)
                .orElseThrow(() -> new IllegalStateException(String.format("La facture %s est inconnue", id)));
    }
}
