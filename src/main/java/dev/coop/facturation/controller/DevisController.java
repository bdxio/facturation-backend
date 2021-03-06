package dev.coop.facturation.controller;

import dev.coop.facturation.FacturationException;
import dev.coop.facturation.model.Devis;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.SocieteCodeKey;
import dev.coop.facturation.persistence.ClientRepository;
import dev.coop.facturation.persistence.DevisRepository;
import dev.coop.facturation.security.ConnectedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author lfo
 */

@RestController
@RequestMapping("devis")
public class DevisController {
    @Autowired
    private DevisRepository devisRepository;
    @Autowired
    private ClientRepository clientRepository;

    @RequestMapping(method = RequestMethod.GET)
    public List<Devis> getDevis(@AuthenticationPrincipal ConnectedUser user) {
        return devisRepository.findBySociete(user.getUtilisateur().getSociete());
    }

    @RequestMapping(method = RequestMethod.POST)
    public Devis saveDevis(@AuthenticationPrincipal ConnectedUser user, @RequestBody Devis devis) {
        final Societe societe = user.getUtilisateur().getSociete();
        if (!devis.getSociete().getNom().equals(societe.getNom())) {
            throw new FacturationException("Societe de l'article ne correspond pas à celle de l'utilisateur!");
        }
        clientRepository.findById(SocieteCodeKey.create(user.getUtilisateur().getSociete(), devis.getClient()))
                .ifPresent(devis::setClient);
        return devisRepository.save(devis);
    }
}
