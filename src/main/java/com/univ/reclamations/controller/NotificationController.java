package com.univ.reclamations.controller;

import com.univ.reclamations.model.Notification;
import com.univ.reclamations.model.Utilisateur;
import com.univ.reclamations.service.NotificationService;
import com.univ.reclamations.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur gérant les notifications des utilisateurs.
 * Fournit une API REST pour compter, lister et marquer
 * les notifications comme lues, utilisée par les appels AJAX
 * depuis l'interface utilisateur.
 */
@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurService utilisateurService;

    /**
     * Constructeur avec injection des dépendances.
     *
     * @param notificationService service de gestion des notifications
     * @param utilisateurService  service de gestion des utilisateurs
     */
    public NotificationController(NotificationService notificationService,
                                  UtilisateurService utilisateurService) {
        this.notificationService = notificationService;
        this.utilisateurService = utilisateurService;
    }

    /**
     * Récupère l'utilisateur actuellement connecté.
     *
     * @return l'utilisateur courant
     */
    private Utilisateur getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return utilisateurService.findByEmail(email);
    }

    /**
     * Retourne le nombre de notifications non lues pour l'utilisateur connecté.
     * Endpoint utilisé en AJAX pour mettre à jour le badge de notification.
     *
     * @return le nombre de notifications non lues au format JSON
     */
    @GetMapping("/api/notifications/count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> countNotifications() {
        try {
            Utilisateur currentUser = getCurrentUser();
            long count = notificationService.countNonLues(currentUser);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Long> response = new HashMap<>();
            response.put("count", 0L);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Retourne la liste des notifications non lues pour l'utilisateur connecté.
     * Endpoint utilisé en AJAX pour afficher le dropdown de notifications.
     *
     * @return la liste des notifications non lues au format JSON
     */
    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<List<Notification>> getNotifications() {
        try {
            Utilisateur currentUser = getCurrentUser();
            List<Notification> notifications = notificationService.getNotificationsNonLues(currentUser);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Marque une notification spécifique comme lue.
     *
     * @param id identifiant de la notification
     * @return une réponse de succès au format JSON
     */
    @PostMapping("/api/notifications/{id}/lire")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> marquerCommeLue(@PathVariable Long id) {
        try {
            notificationService.marquerCommeLue(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification marquée comme lue.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Marque toutes les notifications de l'utilisateur connecté comme lues.
     *
     * @return une réponse de succès au format JSON
     */
    @PostMapping("/api/notifications/lire-tout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> marquerToutesCommeLues() {
        try {
            Utilisateur currentUser = getCurrentUser();
            notificationService.marquerToutesCommeLues(currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Toutes les notifications ont été marquées comme lues.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
