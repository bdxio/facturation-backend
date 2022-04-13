package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientRepository extends InMemoryRepository<Client> {

    @Override
    protected String getEntityName() {
        return "Client";
    }
}
