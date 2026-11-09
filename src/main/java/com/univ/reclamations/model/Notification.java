package com.univ.reclamations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entité représentant une notification envoyée à un utilisateur.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le message de la notification est obligatoire")
    @Size(max = 1000, message = "Le message ne doit pas dépasser 1000 caractères")
    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateNotification;

    @Column(nullable = false)
    private boolean lu = false;

    @Size(max = 500, message = "Le lien ne doit pas dépasser 500 caractères")
    @Column(length = 500)
    private String lien;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    @PrePersist
    protected void onCreate() {
        if (this.dateNotification == null) {
            this.dateNotification = LocalDateTime.now();
        }
    }

    // Constructors
    public Notification() {}

    public Notification(Long id, String message, LocalDateTime dateNotification, boolean lu, String lien, Utilisateur destinataire) {
        this.id = id;
        this.message = message;
        this.dateNotification = dateNotification;
        this.lu = lu;
        this.lien = lien;
        this.destinataire = destinataire;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDateNotification() { return dateNotification; }
    public void setDateNotification(LocalDateTime dateNotification) { this.dateNotification = dateNotification; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }

    public String getLien() { return lien; }
    public void setLien(String lien) { this.lien = lien; }

    public Utilisateur getDestinataire() { return destinataire; }
    public void setDestinataire(Utilisateur destinataire) { this.destinataire = destinataire; }

    // Builder
    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public static class NotificationBuilder {
        private Long id;
        private String message;
        private LocalDateTime dateNotification;
        private boolean lu = false;
        private String lien;
        private Utilisateur destinataire;

        public NotificationBuilder id(Long id) { this.id = id; return this; }
        public NotificationBuilder message(String message) { this.message = message; return this; }
        public NotificationBuilder dateNotification(LocalDateTime dateNotification) { this.dateNotification = dateNotification; return this; }
        public NotificationBuilder lu(boolean lu) { this.lu = lu; return this; }
        public NotificationBuilder lien(String lien) { this.lien = lien; return this; }
        public NotificationBuilder destinataire(Utilisateur destinataire) { this.destinataire = destinataire; return this; }

        public Notification build() {
            return new Notification(id, message, dateNotification, lu, lien, destinataire);
        }
    }
}
