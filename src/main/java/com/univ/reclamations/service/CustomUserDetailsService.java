package com.univ.reclamations.service;

import com.univ.reclamations.model.Utilisateur;
import com.univ.reclamations.repository.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service d'authentification personnalisé pour Spring Security.
 * <p>
 * Charge les utilisateurs depuis la base de données via leur adresse email.
 * Vérifie que le compte est actif avant d'autoriser l'accès.
 * Mappe le rôle de l'utilisateur vers une autorité Spring Security avec le préfixe ROLE_.
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Constructeur avec injection du repository des utilisateurs.
     *
     * @param utilisateurRepository le repository pour accéder aux données utilisateur
     */
    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Charge un utilisateur par son adresse email pour l'authentification Spring Security.
     *
     * @param email l'adresse email de l'utilisateur
     * @return les détails de l'utilisateur pour Spring Security
     * @throws UsernameNotFoundException si l'utilisateur n'existe pas ou si le compte est inactif
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun utilisateur trouvé avec l'email : " + email));

        if (!utilisateur.isActif()) {
            throw new UsernameNotFoundException(
                    "Le compte de l'utilisateur " + email + " est désactivé.");
        }

        return new User(
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())
                )
        );
    }
}
