package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Societe;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SocieteRepository extends MongoRepository<Societe, String> {

}
