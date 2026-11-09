package com.univ.reclamations.service;

import com.univ.reclamations.model.Reclamation;
import com.univ.reclamations.model.Reponse;
import com.univ.reclamations.repository.ReponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des réponses aux réclamations.
 * <p>
 * Fournit les opérations de recherche et de sauvegarde des réponses
 * associées aux réclamations.
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Service
@Transactional
public class ReponseService {

    private final ReponseRepository reponseRepository;

    /**
     * Constructeur avec injection du repository des réponses.
     *
     * @param reponseRepository le repository des réponses
     */
    public ReponseService(ReponseRepository reponseRepository) {
        this.reponseRepository = reponseRepository;
    }

    /**
     * Récupère toutes les réponses associées à une réclamation donnée.
     *
     * @param reclamation la réclamation dont on veut les réponses
     * @return la liste des réponses triées par date
     */
    @Transactional(readOnly = true)
    public List<Reponse> findByReclamation(Reclamation reclamation) {
        return reponseRepository.findByReclamationOrderByDateReponseAsc(reclamation);
    }

    /**
     * Sauvegarde une réponse en base de données.
     *
     * @param reponse la réponse à sauvegarder
     * @return la réponse sauvegardée avec son identifiant généré
     */
    public Reponse save(Reponse reponse) {
        return reponseRepository.save(reponse);
    }
}
