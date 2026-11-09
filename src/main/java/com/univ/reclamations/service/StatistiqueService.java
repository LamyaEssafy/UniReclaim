package com.univ.reclamations.service;

import com.univ.reclamations.model.*;
import com.univ.reclamations.repository.CategorieRepository;
import com.univ.reclamations.repository.ReclamationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de statistiques pour le tableau de bord.
 * <p>
 * Fournit des données agrégées et des comptages pour alimenter les graphiques
 * et indicateurs du tableau de bord administratif, de département et étudiant.
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Service
@Transactional(readOnly = true)
public class StatistiqueService {

    private final ReclamationRepository reclamationRepository;
    private final CategorieRepository categorieRepository;

    private static final DateTimeFormatter MOIS_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");

    /**
     * Constructeur avec injection des dépendances.
     *
     * @param reclamationRepository le repository des réclamations
     * @param categorieRepository   le repository des catégories
     */
    public StatistiqueService(ReclamationRepository reclamationRepository,
                              CategorieRepository categorieRepository) {
        this.reclamationRepository = reclamationRepository;
        this.categorieRepository = categorieRepository;
    }

    /**
     * Calcule le nombre de réclamations par catégorie.
     *
     * @return une map avec le nom de la catégorie en clé et le nombre de réclamations en valeur
     */
    public Map<String, Long> getReclamationsParCategorie() {
        List<Categorie> categories = categorieRepository.findAll();
        Map<String, Long> result = new LinkedHashMap<>();

        for (Categorie categorie : categories) {
            long count = reclamationRepository.countByCategorieId(categorie.getId());
            if (count > 0) {
                result.put(categorie.getNom(), count);
            }
        }

        return result;
    }

    /**
     * Calcule le nombre de réclamations par statut.
     *
     * @return une map avec le nom du statut en clé et le nombre de réclamations en valeur
     */
    public Map<String, Long> getReclamationsParStatut() {
        Map<String, Long> result = new LinkedHashMap<>();

        for (StatutReclamation statut : StatutReclamation.values()) {
            long count = reclamationRepository.countByStatut(statut);
            result.put(statut.name(), count);
        }

        return result;
    }

    /**
     * Calcule le nombre de réclamations par département.
     *
     * @return une map avec le nom du département en clé et le nombre de réclamations en valeur
     */
    public Map<String, Long> getReclamationsParDepartement() {
        List<Reclamation> toutes = reclamationRepository.findAll();
        Map<String, Long> result = new LinkedHashMap<>();

        for (Reclamation rec : toutes) {
            String nomDepartement = determinerDepartement(rec);
            if (nomDepartement != null) {
                result.merge(nomDepartement, 1L, Long::sum);
            }
        }

        // Trier par nombre décroissant
        return result.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Calcule le nombre de réclamations par mois sur les 12 derniers mois.
     *
     * @return une map avec le mois (format MM/yyyy) en clé et le nombre de réclamations en valeur
     */
    public Map<String, Long> getReclamationsParMois() {
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Initialiser les 12 derniers mois à zéro
        for (int i = 11; i >= 0; i--) {
            YearMonth mois = YearMonth.from(now.minusMonths(i));
            result.put(mois.format(MOIS_FORMAT), 0L);
        }

        // Compter les réclamations des 12 derniers mois
        LocalDateTime debutPeriode = now.minusMonths(12).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Reclamation> reclamations = reclamationRepository
                .findByDateCreationAfter(debutPeriode);

        for (Reclamation rec : reclamations) {
            if (rec.getDateCreation() != null) {
                String moisKey = YearMonth.from(rec.getDateCreation()).format(MOIS_FORMAT);
                result.computeIfPresent(moisKey, (key, val) -> val + 1);
            }
        }

        return result;
    }

    /**
     * Calcule le nombre de réclamations par mois pour un département donné.
     */
    public Map<String, Long> getReclamationsParMois(Departement departement) {
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 11; i >= 0; i--) {
            YearMonth mois = YearMonth.from(now.minusMonths(i));
            result.put(mois.format(MOIS_FORMAT), 0L);
        }

        List<Reclamation> reclamations = reclamationRepository.findByDepartement(departement.getId());

        for (Reclamation rec : reclamations) {
            if (rec.getDateCreation() != null) {
                String moisKey = YearMonth.from(rec.getDateCreation()).format(MOIS_FORMAT);
                result.computeIfPresent(moisKey, (key, val) -> val + 1);
            }
        }

        return result;
    }

    /**
     * Calcule le nombre de réclamations par catégorie pour un département donné.
     */
    public Map<String, Long> getReclamationsParCategorie(Departement departement) {
        List<Reclamation> reclamations = reclamationRepository.findByDepartement(departement.getId());
        Map<String, Long> result = new LinkedHashMap<>();

        for (Reclamation rec : reclamations) {
            if (rec.getCategorie() != null) {
                result.merge(rec.getCategorie().getNom(), 1L, Long::sum);
            }
        }

        return result;
    }

    /**
     * Identifie les 5 enseignants les plus sollicités dans un département.
     *
     * @param departement le département à analyser
     * @return une map avec le nom de l'enseignant en clé et le nombre de réclamations en valeur
     */
    public Map<String, Long> getEnseignantsLesPlusSollicites(Departement departement) {
        List<Reclamation> reclamations = reclamationRepository.findByDepartement(departement.getId());

        Map<String, Long> compteurs = new LinkedHashMap<>();

        for (Reclamation rec : reclamations) {
            if (rec.getEnseignantAssigne() != null) {
                String nomComplet = rec.getEnseignantAssigne().getPrenom()
                        + " " + rec.getEnseignantAssigne().getNom();
                compteurs.merge(nomComplet, 1L, Long::sum);
            }
        }

        // Retourner les 5 premiers triés par nombre décroissant
        return compteurs.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Identifie les 5 enseignants les plus sollicités au niveau global.
     */
    public Map<String, Long> getEnseignantsLesPlusSollicites() {
        List<Reclamation> reclamations = reclamationRepository.findAll();
        Map<String, Long> compteurs = new LinkedHashMap<>();

        for (Reclamation rec : reclamations) {
            if (rec.getEnseignantAssigne() != null) {
                String nomComplet = rec.getEnseignantAssigne().getPrenom()
                        + " " + rec.getEnseignantAssigne().getNom();
                compteurs.merge(nomComplet, 1L, Long::sum);
            }
        }

        return compteurs.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Calcule les statistiques globales du système.
     * <p>
     * Retourne les compteurs suivants :
     * <ul>
     *   <li>total : nombre total de réclamations</li>
     *   <li>en_attente : réclamations soumises non encore affectées</li>
     *   <li>en_cours : réclamations affectées ou en traitement</li>
     *   <li>cloturees : réclamations clôturées</li>
     *   <li>rejetees : réclamations rejetées</li>
     *   <li>en_retard : réclamations soumises depuis plus de 7 jours sans réponse</li>
     * </ul>
     * </p>
     *
     * @return une map avec les indicateurs statistiques globaux
     */
    public Map<String, Long> getStatsGlobales() {
        Map<String, Long> stats = new LinkedHashMap<>();

        stats.put("total", reclamationRepository.count());
        stats.put("en_attente", reclamationRepository.countByStatut(StatutReclamation.SOUMISE));
        stats.put("en_cours",
                reclamationRepository.countByStatut(StatutReclamation.AFFECTEE)
                        + reclamationRepository.countByStatut(StatutReclamation.EN_TRAITEMENT)
                        + reclamationRepository.countByStatut(StatutReclamation.REPONSE_ENSEIGNANT));
        stats.put("cloturees", reclamationRepository.countByStatut(StatutReclamation.CLOTUREE)
                + reclamationRepository.countByStatut(StatutReclamation.ARCHIVEE));
        stats.put("rejetees", reclamationRepository.countByStatut(StatutReclamation.REJETEE));

        // Calculer les réclamations en retard (soumises depuis plus de 7 jours)
        LocalDateTime limitRetard = LocalDateTime.now().minusDays(7);
        List<Reclamation> soumises = reclamationRepository.findByStatut(StatutReclamation.SOUMISE);
        long enRetard = soumises.stream()
                .filter(r -> r.getDateCreation() != null && r.getDateCreation().isBefore(limitRetard))
                .count();
        stats.put("en_retard", enRetard);

        return stats;
    }

    /**
     * Calcule les statistiques spécifiques à un département.
     *
     * @param departement le département à analyser
     * @return une map avec les indicateurs statistiques du département
     */
    public Map<String, Long> getStatsDepartement(Departement departement) {
        List<Reclamation> reclamations = reclamationRepository.findByDepartement(departement.getId());
        Map<String, Long> stats = new LinkedHashMap<>();

        stats.put("total", (long) reclamations.size());
        stats.put("en_attente", compterParStatut(reclamations, StatutReclamation.SOUMISE));
        stats.put("en_cours",
                compterParStatut(reclamations, StatutReclamation.AFFECTEE)
                        + compterParStatut(reclamations, StatutReclamation.EN_TRAITEMENT)
                        + compterParStatut(reclamations, StatutReclamation.REPONSE_ENSEIGNANT));
        stats.put("cloturees", compterParStatut(reclamations, StatutReclamation.CLOTUREE)
                + compterParStatut(reclamations, StatutReclamation.ARCHIVEE));
        stats.put("rejetees", compterParStatut(reclamations, StatutReclamation.REJETEE));
        stats.put("validees_chef", compterParStatut(reclamations, StatutReclamation.VALIDEE_CHEF));
        stats.put("traitees",
                compterParStatut(reclamations, StatutReclamation.VALIDEE_CHEF)
                        + compterParStatut(reclamations, StatutReclamation.CLOTUREE)
                        + compterParStatut(reclamations, StatutReclamation.REJETEE)
                        + compterParStatut(reclamations, StatutReclamation.ARCHIVEE));

        return stats;
    }

    /**
     * Calcule les statistiques spécifiques à un étudiant.
     *
     * @param etudiant l'étudiant à analyser
     * @return une map avec les indicateurs statistiques de l'étudiant
     */
    public Map<String, Long> getStatsEtudiant(Utilisateur etudiant) {
        List<Reclamation> reclamations = reclamationRepository
                .findByEtudiantOrderByDateCreationDesc(etudiant);
        Map<String, Long> stats = new LinkedHashMap<>();

        stats.put("total", (long) reclamations.size());
        stats.put("en_attente", compterParStatut(reclamations, StatutReclamation.SOUMISE));
        stats.put("en_cours",
                compterParStatut(reclamations, StatutReclamation.AFFECTEE)
                        + compterParStatut(reclamations, StatutReclamation.EN_TRAITEMENT)
                        + compterParStatut(reclamations, StatutReclamation.REPONSE_ENSEIGNANT));
        stats.put("cloturees", compterParStatut(reclamations, StatutReclamation.CLOTUREE)
                + compterParStatut(reclamations, StatutReclamation.ARCHIVEE));
        stats.put("rejetees", compterParStatut(reclamations, StatutReclamation.REJETEE));
        stats.put("acceptees",
                compterParStatut(reclamations, StatutReclamation.VALIDEE_CHEF)
                        + compterParStatut(reclamations, StatutReclamation.CLOTUREE)
                        + compterParStatut(reclamations, StatutReclamation.ARCHIVEE));

        return stats;
    }

    // ========================================================================
    // Méthodes utilitaires privées
    // ========================================================================

    /**
     * Détermine le nom du département associé à une réclamation.
     * Cherche d'abord via l'étudiant, puis via le module.
     *
     * @param reclamation la réclamation à analyser
     * @return le nom du département ou null si indéterminé
     */
    private String determinerDepartement(Reclamation reclamation) {
        if (reclamation.getEtudiant() != null) {
            if (reclamation.getEtudiant().getDepartement() != null) {
                return reclamation.getEtudiant().getDepartement().getNom();
            }
            if (reclamation.getEtudiant().getFiliere() != null 
                    && reclamation.getEtudiant().getFiliere().getDepartement() != null) {
                return reclamation.getEtudiant().getFiliere().getDepartement().getNom();
            }
        }
        if (reclamation.getModule() != null
                && reclamation.getModule().getFiliere() != null
                && reclamation.getModule().getFiliere().getDepartement() != null) {
            return reclamation.getModule().getFiliere().getDepartement().getNom();
        }
        return null;
    }

    /**
     * Compte les réclamations ayant un statut donné dans une liste.
     *
     * @param reclamations la liste de réclamations à filtrer
     * @param statut       le statut recherché
     * @return le nombre de réclamations avec ce statut
     */
    private long compterParStatut(List<Reclamation> reclamations, StatutReclamation statut) {
        return reclamations.stream()
                .filter(r -> r.getStatut() == statut)
                .count();
    }
}
