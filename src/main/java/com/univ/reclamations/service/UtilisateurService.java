package com.univ.reclamations.service;

import com.univ.reclamations.model.Departement;
import com.univ.reclamations.model.Role;
import com.univ.reclamations.model.Utilisateur;
import com.univ.reclamations.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de gestion des utilisateurs.
 * <p>
 * Fournit les opérations CRUD ainsi que les fonctionnalités de recherche
 * par rôle, département et email. Gère l'encodage des mots de passe
 * lors de la création et de la mise à jour des comptes.
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Autowired
    private com.univ.reclamations.repository.ReclamationRepository reclamationRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.univ.reclamations.repository.ReponseRepository reponseRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.univ.reclamations.repository.NotificationRepository notificationRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.univ.reclamations.repository.ModuleRepository moduleRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.univ.reclamations.repository.PieceJointeRepository pieceJointeRepository;

    /**
     * Constructeur avec injection des dépendances.
     *
     * @param utilisateurRepository le repository des utilisateurs
     * @param passwordEncoder       l'encodeur de mots de passe BCrypt
     */
    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Récupère la liste de tous les utilisateurs.
     *
     * @return la liste complète des utilisateurs
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    /**
     * Recherche un utilisateur par son identifiant.
     *
     * @param id l'identifiant de l'utilisateur
     * @return l'utilisateur trouvé
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Utilisateur non trouvé avec l'id : " + id));
    }

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * @param email l'adresse email de l'utilisateur
     * @return l'utilisateur trouvé
     * @throws EntityNotFoundException si aucun utilisateur ne correspond à cet email
     */
    @Transactional(readOnly = true)
    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Utilisateur non trouvé avec l'email : " + email));
    }

    /**
     * Vérifie si un utilisateur existe avec cet email.
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    /**
     * Récupère la liste des utilisateurs ayant un rôle spécifique.
     *
     * @param role le rôle recherché
     * @return la liste des utilisateurs correspondants
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> findByRole(Role role) {
        return utilisateurRepository.findByRole(role);
    }

    /**
     * Récupère la liste des utilisateurs appartenant à un département.
     *
     * @param departement le département recherché
     * @return la liste des utilisateurs du département
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> findByDepartement(Departement departement) {
        return utilisateurRepository.findByDepartement(departement);
    }

    /**
     * Récupère la liste des enseignants d'un département donné.
     *
     * @param departementId l'identifiant du département
     * @return la liste des enseignants du département
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> findEnseignantsByDepartement(Long departementId) {
        return utilisateurRepository.findByRoleAndDepartementId(Role.ENSEIGNANT, departementId);
    }

    /**
     * Crée un nouvel utilisateur avec encodage du mot de passe.
     * <p>
     * Le mot de passe est automatiquement encodé avec BCrypt.
     * La date de création est initialisée à la date/heure courante.
     * </p>
     *
     * @param utilisateur l'utilisateur à créer
     * @return l'utilisateur sauvegardé
     */
    public Utilisateur save(Utilisateur utilisateur) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        if (utilisateur.getDateCreation() == null) {
            utilisateur.setDateCreation(LocalDateTime.now());
        }
        if (utilisateur.getFiliere() != null && utilisateur.getFiliere().getDepartement() != null) {
            utilisateur.setDepartement(utilisateur.getFiliere().getDepartement());
        }
        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Met à jour un utilisateur existant.
     * <p>
     * Le mot de passe n'est ré-encodé que s'il a été modifié.
     * Si le mot de passe est vide ou null, l'ancien mot de passe est conservé.
     * </p>
     *
     * @param utilisateur l'utilisateur avec les données mises à jour
     * @return l'utilisateur mis à jour
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    public Utilisateur update(Utilisateur utilisateur) {
        Utilisateur existant = utilisateurRepository.findById(utilisateur.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Utilisateur non trouvé avec l'id : " + utilisateur.getId()));

        // Ne ré-encoder le mot de passe que s'il a été modifié
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isBlank()) {
            // Conserver l'ancien mot de passe si le champ est vide
            utilisateur.setMotDePasse(existant.getMotDePasse());
        } else if (!utilisateur.getMotDePasse().equals(existant.getMotDePasse())) {
            // Encoder uniquement si le mot de passe a changé
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }

        // Conserver la date de création originale
        utilisateur.setDateCreation(existant.getDateCreation());

        if (utilisateur.getFiliere() != null && utilisateur.getFiliere().getDepartement() != null) {
            utilisateur.setDepartement(utilisateur.getFiliere().getDepartement());
        }

        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Supprime un utilisateur par son identifiant.
     *
     * @param id l'identifiant de l'utilisateur à supprimer
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    public void delete(Long id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + id));

        // 1. Supprimer toutes les notifications destinées à cet utilisateur
        List<com.univ.reclamations.model.Notification> notifications = notificationRepository
                .findByDestinataireOrderByDateNotificationDesc(user);
        notificationRepository.deleteAll(notifications);

        // 2. Mettre l'auteur à null pour toutes les réponses rédigées par cet utilisateur
        List<com.univ.reclamations.model.Reponse> reponses = reponseRepository.findByAuteur(user);
        for (com.univ.reclamations.model.Reponse rep : reponses) {
            rep.setAuteur(null);
            reponseRepository.save(rep);
        }

        // 3. Libérer l'affectation dans les réclamations où cet utilisateur est l'enseignant assigné
        List<com.univ.reclamations.model.Reclamation> reclamationsAssignees = reclamationRepository.findByEnseignantAssigne(user);
        for (com.univ.reclamations.model.Reclamation rec : reclamationsAssignees) {
            rec.setEnseignantAssigne(null);
            if (rec.getStatut() == com.univ.reclamations.model.StatutReclamation.AFFECTEE 
                    || rec.getStatut() == com.univ.reclamations.model.StatutReclamation.EN_TRAITEMENT) {
                rec.setStatut(com.univ.reclamations.model.StatutReclamation.SOUMISE);
            }
            reclamationRepository.save(rec);
        }

        // 4. Retirer le rôle de responsable de module pour cet utilisateur
        List<com.univ.reclamations.model.ModuleUni> modulesResponsable = moduleRepository.findByEnseignantResponsable(user);
        for (com.univ.reclamations.model.ModuleUni mod : modulesResponsable) {
            mod.setEnseignantResponsable(null);
            moduleRepository.save(mod);
        }

        // 5. Supprimer toutes les réclamations dont cet utilisateur est l'étudiant créateur
        List<com.univ.reclamations.model.Reclamation> reclamationsEtudiant = reclamationRepository
                .findByEtudiantOrderByDateCreationDesc(user);
        for (com.univ.reclamations.model.Reclamation rec : reclamationsEtudiant) {
            List<com.univ.reclamations.model.PieceJointe> pjs = pieceJointeRepository.findByReclamation(rec);
            pieceJointeRepository.deleteAll(pjs);

            List<com.univ.reclamations.model.Reponse> reps = reponseRepository.findByReclamationOrderByDateReponseAsc(rec);
            reponseRepository.deleteAll(reps);

            List<com.univ.reclamations.model.Notification> notifsLies = notificationRepository.findAll().stream()
                    .filter(n -> n.getLien() != null && n.getLien().contains("/reclamations/" + rec.getId()))
                    .toList();
            notificationRepository.deleteAll(notifsLies);

            reclamationRepository.delete(rec);
        }

        // 6. Supprimer l'utilisateur lui-même
        utilisateurRepository.delete(user);
    }

    /**
     * Bascule l'état actif/inactif d'un utilisateur.
     *
     * @param id l'identifiant de l'utilisateur
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    public void toggleActive(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Utilisateur non trouvé avec l'id : " + id));
        utilisateur.setActif(!utilisateur.isActif());
        utilisateurRepository.save(utilisateur);
    }

    /**
     * Compte le nombre total d'utilisateurs.
     *
     * @return le nombre total d'utilisateurs
     */
    @Transactional(readOnly = true)
    public long count() {
        return utilisateurRepository.count();
    }

    /**
     * Compte le nombre d'utilisateurs ayant un rôle spécifique.
     *
     * @param role le rôle à compter
     * @return le nombre d'utilisateurs avec ce rôle
     */
    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        return utilisateurRepository.countByRole(role);
    }
}
