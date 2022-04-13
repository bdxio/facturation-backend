package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Devis;
import org.springframework.stereotype.Component;


@Component
public class DevisRepository extends InMemoryRepository<Devis> {

    @Override
    protected String getEntityName() {
        return "Devis";
    }
}
