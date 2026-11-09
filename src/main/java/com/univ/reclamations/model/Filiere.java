package com.univ.reclamations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entité représentant une filière universitaire.
 */
@Entity
@Table(name = "filieres")
public class Filiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la filière est obligatoire")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    @Column(nullable = false, length = 200)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;

    // Constructors
    public Filiere() {}

    public Filiere(Long id, String nom, Departement departement) {
        this.id = id;
        this.nom = nom;
        this.departement = departement;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }

    // Builder
    public static FiliereBuilder builder() {
        return new FiliereBuilder();
    }

    public static class FiliereBuilder {
        private Long id;
        private String nom;
        private Departement departement;

        public FiliereBuilder id(Long id) { this.id = id; return this; }
        public FiliereBuilder nom(String nom) { this.nom = nom; return this; }
        public FiliereBuilder departement(Departement departement) { this.departement = departement; return this; }

        public Filiere build() {
            return new Filiere(id, nom, departement);
        }
    }
}
