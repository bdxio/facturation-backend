package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Facture;
import org.springframework.stereotype.Component;

@Component
public class FactureRepository extends InMemoryRepository<Facture> {

    @Override
    protected String getEntityName() {
        return "Facture";
    }
}
