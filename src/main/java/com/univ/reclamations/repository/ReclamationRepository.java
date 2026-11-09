package com.univ.reclamations.repository;

import com.univ.reclamations.model.Categorie;
import com.univ.reclamations.model.Departement;
import com.univ.reclamations.model.Reclamation;
import com.univ.reclamations.model.StatutReclamation;
import com.univ.reclamations.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link Reclamation}.
 * <p>
 * Fournit les opérations CRUD standard ainsi que des méthodes de recherche
 * avancées pour retrouver les réclamations par étudiant, enseignant, statut,
 * catégorie et département.
 * </p>
 *
 * @see Reclamation
 * @see Utilisateur
 * @see StatutReclamation
 * @see Categorie
 */
@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    /**
     * Recherche une réclamation par son code unique.
     *
     * @param code le code de la réclamation
     * @return un {@link Optional} contenant la réclamation trouvée, ou vide
     */
    Optional<Reclamation> findByCode(String code);

    /**
     * Recherche toutes les réclamations soumises par un étudiant donné.
     *
     * @param etudiant l'étudiant auteur des réclamations
     * @return la liste des réclamations de l'étudiant
     */
    List<Reclamation> findByEtudiant(Utilisateur etudiant);

    /**
     * Recherche toutes les réclamations soumises par un étudiant,
     * triées par date de création décroissante.
     *
     * @param etudiant l'étudiant auteur des réclamations
     * @return la liste triée des réclamations de l'étudiant
     */
    List<Reclamation> findByEtudiantOrderByDateCreationDesc(Utilisateur etudiant);

    /**
     * Recherche toutes les réclamations affectées à un enseignant donné.
     *
     * @param enseignant l'enseignant assigné
     * @return la liste des réclamations affectées à l'enseignant
     */
    List<Reclamation> findByEnseignantAssigne(Utilisateur enseignant);

    /**
     * Recherche les réclamations affectées à un enseignant avec un statut spécifique.
     *
     * @param enseignant l'enseignant assigné
     * @param statut     le statut recherché
     * @return la liste des réclamations correspondantes
     */
    List<Reclamation> findByEnseignantAssigneAndStatut(Utilisateur enseignant, StatutReclamation statut);

    /**
     * Recherche toutes les réclamations ayant un statut spécifique.
     *
     * @param statut le statut recherché
     * @return la liste des réclamations avec ce statut
     */
    List<Reclamation> findByStatut(StatutReclamation statut);

    /**
     * Compte le nombre de réclamations ayant un statut spécifique.
     *
     * @param statut le statut à compter
     * @return le nombre de réclamations avec ce statut
     */
    long countByStatut(StatutReclamation statut);

    /**
     * Compte le nombre de réclamations dans une catégorie donnée.
     *
     * @param categorie la catégorie à compter
     * @return le nombre de réclamations dans cette catégorie
     */
    /**
     * Compte le nombre de réclamations dans une catégorie donnée.
     *
     * @param categorie la catégorie à compter
     * @return le nombre de réclamations dans cette catégorie
     */
    long countByCategorie(Categorie categorie);

    /**
     * Compte le nombre de réclamations par l'ID de la catégorie.
     */
    long countByCategorieId(Long id);

    /**
     * Recherche toutes les réclamations créées après une date donnée.
     */
    List<Reclamation> findByDateCreationAfter(java.time.LocalDateTime date);

    /**
     * Recherche les réclamations liées à un département donné par son ID.
     */
    @Query("SELECT DISTINCT r FROM Reclamation r " +
           "LEFT JOIN r.module m " +
           "LEFT JOIN m.filiere f " +
           "LEFT JOIN r.etudiant e " +
           "LEFT JOIN e.departement ed " +
           "LEFT JOIN e.filiere ef " +
           "LEFT JOIN ef.departement efd " +
           "WHERE f.departement.id = :deptId " +
           "OR ed.id = :deptId " +
           "OR efd.id = :deptId")
    List<Reclamation> findByDepartement(@Param("deptId") Long departementId);

    /**
     * Recherche toutes les réclamations assignées à un enseignant triées par date.
     */
    List<Reclamation> findByEnseignantAssigneOrderByDateCreationDesc(Utilisateur enseignant);

    /**
     * Compte les codes commençant par le préfixe.
     */
    long countByCodeStartingWith(String prefix);
}
