package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Societe;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SocieteRepository extends MongoRepository<Societe, String> {
    default Societe findByIdOrThrow(String id) {
        return this.findById(id)
                .orElseThrow(() -> new IllegalStateException(String.format("La société %s est inconnue", id)));
    }
}
