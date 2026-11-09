package com.univ.reclamations.repository;

import com.univ.reclamations.model.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA pour l'entité {@link Departement}.
 * <p>
 * Fournit les opérations CRUD standard héritées de {@link JpaRepository}.
 * </p>
 *
 * @see Departement
 */
@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {
}
