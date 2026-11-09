package com.univ.reclamations.repository;

import com.univ.reclamations.model.Departement;
import com.univ.reclamations.model.Role;
import com.univ.reclamations.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link Utilisateur}.
 * <p>
 * Fournit les opérations CRUD standard ainsi que des méthodes de recherche
 * personnalisées pour retrouver les utilisateurs par email, rôle et département.
 * </p>
 *
 * @see Utilisateur
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * @param email l'adresse email de l'utilisateur
     * @return un {@link Optional} contenant l'utilisateur trouvé, ou vide
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Recherche tous les utilisateurs ayant un rôle spécifique.
     *
     * @param role le rôle recherché
     * @return la liste des utilisateurs correspondants
     */
    List<Utilisateur> findByRole(Role role);

    /**
     * Recherche tous les utilisateurs appartenant à un département donné.
     *
     * @param departement le département recherché
     * @return la liste des utilisateurs du département
     */
    List<Utilisateur> findByDepartement(Departement departement);

    /**
     * Recherche tous les utilisateurs ayant un rôle spécifique dans un département donné.
     *
     * @param role        le rôle recherché
     * @param departement le département recherché
     * @return la liste des utilisateurs correspondants
     */
    /**
     * Recherche tous les utilisateurs ayant un rôle spécifique dans un département donné par son ID.
     */
    List<Utilisateur> findByRoleAndDepartementId(Role role, Long departementId);

    /**
     * Recherche tous les utilisateurs ayant un rôle spécifique dans un département donné.
     */
    List<Utilisateur> findByRoleAndDepartement(Role role, Departement departement);

    /**
     * Compte le nombre d'utilisateurs ayant un rôle donné.
     */
    long countByRole(Role role);

    /**
     * Vérifie si un utilisateur existe déjà avec l'adresse email donnée.
     *
     * @param email l'adresse email à vérifier
     * @return {@code true} si un utilisateur avec cet email existe
     */
    boolean existsByEmail(String email);
}
