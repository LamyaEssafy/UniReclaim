package com.univ.reclamations.repository;

import com.univ.reclamations.model.Filiere;
import com.univ.reclamations.model.ModuleUni;
import com.univ.reclamations.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité {@link ModuleUni}.
 * <p>
 * Fournit les opérations CRUD standard ainsi que des méthodes de recherche
 * pour retrouver les modules par filière et par enseignant responsable.
 * </p>
 *
 * @see ModuleUni
 * @see Filiere
 * @see Utilisateur
 */
@Repository
public interface ModuleRepository extends JpaRepository<ModuleUni, Long> {

    /**
     * Recherche tous les modules rattachés à une filière donnée.
     *
     * @param filiere la filière recherchée
     * @return la liste des modules de la filière
     */
    List<ModuleUni> findByFiliere(Filiere filiere);

    /**
     * Recherche tous les modules rattachés à une filière par son identifiant.
     *
     * @param filiereId l'identifiant de la filière
     * @return la liste des modules de la filière
     */
    List<ModuleUni> findByFiliereId(Long filiereId);

    /**
     * Recherche tous les modules dont l'enseignant donné est responsable.
     *
     * @param enseignant l'enseignant responsable
     * @return la liste des modules de l'enseignant
     */
    List<ModuleUni> findByEnseignantResponsable(Utilisateur enseignant);
}
