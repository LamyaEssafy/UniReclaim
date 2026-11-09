package com.univ.reclamations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Classe principale de l'application de gestion des réclamations universitaires.
 * <p>
 * Lance le contexte Spring Boot et permet également le déploiement
 * de l'application sous forme de fichier WAR sur un serveur Tomcat.
 * </p>
 *
 * @author Université
 * @version 1.0
 */
@SpringBootApplication
public class ReclamationsApplication extends SpringBootServletInitializer {

    /**
     * Point d'entrée principal de l'application.
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(ReclamationsApplication.class, args);
    }

    /**
     * Configuration nécessaire pour le déploiement en tant que fichier WAR
     * sur un serveur d'applications externe (Tomcat).
     *
     * @param application le constructeur Spring Boot
     * @return le constructeur configuré
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ReclamationsApplication.class);
    }
}