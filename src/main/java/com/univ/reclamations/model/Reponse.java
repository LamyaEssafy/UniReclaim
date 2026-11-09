package com.univ.reclamations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Entité représentant une réponse à une réclamation.
 */
@Entity
@Table(name = "reponses")
public class Reponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le contenu de la réponse est obligatoire")
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateReponse;

    @Column(nullable = false)
    private boolean valideParChef = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reclamation_id", nullable = false)
    private Reclamation reclamation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = true)
    private Utilisateur auteur;

    @PrePersist
    protected void onCreate() {
        if (this.dateReponse == null) {
            this.dateReponse = LocalDateTime.now();
        }
    }

    // Constructors
    public Reponse() {}

    public Reponse(Long id, String contenu, LocalDateTime dateReponse, boolean valideParChef, Reclamation reclamation, Utilisateur auteur) {
        this.id = id;
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.valideParChef = valideParChef;
        this.reclamation = reclamation;
        this.auteur = auteur;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }

    public boolean isValideParChef() { return valideParChef; }
    public void setValideParChef(boolean valideParChef) { this.valideParChef = valideParChef; }

    public Reclamation getReclamation() { return reclamation; }
    public void setReclamation(Reclamation reclamation) { this.reclamation = reclamation; }

    public Utilisateur getAuteur() { return auteur; }
    public void setAuteur(Utilisateur auteur) { this.auteur = auteur; }

    // Builder
    public static ReponseBuilder builder() {
        return new ReponseBuilder();
    }

    public static class ReponseBuilder {
        private Long id;
        private String contenu;
        private LocalDateTime dateReponse;
        private boolean valideParChef = false;
        private Reclamation reclamation;
        private Utilisateur auteur;

        public ReponseBuilder id(Long id) { this.id = id; return this; }
        public ReponseBuilder contenu(String contenu) { this.contenu = contenu; return this; }
        public ReponseBuilder dateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; return this; }
        public ReponseBuilder valideParChef(boolean valideParChef) { this.valideParChef = valideParChef; return this; }
        public ReponseBuilder reclamation(Reclamation reclamation) { this.reclamation = reclamation; return this; }
        public ReponseBuilder auteur(Utilisateur auteur) { this.auteur = auteur; return this; }

        public Reponse build() {
            return new Reponse(id, contenu, dateReponse, valideParChef, reclamation, auteur);
        }
    }
}
