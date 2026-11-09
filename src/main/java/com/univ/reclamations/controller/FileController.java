package com.univ.reclamations.controller;

import com.univ.reclamations.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLConnection;

/**
 * Contrôleur gérant le téléchargement et l'envoi de fichiers.
 * Utilise le StorageService pour stocker et récupérer les fichiers
 * (pièces jointes aux réclamations).
 */
@Controller
public class FileController {

    private final StorageService storageService;

    /**
     * Constructeur avec injection du service de stockage.
     *
     * @param storageService service de gestion du stockage de fichiers
     */
    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Télécharge un fichier par son nom.
     * Le fichier est retourné en tant que ressource avec les en-têtes HTTP appropriés
     * pour le téléchargement ou l'affichage dans le navigateur.
     *
     * @param filename le nom du fichier à télécharger
     * @return la réponse HTTP contenant le fichier en tant que ressource
     */
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Resource resource = storageService.loadAsResource(filename);

            // Déterminer le type MIME du fichier
            String contentType = URLConnection.guessContentTypeFromName(filename);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Envoie un fichier vers le serveur.
     * Le fichier est stocké via le StorageService et le nom du fichier
     * stocké est retourné dans les attributs flash.
     *
     * @param fichier           le fichier à envoyer
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la page précédente avec le résultat
     */
    @PostMapping("/files/upload")
    public String uploadFile(@RequestParam("fichier") MultipartFile fichier,
                              RedirectAttributes redirectAttributes) {
        try {
            if (fichier.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier.");
                return "redirect:/";
            }

            String storedFilename = storageService.store(fichier);
            redirectAttributes.addFlashAttribute("success", "Fichier envoyé avec succès.");
            redirectAttributes.addFlashAttribute("filename", storedFilename);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'envoi du fichier : " + e.getMessage());
        }
        return "redirect:/";
    }
}
