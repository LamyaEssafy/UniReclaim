package com.univ.reclamations.service;

import com.univ.reclamations.model.*;
import com.univ.reclamations.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service principal de gestion des réclamations.
 * <p>
 * Ce service constitue le cœur métier de l'application. Il gère le cycle de vie
 * complet d'une réclamation : création avec génération de code unique, affectation
 * automatique aux enseignants, suivi du statut, gestion des réponses, validation
 * par le chef de département, et clôture ou rejet.
 * </p>
 * <p>
 * Chaque changement d'état déclenche l'envoi de notifications appropriées
 * aux parties concernées (étudiant, enseignant, chef de département).
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Service
@Transactional
public class ReclamationService {

    private static final Logger log = LoggerFactory.getLogger(ReclamationService.class);
    private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ReclamationRepository reclamationRepository;
    private final ReponseRepository reponseRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    /**
     * Constructeur avec injection de toutes les dépendances nécessaires.
     *
     * @param reclamationRepository le repository des réclamations
     * @param reponseRepository     le repository des réponses
     * @param utilisateurRepository le repository des utilisateurs
     * @param notificationService   le service de notifications
     */
    public ReclamationService(ReclamationRepository reclamationRepository,
                              ReponseRepository reponseRepository,
                              UtilisateurRepository utilisateurRepository,
                              NotificationService notificationService) {
        this.reclamationRepository = reclamationRepository;
        this.reponseRepository = reponseRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationService = notificationService;
    }

    /**
     * Récupère la liste de toutes les réclamations.
     *
     * @return la liste complète des réclamations
     */
    @Transactional(readOnly = true)
    public List<Reclamation> findAll() {
        return reclamationRepository.findAll();
    }

    /**
     * Recherche une réclamation par son identifiant.
     *
     * @param id l'identifiant de la réclamation
     * @return la réclamation trouvée
     * @throws EntityNotFoundException si la réclamation n'existe pas
     */
    @Transactional(readOnly = true)
    public Reclamation findById(Long id) {
        return reclamationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Réclamation non trouvée avec l'id : " + id));
    }

    /**
     * Récupère les réclamations d'un étudiant, triées par date de création décroissante.
     *
     * @param etudiant l'étudiant concerné
     * @return la liste des réclamations de l'étudiant
     */
    @Transactional(readOnly = true)
    public List<Reclamation> findByEtudiant(Utilisateur etudiant) {
        return reclamationRepository.findByEtudiantOrderByDateCreationDesc(etudiant);
    }

    /**
     * Récupère les réclamations affectées à un enseignant.
     *
     * @param enseignant l'enseignant concerné
     * @return la liste des réclamations affectées à l'enseignant
     */
    @Transactional(readOnly = true)
    public List<Reclamation> findByEnseignant(Utilisateur enseignant) {
        return reclamationRepository.findByEnseignantAssigneOrderByDateCreationDesc(enseignant);
    }

    /**
     * Récupère les réclamations d'un département.
     * <p>
     * Recherche via le département du module associé ou via le département
     * de l'étudiant qui a créé la réclamation.
     * </p>
     *
     * @param departement le département concerné
     * @return la liste des réclamations du département
     */
    @Transactional(readOnly = true)
    public List<Reclamation> findByDepartement(Departement departement) {
        return reclamationRepository.findByDepartement(departement.getId());
    }

    /**
     * Récupère les réclamations ayant un statut spécifique.
     *
     * @param statut le statut recherché
     * @return la liste des réclamations avec ce statut
     */
    @Transactional(readOnly = true)
    public List<Reclamation> findByStatut(StatutReclamation statut) {
        return reclamationRepository.findByStatut(statut);
    }

    /**
     * Crée une nouvelle réclamation avec génération de code unique et affectation automatique.
     * <p>
     * Le processus de création comprend les étapes suivantes :
     * <ol>
     *   <li>Génération d'un code unique au format REC-yyyyMMdd-XXX</li>
     *   <li>Initialisation de la date de création et du statut SOUMISE</li>
     *   <li>Tentative d'affectation automatique selon le type de destinataire de la catégorie :
     *     <ul>
     *       <li>MODULE : affectation à l'enseignant responsable du module</li>
     *       <li>ENSEIGNANT : affectation directe si un enseignant est déjà spécifié</li>
     *     </ul>
     *   </li>
     *   <li>Envoi de notifications aux parties concernées</li>
     * </ol>
     * </p>
     *
     * @param reclamation la réclamation à créer
     * @return la réclamation créée et sauvegardée
     */
    public Reclamation creerReclamation(Reclamation reclamation) {
        // Générer le code unique
        reclamation.setCode(genererCodeUnique());

        // Initialiser les dates et le statut
        reclamation.setDateCreation(LocalDateTime.now());
        reclamation.setDateModification(LocalDateTime.now());
        reclamation.setStatut(StatutReclamation.SOUMISE);

        // Tenter l'affectation automatique selon le type de destinataire de la catégorie et le module
        if (reclamation.getCategorie() != null) {
            // Si un module est sélectionné et qu'il possède un enseignant responsable, 
            // on auto-affecte à cet enseignant (valable pour Note, Absence, etc.)
            if (reclamation.getModule() != null && reclamation.getModule().getEnseignantResponsable() != null) {
                reclamation.setEnseignantAssigne(reclamation.getModule().getEnseignantResponsable());
                reclamation.setStatut(StatutReclamation.AFFECTEE);
                log.info("Réclamation auto-affectée à l'enseignant responsable du module : {}",
                        reclamation.getModule().getEnseignantResponsable().getEmail());
            } else if (reclamation.getEnseignantAssigne() != null) {
                reclamation.setStatut(StatutReclamation.AFFECTEE);
                log.info("Réclamation affectée directement à l'enseignant : {}",
                        reclamation.getEnseignantAssigne().getEmail());
            } else {
                log.info("Pas d'affectation automatique possible pour cette réclamation (affectation manuelle ou validation requise).");
            }
        }

        Reclamation saved = reclamationRepository.save(reclamation);

        // Envoyer les notifications
        if (saved.getEnseignantAssigne() != null) {
            notificationService.creerNotification(
                    saved.getEnseignantAssigne(),
                    "Nouvelle réclamation affectée : " + saved.getCode() + " - " + saved.getSujet(),
                    "/enseignant/reclamations/" + saved.getId()
            );
        }

        // Notifier le chef de département si l'étudiant a un département
        notifierChefDepartement(saved,
                "Nouvelle réclamation soumise : " + saved.getCode() + " - " + saved.getSujet());

        log.info("Réclamation créée avec le code : {}", saved.getCode());
        return saved;
    }

    /**
     * Change le statut d'une réclamation.
     *
     * @param id        l'identifiant de la réclamation
     * @param newStatut le nouveau statut à appliquer
     * @return la réclamation mise à jour
     * @throws EntityNotFoundException si la réclamation n'existe pas
     */
    public Reclamation changerStatut(Long id, StatutReclamation newStatut) {
        Reclamation reclamation = findById(id);
        reclamation.setStatut(newStatut);
        reclamation.setDateModification(LocalDateTime.now());
        return reclamationRepository.save(reclamation);
    }

    /**
     * Affecte un enseignant à une réclamation et passe le statut à AFFECTEE.
     *
     * @param reclamationId l'identifiant de la réclamation
     * @param enseignantId  l'identifiant de l'enseignant à affecter
     * @return la réclamation mise à jour
     * @throws EntityNotFoundException si la réclamation ou l'enseignant n'existe pas
     */
    public Reclamation affecterEnseignant(Long reclamationId, Long enseignantId) {
        Reclamation reclamation = findById(reclamationId);
        Utilisateur enseignant = utilisateurRepository.findById(enseignantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Enseignant non trouvé avec l'id : " + enseignantId));

        reclamation.setEnseignantAssigne(enseignant);
        reclamation.setStatut(StatutReclamation.AFFECTEE);
        reclamation.setDateModification(LocalDateTime.now());

        Reclamation saved = reclamationRepository.save(reclamation);

        // Notifier l'enseignant de l'affectation
        notificationService.creerNotification(
                enseignant,
                "Une réclamation vous a été affectée : " + saved.getCode() + " - " + saved.getSujet(),
                "/enseignant/reclamations/" + saved.getId()
        );

        log.info("Réclamation {} affectée à l'enseignant {}", saved.getCode(), enseignant.getEmail());
        return saved;
    }

    /**
     * Soumet une réponse à une réclamation par un enseignant.
     * <p>
     * Crée l'entité Reponse, change le statut de la réclamation à REPONSE_ENSEIGNANT,
     * et notifie l'étudiant ainsi que le chef de département.
     * </p>
     *
     * @param reclamationId l'identifiant de la réclamation
     * @param contenu       le contenu de la réponse
     * @param auteur        l'auteur de la réponse (enseignant)
     * @return la réponse créée
     * @throws EntityNotFoundException si la réclamation n'existe pas
     */
    public Reponse soumettreReponse(Long reclamationId, String contenu, Utilisateur auteur) {
        Reclamation reclamation = findById(reclamationId);

        // Créer la réponse
        Reponse reponse = new Reponse();
        reponse.setContenu(contenu);
        reponse.setDateReponse(LocalDateTime.now());
        reponse.setReclamation(reclamation);
        reponse.setAuteur(auteur);
        reponse.setValideParChef(false);

        Reponse savedReponse = reponseRepository.save(reponse);

        // Mettre à jour le statut de la réclamation
        reclamation.setStatut(StatutReclamation.REPONSE_ENSEIGNANT);
        reclamation.setDateModification(LocalDateTime.now());
        reclamationRepository.save(reclamation);

        // Notifier l'étudiant
        notificationService.creerNotification(
                reclamation.getEtudiant(),
                "L'enseignant a répondu à votre réclamation : " + reclamation.getCode(),
                "/etudiant/reclamations/" + reclamation.getId()
        );

        // Notifier le chef de département
        notifierChefDepartement(reclamation,
                "Réponse de l'enseignant sur la réclamation : " + reclamation.getCode());

        log.info("Réponse soumise pour la réclamation {} par {}", reclamation.getCode(), auteur.getEmail());
        return savedReponse;
    }

    /**
     * Valide la réponse d'un enseignant par le chef de département.
     * <p>
     * Change le statut à VALIDEE_CHEF et notifie l'étudiant et l'enseignant.
     * </p>
     *
     * @param reclamationId l'identifiant de la réclamation
     * @return la réclamation mise à jour
     * @throws EntityNotFoundException si la réclamation n'existe pas
     */
    public Reclamation validerReponseChef(Long reclamationId) {
        Reclamation reclamation = findById(reclamationId);
        reclamation.setStatut(StatutReclamation.VALIDEE_CHEF);
        reclamation.setDateModification(LocalDateTime.now());

        // Marquer les réponses comme validées par le chef
        List<Reponse> reponses = reponseRepository.findByReclamationOrderByDateReponseAsc(reclamation);
        reponses.forEach(r -> r.setValideParChef(true));
        reponseRepository.saveAll(reponses);

        Reclamation saved = reclamationRepository.save(reclamation);

        // Notifier l'étudiant
        notificationService.creerNotification(
                reclamation.getEtudiant(),
                "La réponse à votre réclamation " + reclamation.getCode()
                        + " a été validée par le chef de département.",
                "/etudiant/reclamations/" + reclamation.getId()
        );

        // Notifier l'enseignant
        if (reclamation.getEnseignantAssigne() != null) {
            notificationService.creerNotification(
                    reclamation.getEnseignantAssigne(),
                    "Votre réponse à la réclamation " + reclamation.getCode()
                            + " a été validée par le chef de département.",
                    "/enseignant/reclamations/" + reclamation.getId()
            );
        }

        log.info("Réponse validée par le chef pour la réclamation {}", reclamation.getCode());
        return saved;
    }

    /**
     * Clôture une réclamation.
     * <p>
     * Change le statut à CLOTUREE, met à jour la date de modification,
     * et notifie l'étudiant.
     * </p>
     *
     * @param reclamationId l'identifiant de la réclamation
     * @return la réclamation clôturée
     * @throws EntityNotFoundException si la réclamation n'existe pas
     */
    public Reclamation cloturerReclamation(Long reclamationId) {
        Reclamation reclamation = findById(reclamationId);
        reclamation.setStatut(StatutReclamation.CLOTUREE);
        reclamation.setDateModification(LocalDateTime.now());

        Reclamation saved = reclamationRepository.save(reclamation);

        // Notifier l'étudiant
        notificationService.creerNotification(
                reclamation.getEtudiant(),
                "Votre réclamation " + reclamation.getCode() + " a été clôturée.",
                "/etudiant/reclamations/" + reclamation.getId()
        );

        log.info("Réclamation {} clôturée", reclamation.getCode());
        return saved;
    }

    /**
     * Rejette une réclamation avec un motif de rejet.
     * <p>
     * Change le statut à REJETEE, crée une réponse contenant le motif de rejet,
     * et notifie l'étudiant.
     * </p>
     *
     * @param reclamationId l'identifiant de la réclamation
     * @param motif         le motif du rejet
     * @return la réclamation rejetée
     * @throws EntityNotFoundException si la réclamation n'existe pas
     */
    public Reclamation rejeterReclamation(Long reclamationId, String motif) {
        Reclamation reclamation = findById(reclamationId);
        reclamation.setStatut(StatutReclamation.REJETEE);
        reclamation.setDateModification(LocalDateTime.now());

        // Créer une réponse avec le motif de rejet
        Reponse reponseRejet = new Reponse();
        reponseRejet.setContenu("Réclamation rejetée. Motif : " + motif);
        reponseRejet.setDateReponse(LocalDateTime.now());
        reponseRejet.setReclamation(reclamation);
        reponseRejet.setValideParChef(false);
        reponseRepository.save(reponseRejet);

        Reclamation saved = reclamationRepository.save(reclamation);

        // Notifier l'étudiant
        notificationService.creerNotification(
                reclamation.getEtudiant(),
                "Votre réclamation " + reclamation.getCode() + " a été rejetée. Motif : " + motif,
                "/etudiant/reclamations/" + reclamation.getId()
        );

        log.info("Réclamation {} rejetée. Motif : {}", reclamation.getCode(), motif);
        return saved;
    }

    /**
     * Renvoyer la réclamation pour compléments (statut SOUMISE).
     */
    public Reclamation retournerReclamation(Long reclamationId, String commentaire) {
        Reclamation reclamation = findById(reclamationId);
        reclamation.setStatut(StatutReclamation.SOUMISE);
        reclamation.setDateModification(LocalDateTime.now());

        // Créer une réponse système avec les détails demandés
        Reponse reponseComplement = new Reponse();
        reponseComplement.setContenu("Retour pour compléments. Motif : " + commentaire);
        reponseComplement.setDateReponse(LocalDateTime.now());
        reponseComplement.setReclamation(reclamation);
        reponseComplement.setValideParChef(false);
        reponseRepository.save(reponseComplement);

        Reclamation saved = reclamationRepository.save(reclamation);

        // Notifier l'étudiant
        notificationService.creerNotification(
                reclamation.getEtudiant(),
                "Votre réclamation " + reclamation.getCode() + " a été renvoyée pour compléments : " + commentaire,
                "/etudiant/reclamations/" + reclamation.getId()
        );

        // Notifier l'enseignant assigné s'il y en a un
        if (reclamation.getEnseignantAssigne() != null) {
            notificationService.creerNotification(
                    reclamation.getEnseignantAssigne(),
                    "La réclamation " + reclamation.getCode() + " a été renvoyée pour compléments : " + commentaire,
                    "/enseignant/reclamations/" + reclamation.getId()
            );
        }

        return saved;
    }

    /**
     * Archiver une réclamation (statut ARCHIVEE).
     */
    public Reclamation archiverReclamation(Long reclamationId) {
        Reclamation reclamation = findById(reclamationId);
        reclamation.setStatut(StatutReclamation.ARCHIVEE);
        reclamation.setDateModification(LocalDateTime.now());

        Reclamation saved = reclamationRepository.save(reclamation);

        // Notifier l'étudiant
        notificationService.creerNotification(
                reclamation.getEtudiant(),
                "Votre réclamation " + reclamation.getCode() + " a été archivée.",
                "/etudiant/reclamations/" + reclamation.getId()
        );

        return saved;
    }

    /**
     * Compte le nombre total de réclamations.
     *
     * @return le nombre total de réclamations
     */
    @Transactional(readOnly = true)
    public long count() {
        return reclamationRepository.count();
    }

    /**
     * Compte le nombre de réclamations ayant un statut donné.
     *
     * @param statut le statut à compter
     * @return le nombre de réclamations avec ce statut
     */
    @Transactional(readOnly = true)
    public long countByStatut(StatutReclamation statut) {
        return reclamationRepository.countByStatut(statut);
    }

    /**
     * Compte le nombre de réclamations d'une catégorie donnée.
     *
     * @param categorieId l'identifiant de la catégorie
     * @return le nombre de réclamations dans cette catégorie
     */
    @Transactional(readOnly = true)
    public long countByCategorie(Long categorieId) {
        return reclamationRepository.countByCategorieId(categorieId);
    }

    // ========================================================================
    // Méthodes utilitaires privées
    // ========================================================================

    /**
     * Génère un code unique pour une réclamation au format REC-yyyyMMdd-XXX.
     * Le numéro séquentiel est basé sur le nombre de réclamations créées à la date courante.
     *
     * @return le code unique généré
     */
    private String genererCodeUnique() {
        String datePart = LocalDateTime.now().format(CODE_DATE_FORMAT);
        String prefix = "REC-" + datePart + "-";

        // Compter les réclamations existantes avec le même préfixe de date
        long countToday = reclamationRepository.countByCodeStartingWith(prefix);
        String sequentialNumber = String.format("%03d", countToday + 1);

        return prefix + sequentialNumber;
    }

    /**
     * Notifie le chef de département associé à la réclamation.
     * <p>
     * Recherche le chef via le département de l'étudiant ou via le département
     * du module associé à la réclamation.
     * </p>
     *
     * @param reclamation la réclamation concernée
     * @param message     le message de notification
     */
    private void notifierChefDepartement(Reclamation reclamation, String message) {
        Departement departement = null;

        // Tenter de récupérer le département via l'étudiant
        if (reclamation.getEtudiant() != null) {
            if (reclamation.getEtudiant().getDepartement() != null) {
                departement = reclamation.getEtudiant().getDepartement();
            } else if (reclamation.getEtudiant().getFiliere() != null 
                    && reclamation.getEtudiant().getFiliere().getDepartement() != null) {
                departement = reclamation.getEtudiant().getFiliere().getDepartement();
            }
        }
        
        // Sinon, via le module
        if (departement == null && reclamation.getModule() != null
                && reclamation.getModule().getFiliere() != null
                && reclamation.getModule().getFiliere().getDepartement() != null) {
            departement = reclamation.getModule().getFiliere().getDepartement();
        }

        if (departement != null) {
            List<Utilisateur> chefs = utilisateurRepository
                    .findByRoleAndDepartement(Role.CHEF, departement);
            for (Utilisateur chef : chefs) {
                notificationService.creerNotification(
                        chef,
                        message,
                        "/chef/reclamations/" + reclamation.getId()
                );
            }
        }
    }
}
