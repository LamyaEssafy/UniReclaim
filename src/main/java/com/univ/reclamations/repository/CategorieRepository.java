package com.univ.reclamations.repository;

import com.univ.reclamations.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link Categorie}.
 * <p>
 * Fournit les opérations CRUD standard ainsi qu'une méthode de recherche
 * par nom de catégorie.
 * </p>
 *
 * @see Categorie
 */
@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    /**
     * Recherche une catégorie par son nom.
     *
     * @param nom le nom de la catégorie
     * @return un {@link Optional} contenant la catégorie trouvée, ou vide
     */
    Optional<Categorie> findByNom(String nom);
}
