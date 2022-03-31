package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Societe;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SocieteRepository {

    Map<String, Societe> repo = new HashMap<>();

    public Optional<Societe> findById(final String id) {
        if(repo.containsKey(id)){
            return Optional.of(repo.get(id));
        }else{
            return Optional.empty();
        }
    }

    public void save(final Societe entity) {
        repo.put(entity.getNomCourt(), entity);
    }

    public Societe findByIdOrThrow(String id) {
        return this.findById(id).orElseThrow(() -> new IllegalStateException(String.format("Unknown Entity [%s] with id:[%s]", "Societe", id)));
    }


    public void deleteAll() {
        repo = new HashMap<>();
    }
}
