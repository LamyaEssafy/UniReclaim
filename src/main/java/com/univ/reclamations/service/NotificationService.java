package com.univ.reclamations.service;

import com.univ.reclamations.model.Notification;
import com.univ.reclamations.model.Utilisateur;
import com.univ.reclamations.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de gestion des notifications utilisateur.
 * <p>
 * Permet de créer, consulter et gérer l'état de lecture des notifications
 * envoyées aux utilisateurs du système de réclamations.
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Constructeur avec injection du repository des notifications.
     *
     * @param notificationRepository le repository des notifications
     */
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Crée et sauvegarde une nouvelle notification pour un utilisateur.
     *
     * @param destinataire l'utilisateur destinataire de la notification
     * @param message      le contenu du message de notification
     * @param lien         le lien associé à la notification (page de détail, etc.)
     * @return la notification créée et sauvegardée
     */
    public Notification creerNotification(Utilisateur destinataire, String message, String lien) {
        Notification notification = new Notification();
        notification.setDestinataire(destinataire);
        notification.setMessage(message);
        notification.setLien(lien);
        notification.setDateNotification(LocalDateTime.now());
        notification.setLu(false);
        return notificationRepository.save(notification);
    }

    /**
     * Récupère la liste des notifications non lues d'un utilisateur.
     *
     * @param utilisateur l'utilisateur concerné
     * @return la liste des notifications non lues, triées par date décroissante
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsNonLues(Utilisateur utilisateur) {
        return notificationRepository.findByDestinataireAndLuFalseOrderByDateNotificationDesc(utilisateur);
    }

    /**
     * Compte le nombre de notifications non lues d'un utilisateur.
     *
     * @param utilisateur l'utilisateur concerné
     * @return le nombre de notifications non lues
     */
    @Transactional(readOnly = true)
    public long countNonLues(Utilisateur utilisateur) {
        return notificationRepository.countByDestinataireAndLuFalse(utilisateur);
    }

    /**
     * Marque une notification spécifique comme lue.
     *
     * @param notificationId l'identifiant de la notification
     * @throws EntityNotFoundException si la notification n'existe pas
     */
    public void marquerCommeLue(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notification non trouvée avec l'id : " + notificationId));
        notification.setLu(true);
        notificationRepository.save(notification);
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues.
     *
     * @param utilisateur l'utilisateur dont les notifications doivent être marquées
     */
    public void marquerToutesCommeLues(Utilisateur utilisateur) {
        List<Notification> nonLues = notificationRepository
                .findByDestinataireAndLuFalseOrderByDateNotificationDesc(utilisateur);
        nonLues.forEach(notification -> notification.setLu(true));
        notificationRepository.saveAll(nonLues);
    }

    /**
     * Récupère toutes les notifications d'un utilisateur, triées par date décroissante.
     *
     * @param utilisateur l'utilisateur concerné
     * @return la liste de toutes les notifications de l'utilisateur
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Utilisateur utilisateur) {
        return notificationRepository.findByDestinataireOrderByDateNotificationDesc(utilisateur);
    }
}
