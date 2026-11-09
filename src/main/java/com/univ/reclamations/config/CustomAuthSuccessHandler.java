package com.univ.reclamations.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Gestionnaire personnalisé du succès d'authentification.
 * <p>
 * Redirige l'utilisateur vers le tableau de bord correspondant à son rôle
 * après une connexion réussie. Chaque rôle dispose de son propre espace :
 * <ul>
 *   <li>ROLE_ADMIN → /admin/dashboard</li>
 *   <li>ROLE_ETUDIANT → /etudiant/dashboard</li>
 *   <li>ROLE_ENSEIGNANT → /enseignant/dashboard</li>
 *   <li>ROLE_CHEF → /chef/dashboard</li>
 * </ul>
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthSuccessHandler.class);

    /**
     * Détermine l'URL de redirection en fonction du rôle de l'utilisateur authentifié.
     *
     * @param request        la requête HTTP
     * @param response       la réponse HTTP
     * @param authentication l'objet d'authentification contenant les autorités de l'utilisateur
     * @throws IOException      en cas d'erreur d'entrée/sortie
     * @throws ServletException en cas d'erreur de servlet
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String redirectUrl = determinerUrlRedirection(authentication);
        log.info("Utilisateur '{}' authentifié avec succès. Redirection vers : {}",
                authentication.getName(), redirectUrl);
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    /**
     * Détermine l'URL de redirection appropriée en fonction des autorités de l'utilisateur.
     *
     * @param authentication l'objet d'authentification
     * @return l'URL de redirection correspondant au rôle
     */
    private String determinerUrlRedirection(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            return switch (role) {
                case "ROLE_ADMIN" -> "/admin/dashboard";
                case "ROLE_ETUDIANT" -> "/etudiant/dashboard";
                case "ROLE_ENSEIGNANT" -> "/enseignant/dashboard";
                case "ROLE_CHEF" -> "/chef/dashboard";
                default -> "/login";
            };
        }
        // Si aucun rôle n'est trouvé, rediriger vers la page de connexion
        log.warn("Aucun rôle reconnu pour l'utilisateur : {}", authentication.getName());
        return "/login";
    }
}
