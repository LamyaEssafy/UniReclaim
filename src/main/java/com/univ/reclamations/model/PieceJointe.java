package com.univ.reclamations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entité représentant une pièce jointe attachée à une réclamation.
 */
@Entity
@Table(name = "pieces_jointes")
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du fichier est obligatoire")
    @Size(max = 500, message = "Le nom du fichier ne doit pas dépasser 500 caractères")
    @Column(nullable = false, length = 500)
    private String nomFichier;

    @NotBlank(message = "Le nom original est obligatoire")
    @Size(max = 500, message = "Le nom original ne doit pas dépasser 500 caractères")
    @Column(nullable = false, length = 500)
    private String nomOriginal;

    @Size(max = 100, message = "Le type MIME ne doit pas dépasser 100 caractères")
    @Column(length = 100)
    private String typeMime;

    private Long taille;

    @NotBlank(message = "Le chemin du fichier est obligatoire")
    @Size(max = 1000, message = "Le chemin ne doit pas dépasser 1000 caractères")
    @Column(nullable = false, length = 1000)
    private String cheminFichier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reclamation_id", nullable = false)
    private Reclamation reclamation;

    // Constructors
    public PieceJointe() {}

    public PieceJointe(Long id, String nomFichier, String nomOriginal, String typeMime, Long taille, String cheminFichier, Reclamation reclamation) {
        this.id = id;
        this.nomFichier = nomFichier;
        this.nomOriginal = nomOriginal;
        this.typeMime = typeMime;
        this.taille = taille;
        this.cheminFichier = cheminFichier;
        this.reclamation = reclamation;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getNomOriginal() { return nomOriginal; }
    public void setNomOriginal(String nomOriginal) { this.nomOriginal = nomOriginal; }

    public String getTypeMime() { return typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }

    public Long getTaille() { return taille; }
    public void setTaille(Long taille) { this.taille = taille; }

    public String getCheminFichier() { return cheminFichier; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }

    public Reclamation getReclamation() { return reclamation; }
    public void setReclamation(Reclamation reclamation) { this.reclamation = reclamation; }

    // Builder
    public static PieceJointeBuilder builder() {
        return new PieceJointeBuilder();
    }

    public static class PieceJointeBuilder {
        private Long id;
        private String nomFichier;
        private String nomOriginal;
        private String typeMime;
        private Long taille;
        private String cheminFichier;
        private Reclamation reclamation;

        public PieceJointeBuilder id(Long id) { this.id = id; return this; }
        public PieceJointeBuilder nomFichier(String nomFichier) { this.nomFichier = nomFichier; return this; }
        public PieceJointeBuilder nomOriginal(String nomOriginal) { this.nomOriginal = nomOriginal; return this; }
        public PieceJointeBuilder typeMime(String typeMime) { this.typeMime = typeMime; return this; }
        public PieceJointeBuilder taille(Long taille) { this.taille = taille; return this; }
        public PieceJointeBuilder cheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; return this; }
        public PieceJointeBuilder reclamation(Reclamation reclamation) { this.reclamation = reclamation; return this; }

        public PieceJointe build() {
            return new PieceJointe(id, nomFichier, nomOriginal, typeMime, taille, cheminFichier, reclamation);
        }
    }
}
