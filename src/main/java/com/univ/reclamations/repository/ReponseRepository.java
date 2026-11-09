package com.univ.reclamations.repository;

import com.univ.reclamations.model.Reclamation;
import com.univ.reclamations.model.Reponse;
import com.univ.reclamations.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité {@link Reponse}.
 * <p>
 * Fournit les opérations CRUD standard ainsi que des méthodes de recherche
 * pour retrouver les réponses par réclamation, avec tri chronologique.
 * </p>
 *
 * @see Reponse
 * @see Reclamation
 */
@Repository
public interface ReponseRepository extends JpaRepository<Reponse, Long> {

    /**
     * Recherche toutes les réponses associées à une réclamation donnée.
     *
     * @param reclamation la réclamation recherchée
     * @return la liste des réponses de la réclamation
     */
    List<Reponse> findByReclamation(Reclamation reclamation);

    /**
     * Recherche toutes les réponses d'une réclamation,
     * triées par date de réponse décroissante (les plus récentes en premier).
     *
     * @param reclamation la réclamation recherchée
     * @return la liste triée des réponses
     */
    List<Reponse> findByReclamationOrderByDateReponseDesc(Reclamation reclamation);

    /**
     * Recherche toutes les réponses d'une réclamation, triées par date croissante.
     */
    List<Reponse> findByReclamationOrderByDateReponseAsc(Reclamation reclamation);

    /**
     * Recherche toutes les réponses écrites par un auteur donné.
     */
    List<Reponse> findByAuteur(Utilisateur auteur);
}
