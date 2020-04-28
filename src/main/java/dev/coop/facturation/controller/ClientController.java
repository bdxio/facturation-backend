package dev.coop.facturation.controller;

import dev.coop.facturation.FacturationException;
import dev.coop.facturation.model.Client;
import dev.coop.facturation.model.SocieteCodeKey;
import dev.coop.facturation.model.Utilisateur;
import dev.coop.facturation.persistence.ClientRepository;
import dev.coop.facturation.security.ConnectedUser;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("clients")
public class ClientController  {

    @Autowired
    private ClientRepository clientRepository;

    @RequestMapping(method = RequestMethod.GET)
    public List<Client> getClients(@AuthenticationPrincipal ConnectedUser user) {
        return clientRepository.findBySociete(user.getUtilisateur().getSociete());
    }

    @RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public Client saveClient(@AuthenticationPrincipal ConnectedUser user, @RequestBody Client client) {
        final Utilisateur utilisateur = user.getUtilisateur();
        final Client clientInDb = clientRepository.findOne(SocieteCodeKey.create(utilisateur.getSociete(), client.getCode()));
        if (clientInDb == null) {
            client.setSociete(utilisateur.getSociete());
        } else if (clientInDb.getSociete().equals(utilisateur.getSociete())) {
            client.setSociete(clientInDb.getSociete());
        } else {
            throw new FacturationException("La société de l'utilisateur doit correspondre au client");
        }
        return clientRepository.save(client);
    }
}
