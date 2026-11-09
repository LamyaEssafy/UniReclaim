package com.univ.reclamations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entité représentant un module d'enseignement universitaire.
 */
@Entity
@Table(name = "modules")
public class ModuleUni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du module est obligatoire")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    @Column(nullable = false, length = 200)
    private String nom;

    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String description;

    @NotBlank(message = "Le code du module est obligatoire")
    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères")
    @Column(nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_responsable_id")
    private Utilisateur enseignantResponsable;

    // Constructors
    public ModuleUni() {}

    public ModuleUni(Long id, String nom, String description, String code, Filiere filiere, Utilisateur enseignantResponsable) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.code = code;
        this.filiere = filiere;
        this.enseignantResponsable = enseignantResponsable;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Filiere getFiliere() { return filiere; }
    public void setFiliere(Filiere filiere) { this.filiere = filiere; }

    public Utilisateur getEnseignantResponsable() { return enseignantResponsable; }
    public void setEnseignantResponsable(Utilisateur enseignantResponsable) { this.enseignantResponsable = enseignantResponsable; }

    // Builder
    public static ModuleUniBuilder builder() {
        return new ModuleUniBuilder();
    }

    public static class ModuleUniBuilder {
        private Long id;
        private String nom;
        private String description;
        private String code;
        private Filiere filiere;
        private Utilisateur enseignantResponsable;

        public ModuleUniBuilder id(Long id) { this.id = id; return this; }
        public ModuleUniBuilder nom(String nom) { this.nom = nom; return this; }
        public ModuleUniBuilder description(String description) { this.description = description; return this; }
        public ModuleUniBuilder code(String code) { this.code = code; return this; }
        public ModuleUniBuilder filiere(Filiere filiere) { this.filiere = filiere; return this; }
        public ModuleUniBuilder enseignantResponsable(Utilisateur enseignantResponsable) { this.enseignantResponsable = enseignantResponsable; return this; }

        public ModuleUni build() {
            return new ModuleUni(id, nom, description, code, filiere, enseignantResponsable);
        }
    }
}
