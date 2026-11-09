package com.univ.reclamations.model;

/**
 * Énumération des statuts possibles d'une réclamation.
 * <p>
 * Représente le cycle de vie complet d'une réclamation, depuis sa création
 * en brouillon jusqu'à sa clôture ou son rejet.
 * </p>
 *
 * <ul>
 *     <li>{@link #BROUILLON} — Réclamation en cours de rédaction, non encore soumise</li>
 *     <li>{@link #SOUMISE} — Réclamation soumise et en attente d'affectation</li>
 *     <li>{@link #AFFECTEE} — Réclamation affectée à un enseignant</li>
 *     <li>{@link #EN_TRAITEMENT} — Réclamation en cours de traitement par l'enseignant</li>
 *     <li>{@link #REPONSE_ENSEIGNANT} — Réponse fournie par l'enseignant, en attente de validation</li>
 *     <li>{@link #VALIDEE_CHEF} — Réponse validée par le chef de département</li>
 *     <li>{@link #CLOTUREE} — Réclamation clôturée définitivement</li>
 *     <li>{@link #REJETEE} — Réclamation rejetée</li>
 * </ul>
 */
public enum StatutReclamation {
    BROUILLON,
    SOUMISE,
    AFFECTEE,
    EN_TRAITEMENT,
    REPONSE_ENSEIGNANT,
    VALIDEE_CHEF,
    CLOTUREE,
    REJETEE,
    ARCHIVEE
}
