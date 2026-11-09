package com.univ.reclamations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entité représentant un utilisateur du système de réclamations.
 */
@Entity
@Table(name = "utilisateurs", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_utilisateur_email")
})
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(nullable = false)
    private String motDePasse;

    @Size(max = 20, message = "Le téléphone ne doit pas dépasser 20 caractères")
    @Column(length = 20)
    private String telephone;

    @Column(length = 500)
    private String avatar;

    @Column(nullable = false)
    private boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }

    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }

    public String getInitiale() {
        if (this.prenom == null || this.prenom.trim().isEmpty()) {
            return "U";
        }
        return String.valueOf(this.prenom.trim().charAt(0)).toUpperCase();
    }

    // Constructors
    public Utilisateur() {}

    public Utilisateur(Long id, String prenom, String nom, String email, String motDePasse, String telephone, String avatar, boolean actif, Role role, LocalDateTime dateCreation, Departement departement, Filiere filiere) {
        this.id = id;
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.avatar = avatar;
        this.actif = actif;
        this.role = role;
        this.dateCreation = dateCreation;
        this.departement = departement;
        this.filiere = filiere;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }

    public Filiere getFiliere() { return filiere; }
    public void setFiliere(Filiere filiere) { this.filiere = filiere; }

    // Builder
    public static UtilisateurBuilder builder() {
        return new UtilisateurBuilder();
    }

    public static class UtilisateurBuilder {
        private Long id;
        private String prenom;
        private String nom;
        private String email;
        private String motDePasse;
        private String telephone;
        private String avatar;
        private boolean actif = true;
        private Role role;
        private LocalDateTime dateCreation;
        private Departement departement;
        private Filiere filiere;

        public UtilisateurBuilder id(Long id) { this.id = id; return this; }
        public UtilisateurBuilder prenom(String prenom) { this.prenom = prenom; return this; }
        public UtilisateurBuilder nom(String nom) { this.nom = nom; return this; }
        public UtilisateurBuilder email(String email) { this.email = email; return this; }
        public UtilisateurBuilder motDePasse(String motDePasse) { this.motDePasse = motDePasse; return this; }
        public UtilisateurBuilder telephone(String telephone) { this.telephone = telephone; return this; }
        public UtilisateurBuilder avatar(String avatar) { this.avatar = avatar; return this; }
        public UtilisateurBuilder actif(boolean actif) { this.actif = actif; return this; }
        public UtilisateurBuilder role(Role role) { this.role = role; return this; }
        public UtilisateurBuilder dateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; return this; }
        public UtilisateurBuilder departement(Departement departement) { this.departement = departement; return this; }
        public UtilisateurBuilder filiere(Filiere filiere) { this.filiere = filiere; return this; }

        public Utilisateur build() {
            return new Utilisateur(id, prenom, nom, email, motDePasse, telephone, avatar, actif, role, dateCreation, departement, filiere);
        }
    }
}
