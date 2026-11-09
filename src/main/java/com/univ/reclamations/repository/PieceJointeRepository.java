package com.univ.reclamations.repository;

import com.univ.reclamations.model.PieceJointe;
import com.univ.reclamations.model.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité {@link PieceJointe}.
 * <p>
 * Fournit les opérations CRUD standard ainsi qu'une méthode de recherche
 * pour retrouver les pièces jointes d'une réclamation donnée.
 * </p>
 *
 * @see PieceJointe
 * @see Reclamation
 */
@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, Long> {

    /**
     * Recherche toutes les pièces jointes associées à une réclamation donnée.
     *
     * @param reclamation la réclamation recherchée
     * @return la liste des pièces jointes de la réclamation
     */
    List<PieceJointe> findByReclamation(Reclamation reclamation);
}
