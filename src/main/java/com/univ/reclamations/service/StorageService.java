package com.univ.reclamations.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service de stockage des fichiers uploadés.
 * <p>
 * Gère le téléversement, le chargement et la suppression des fichiers
 * (pièces jointes) sur le système de fichiers local.
 * Utilise un préfixe UUID pour garantir l'unicité des noms de fichiers
 * et empêche les attaques de type path traversal.
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final Path rootLocation;

    /**
     * Constructeur avec injection du répertoire d'upload depuis la configuration.
     *
     * @param uploadDir le chemin du répertoire d'upload configuré dans application.properties
     */
    public StorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Initialise le répertoire d'upload lors du démarrage de l'application.
     * Crée le répertoire s'il n'existe pas.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Répertoire d'upload initialisé : {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Impossible de créer le répertoire d'upload : " + rootLocation, e);
        }
    }

    /**
     * Stocke un fichier uploadé sur le système de fichiers.
     * <p>
     * Le fichier est renommé avec un préfixe UUID pour éviter les collisions.
     * Le nom original est nettoyé pour prévenir les attaques de type path traversal.
     * </p>
     *
     * @param file le fichier multipart à stocker
     * @return le nom unique du fichier stocké
     * @throws RuntimeException si le fichier est vide ou si une erreur d'écriture survient
     */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Impossible de stocker un fichier vide.");
        }

        // Nettoyer le nom du fichier pour prévenir le path traversal
        String nomOriginal = sanitizeFilename(file.getOriginalFilename());
        String nomUnique = UUID.randomUUID() + "_" + nomOriginal;

        try {
            Path destinationFile = rootLocation.resolve(Paths.get(nomUnique)).normalize();

            // Vérification de sécurité contre le path traversal
            if (!destinationFile.startsWith(rootLocation)) {
                throw new RuntimeException(
                        "Tentative de stockage de fichier en dehors du répertoire autorisé.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Fichier stocké avec succès : {}", nomUnique);
            return nomUnique;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du stockage du fichier : " + nomOriginal, e);
        }
    }

    /**
     * Charge un fichier stocké en tant que ressource Spring.
     *
     * @param filename le nom du fichier à charger
     * @return la ressource Spring correspondante
     * @throws RuntimeException si le fichier n'existe pas ou n'est pas lisible
     */
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();

            // Vérification de sécurité contre le path traversal
            if (!file.startsWith(rootLocation)) {
                throw new RuntimeException(
                        "Tentative d'accès à un fichier en dehors du répertoire autorisé.");
            }

            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier introuvable ou illisible : " + filename);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur lors du chargement du fichier : " + filename, e);
        }
    }

    /**
     * Supprime un fichier stocké du système de fichiers.
     *
     * @param filename le nom du fichier à supprimer
     */
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();

            // Vérification de sécurité contre le path traversal
            if (!file.startsWith(rootLocation)) {
                throw new RuntimeException(
                        "Tentative de suppression d'un fichier en dehors du répertoire autorisé.");
            }

            if (Files.exists(file)) {
                Files.delete(file);
                log.info("Fichier supprimé : {}", filename);
            } else {
                log.warn("Fichier à supprimer introuvable : {}", filename);
            }

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier : " + filename, e);
        }
    }

    /**
     * Nettoie un nom de fichier pour prévenir les attaques de type path traversal.
     * <p>
     * Supprime les caractères dangereux et les séquences de répertoires relatifs.
     * </p>
     *
     * @param filename le nom de fichier original
     * @return le nom de fichier nettoyé
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "fichier_sans_nom";
        }

        // Extraire uniquement le nom du fichier (sans chemin)
        String sanitized = Paths.get(filename).getFileName().toString();

        // Supprimer les caractères spéciaux dangereux
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9.\\-_àâäéèêëïîôùûüÿçÀÂÄÉÈÊËÏÎÔÙÛÜŸÇ]", "_");

        // Empêcher les noms de fichiers commençant par un point (fichiers cachés)
        if (sanitized.startsWith(".")) {
            sanitized = "_" + sanitized;
        }

        return sanitized;
    }
}
