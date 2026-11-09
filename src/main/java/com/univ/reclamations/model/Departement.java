package com.univ.reclamations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un département universitaire.
 */
@Entity
@Table(name = "departements")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du département est obligatoire")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    @Column(nullable = false, length = 200)
    private String nom;

    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Filiere> filieres = new ArrayList<>();

    // Constructors
    public Departement() {}

    public Departement(Long id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Filiere> getFilieres() { return filieres; }
    public void setFilieres(List<Filiere> filieres) { this.filieres = filieres; }

    // Builder
    public static DepartementBuilder builder() {
        return new DepartementBuilder();
    }

    public static class DepartementBuilder {
        private Long id;
        private String nom;
        private String description;

        public DepartementBuilder id(Long id) { this.id = id; return this; }
        public DepartementBuilder nom(String nom) { this.nom = nom; return this; }
        public DepartementBuilder description(String description) { this.description = description; return this; }

        public Departement build() {
            return new Departement(id, nom, description);
        }
    }
}
