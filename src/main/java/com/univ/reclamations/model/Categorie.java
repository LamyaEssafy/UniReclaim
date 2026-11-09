package com.univ.reclamations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entité représentant une catégorie de réclamation.
 */
@Entity
@Table(name = "categories")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    @Column(nullable = false, length = 200)
    private String nom;

    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String description;

    @NotBlank(message = "Le type de destinataire est obligatoire")
    @Size(max = 50, message = "Le type ne doit pas dépasser 50 caractères")
    @Column(nullable = false, length = 50)
    private String destinataireType;

    // Constructors
    public Categorie() {}

    public Categorie(Long id, String nom, String description, String destinataireType) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.destinataireType = destinataireType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDestinataireType() { return destinataireType; }
    public void setDestinataireType(String destinataireType) { this.destinataireType = destinataireType; }

    // Builder pattern
    public static CategorieBuilder builder() {
        return new CategorieBuilder();
    }

    public static class CategorieBuilder {
        private Long id;
        private String nom;
        private String description;
        private String destinataireType;

        public CategorieBuilder id(Long id) { this.id = id; return this; }
        public CategorieBuilder nom(String nom) { this.nom = nom; return this; }
        public CategorieBuilder description(String description) { this.description = description; return this; }
        public CategorieBuilder destinataireType(String destinataireType) { this.destinataireType = destinataireType; return this; }

        public Categorie build() {
            return new Categorie(id, nom, description, destinataireType);
        }
    }
}
