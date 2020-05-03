package dev.coop.facturation.controller;

import dev.coop.facturation.FacturationException;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.SocieteCodeKey;
import dev.coop.facturation.persistence.ClientRepository;
import dev.coop.facturation.persistence.FactureRepository;
import dev.coop.facturation.security.ConnectedUser;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("factures")
public class FactureController {

    @Autowired
    private FactureRepository factureRepository;
    @Autowired
    private ClientRepository clientRepository;

    @RequestMapping(method = RequestMethod.GET)
    public List<Facture> getFactures(@AuthenticationPrincipal ConnectedUser user) {
        return factureRepository.findBySociete(user.getUtilisateur().getSociete());
    }

    @RequestMapping(method = RequestMethod.POST)
    public Facture saveFacture(@AuthenticationPrincipal ConnectedUser user, @RequestBody Facture facture) {
        final Societe societe = user.getUtilisateur().getSociete();
        if (!facture.getSociete().getNom().equals(societe.getNom())) {
            throw new FacturationException("Societe de l'article ne correspond pas à celle de l'utilisateur!");
        }

        clientRepository.findById(SocieteCodeKey.create(user.getUtilisateur().getSociete(), facture.getClient()))
                .ifPresent(facture::setClient);

        return factureRepository.save(facture);
    }
}
