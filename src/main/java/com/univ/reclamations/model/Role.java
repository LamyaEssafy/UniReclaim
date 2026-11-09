package com.univ.reclamations.model;

/**
 * Énumération des rôles disponibles dans le système de réclamations.
 * <p>
 * Chaque utilisateur possède un rôle unique qui détermine ses permissions
 * et son accès aux fonctionnalités de la plateforme.
 * </p>
 *
 * <ul>
 *     <li>{@link #ADMIN} — Administrateur système avec accès complet</li>
 *     <li>{@link #ETUDIANT} — Étudiant pouvant soumettre des réclamations</li>
 *     <li>{@link #ENSEIGNANT} — Enseignant pouvant traiter les réclamations affectées</li>
 *     <li>{@link #CHEF} — Chef de département validant les réponses</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    ETUDIANT,
    ENSEIGNANT,
    CHEF
}
