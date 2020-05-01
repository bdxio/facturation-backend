package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Client;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.SocieteCodeKey;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClientRepository extends MongoRepository<Client, SocieteCodeKey> {
    
    List<Client> findBySociete(Societe societe);

    default Client findByIdOrThrow(SocieteCodeKey id) {
        return this.findById(id)
                .orElseThrow(() -> new IllegalStateException(String.format("Le client %s est inconnue", id)));
    }
}
