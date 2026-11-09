package com.univ.reclamations.repository;

import com.univ.reclamations.model.Notification;
import com.univ.reclamations.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité {@link Notification}.
 * <p>
 * Fournit les opérations CRUD standard ainsi que des méthodes de recherche
 * pour retrouver les notifications d'un utilisateur, avec gestion
 * des notifications non lues.
 * </p>
 *
 * @see Notification
 * @see Utilisateur
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Recherche toutes les notifications d'un destinataire,
     * triées par date de notification décroissante (les plus récentes en premier).
     *
     * @param destinataire l'utilisateur destinataire
     * @return la liste triée des notifications du destinataire
     */
    List<Notification> findByDestinataireOrderByDateNotificationDesc(Utilisateur destinataire);

    /**
     * Recherche toutes les notifications non lues d'un destinataire.
     *
     * @param destinataire l'utilisateur destinataire
     * @return la liste des notifications non lues
     */
    List<Notification> findByDestinataireAndLuFalse(Utilisateur destinataire);

    /**
     * Recherche toutes les notifications non lues d'un destinataire, triées par date décroissante.
     */
    List<Notification> findByDestinataireAndLuFalseOrderByDateNotificationDesc(Utilisateur destinataire);

    /**
     * Compte le nombre de notifications non lues d'un destinataire.
     *
     * @param destinataire l'utilisateur destinataire
     * @return le nombre de notifications non lues
     */
    long countByDestinataireAndLuFalse(Utilisateur destinataire);
}
