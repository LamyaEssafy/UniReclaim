package com.univ.reclamations.repository;

import com.univ.reclamations.model.Departement;
import com.univ.reclamations.model.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité {@link Filiere}.
 * <p>
 * Fournit les opérations CRUD standard ainsi que des méthodes de recherche
 * pour retrouver les filières par département.
 * </p>
 *
 * @see Filiere
 * @see Departement
 */
@Repository
public interface FiliereRepository extends JpaRepository<Filiere, Long> {

    /**
     * Recherche toutes les filières rattachées à un département donné.
     *
     * @param departement le département recherché
     * @return la liste des filières du département
     */
    List<Filiere> findByDepartement(Departement departement);

    /**
     * Recherche toutes les filières rattachées à un département par son identifiant.
     *
     * @param departementId l'identifiant du département
     * @return la liste des filières du département
     */
    List<Filiere> findByDepartementId(Long departementId);
}
