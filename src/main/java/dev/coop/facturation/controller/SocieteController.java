package dev.coop.facturation.controller;

import dev.coop.facturation.model.Societe;
import dev.coop.facturation.persistence.SocieteRepository;
import dev.coop.facturation.security.ConnectedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("societes")
public class SocieteController {

    @Autowired
    private SocieteRepository societeRepository;
   
    @RequestMapping(method = RequestMethod.GET)
    public Societe getSociete(@AuthenticationPrincipal ConnectedUser user) {
        return user.getUtilisateur().getSociete();
    }

    @RequestMapping(method = RequestMethod.POST)
    public Societe saveSociete(@AuthenticationPrincipal ConnectedUser user, @RequestBody Societe societe) {
        return societeRepository.save(societe);
    }
}
