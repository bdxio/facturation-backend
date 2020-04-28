package dev.coop.facturation.security;

import dev.coop.facturation.model.Utilisateur;
import dev.coop.facturation.persistence.UtilisateurRepository;
import java.util.Arrays;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
   
    
    @Override
    public UserDetails loadUserByUsername(String string) throws UsernameNotFoundException {
        final Utilisateur utilisateur = utilisateurRepository.findByLogin(string);
        return new ConnectedUser(utilisateur);
    }

    
    
}
