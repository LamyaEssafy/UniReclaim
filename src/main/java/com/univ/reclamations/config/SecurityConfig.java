package com.univ.reclamations.config;

import com.univ.reclamations.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité Spring Security.
 * <p>
 * Définit les règles d'autorisation pour les différentes URL de l'application,
 * la page de connexion personnalisée, la gestion de la déconnexion,
 * le mécanisme « remember-me » et le fournisseur d'authentification.
 * </p>
 * <p>
 * Les rôles sont mappés aux espaces URL suivants :
 * <ul>
 *   <li>/admin/** → ADMIN</li>
 *   <li>/etudiant/** → ETUDIANT</li>
 *   <li>/enseignant/** → ENSEIGNANT</li>
 *   <li>/chef/** → CHEF</li>
 * </ul>
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;

    /**
     * Constructeur avec injection des dépendances de sécurité.
     *
     * @param customUserDetailsService le service de chargement des utilisateurs
     * @param customAuthSuccessHandler le gestionnaire de succès d'authentification
     */
    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          CustomAuthSuccessHandler customAuthSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthSuccessHandler = customAuthSuccessHandler;
    }

    /**
     * Bean de l'encodeur de mots de passe BCrypt.
     *
     * @return l'encodeur BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Fournisseur d'authentification basé sur la base de données.
     * <p>
     * Utilise le service CustomUserDetailsService pour charger les utilisateurs
     * et BCryptPasswordEncoder pour vérifier les mots de passe.
     * </p>
     *
     * @return le fournisseur d'authentification configuré
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Chaîne de filtres de sécurité définissant les règles d'accès.
     * <p>
     * Configuration :
     * <ul>
     *   <li>Ressources publiques accessibles sans authentification</li>
     *   <li>Espaces protégés par rôle (admin, étudiant, enseignant, chef)</li>
     *   <li>Page de connexion personnalisée avec redirection basée sur le rôle</li>
     *   <li>Remember-me activé pour les sessions persistantes</li>
     *   <li>Déconnexion redirigée vers /login?logout</li>
     * </ul>
     * </p>
     *
     * @param http le constructeur de configuration de sécurité HTTP
     * @return la chaîne de filtres de sécurité construite
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        // Ressources publiques
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",
                                "/api/public/**"
                        ).permitAll()

                        // Espaces protégés par rôle
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/etudiant/**").hasRole("ETUDIANT")
                        .requestMatchers("/enseignant/**").hasRole("ENSEIGNANT")
                        .requestMatchers("/chef/**").hasRole("CHEF")

                        // Toute autre requête nécessite une authentification
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/notifications/api/**")
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .userDetailsService(customUserDetailsService)
                        .tokenValiditySeconds(86400) // 24 heures
                        .key("reclamations-remember-me-key")
                );

        return http.build();
    }
}
