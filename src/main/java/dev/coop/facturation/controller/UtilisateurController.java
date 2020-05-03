package dev.coop.facturation.controller;

import dev.coop.facturation.model.Utilisateur;
import dev.coop.facturation.security.ConnectedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("utilisateurs")
public class UtilisateurController {
    
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Utilisateur getProfile(@AuthenticationPrincipal ConnectedUser user, String login) {
        final Utilisateur utilisateur = user.getUtilisateur().setPassword(null);
        return utilisateur;
    }
}
