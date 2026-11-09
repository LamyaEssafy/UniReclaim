package com.univ.reclamations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entité représentant une réclamation universitaire.
 */
@Entity
@Table(name = "reclamations", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code", name = "uk_reclamation_code")
})
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(max = 300, message = "Le sujet ne doit pas dépasser 300 caractères")
    @Column(nullable = false, length = 300)
    private String sujet;

    @NotBlank(message = "La description est obligatoire")
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutReclamation statut = StatutReclamation.BROUILLON;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Utilisateur etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private ModuleUni module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_assigne_id")
    private Utilisateur enseignantAssigne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reponse> reponses = new ArrayList<>();

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PieceJointe> piecesJointes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
        if (this.code == null || this.code.isBlank()) {
            String datePart = this.dateCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int randomPart = ThreadLocalRandom.current().nextInt(1, 1000);
            this.code = String.format("REC-%s-%03d", datePart, randomPart);
        }
        if (this.statut == null) {
            this.statut = StatutReclamation.BROUILLON;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    // Constructors
    public Reclamation() {}

    public Reclamation(Long id, String code, String sujet, String description, LocalDateTime dateCreation, LocalDateTime dateModification, StatutReclamation statut, Utilisateur etudiant, ModuleUni module, Utilisateur enseignantAssigne, Categorie categorie) {
        this.id = id;
        this.code = code;
        this.sujet = sujet;
        this.description = description;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.statut = statut;
        this.etudiant = etudiant;
        this.module = module;
        this.enseignantAssigne = enseignantAssigne;
        this.categorie = categorie;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public StatutReclamation getStatut() { return statut; }
    public void setStatut(StatutReclamation statut) { this.statut = statut; }

    public Utilisateur getEtudiant() { return etudiant; }
    public void setEtudiant(Utilisateur etudiant) { this.etudiant = etudiant; }

    public ModuleUni getModule() { return module; }
    public void setModule(ModuleUni module) { this.module = module; }

    public Utilisateur getEnseignantAssigne() { return enseignantAssigne; }
    public void setEnseignantAssigne(Utilisateur enseignantAssigne) { this.enseignantAssigne = enseignantAssigne; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public List<Reponse> getReponses() { return reponses; }
    public void setReponses(List<Reponse> reponses) { this.reponses = reponses; }

    public List<PieceJointe> getPiecesJointes() { return piecesJointes; }
    public void setPiecesJointes(List<PieceJointe> piecesJointes) { this.piecesJointes = piecesJointes; }

    // Builder
    public static ReclamationBuilder builder() {
        return new ReclamationBuilder();
    }

    public static class ReclamationBuilder {
        private Long id;
        private String code;
        private String sujet;
        private String description;
        private LocalDateTime dateCreation;
        private LocalDateTime dateModification;
        private StatutReclamation statut = StatutReclamation.BROUILLON;
        private Utilisateur etudiant;
        private ModuleUni module;
        private Utilisateur enseignantAssigne;
        private Categorie categorie;

        public ReclamationBuilder id(Long id) { this.id = id; return this; }
        public ReclamationBuilder code(String code) { this.code = code; return this; }
        public ReclamationBuilder sujet(String sujet) { this.sujet = sujet; return this; }
        public ReclamationBuilder description(String description) { this.description = description; return this; }
        public ReclamationBuilder dateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; return this; }
        public ReclamationBuilder dateModification(LocalDateTime dateModification) { this.dateModification = dateModification; return this; }
        public ReclamationBuilder statut(StatutReclamation statut) { this.statut = statut; return this; }
        public ReclamationBuilder etudiant(Utilisateur etudiant) { this.etudiant = etudiant; return this; }
        public ReclamationBuilder module(ModuleUni module) { this.module = module; return this; }
        public ReclamationBuilder enseignantAssigne(Utilisateur enseignantAssigne) { this.enseignantAssigne = enseignantAssigne; return this; }
        public ReclamationBuilder categorie(Categorie categorie) { this.categorie = categorie; return this; }

        public Reclamation build() {
            return new Reclamation(id, code, sujet, description, dateCreation, dateModification, statut, etudiant, module, enseignantAssigne, categorie);
        }
    }
}
