package com.univ.reclamations.config;

import com.univ.reclamations.model.*;
import com.univ.reclamations.repository.CategorieRepository;
import com.univ.reclamations.repository.DepartementRepository;
import com.univ.reclamations.repository.FiliereRepository;
import com.univ.reclamations.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Initialiseur de données au démarrage de l'application.
 * <p>
 * Crée les données de base nécessaires au fonctionnement de l'application
 * si elles n'existent pas encore :
 * <ul>
 *   <li>Compte administrateur par défaut</li>
 *   <li>Catégories de réclamations</li>
 *   <li>Départements de l'université</li>
 *   <li>Filières associées aux départements</li>
 * </ul>
 * </p>
 * <p>
 * L'initialisation ne s'exécute que si aucun administrateur n'existe en base.
 * </p>
 *
 * @author Système de Réclamations Universitaires
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final DepartementRepository departementRepository;
    private final FiliereRepository filiereRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructeur avec injection de toutes les dépendances nécessaires à l'initialisation.
     *
     * @param utilisateurRepository le repository des utilisateurs
     * @param categorieRepository   le repository des catégories
     * @param departementRepository le repository des départements
     * @param filiereRepository     le repository des filières
     * @param passwordEncoder       l'encodeur de mots de passe
     */
    public DataInitializer(UtilisateurRepository utilisateurRepository,
                           CategorieRepository categorieRepository,
                           DepartementRepository departementRepository,
                           FiliereRepository filiereRepository,
                           PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.categorieRepository = categorieRepository;
        this.departementRepository = departementRepository;
        this.filiereRepository = filiereRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Point d'entrée de l'initialisation exécuté au démarrage de l'application.
     * <p>
     * Vérifie si un administrateur existe déjà. Si ce n'est pas le cas,
     * procède à l'initialisation complète des données de base.
     * </p>
     *
     * @param args les arguments de la ligne de commande (non utilisés)
     */
    @Override
    @Transactional
    public void run(String... args) {
        List<Utilisateur> admins = utilisateurRepository.findByRole(Role.ADMIN);

        if (admins.isEmpty()) {
            log.info("=== Initialisation des données de base ===");

            creerAdminParDefaut();
            creerCategories();
            creerDepartementsEtFilieres();

            log.info("=== Initialisation terminée avec succès ===");
        } else {
            log.info("Données déjà initialisées. {} administrateur(s) trouvé(s).", admins.size());
        }
    }

    /**
     * Crée le compte administrateur par défaut.
     * <p>
     * Identifiants par défaut :
     * <ul>
     *   <li>Email : admin@univ.ma</li>
     *   <li>Mot de passe : admin123</li>
     * </ul>
     * </p>
     */
    private void creerAdminParDefaut() {
        Utilisateur admin = new Utilisateur();
        admin.setPrenom("Admin");
        admin.setNom("Système");
        admin.setEmail("admin@univ.ma");
        admin.setMotDePasse(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setActif(true);
        admin.setDateCreation(LocalDateTime.now());

        utilisateurRepository.save(admin);
        log.info("Administrateur créé : admin@univ.ma / admin123");
    }

    /**
     * Crée les catégories de réclamations prédéfinies.
     * <p>
     * Chaque catégorie a un type de destinataire qui détermine
     * le routage automatique de la réclamation.
     * </p>
     */
    private void creerCategories() {
        creerCategorie("Réclamation de note",
                "Réclamation concernant une note d'examen ou de contrôle continu",
                "MODULE");
        creerCategorie("Réclamation d'absence",
                "Réclamation concernant une absence non justifiée ou mal enregistrée",
                "ENSEIGNANT");
        creerCategorie("Réclamation d'inscription",
                "Réclamation concernant l'inscription administrative ou pédagogique",
                "SCOLARITE");
        creerCategorie("Réclamation de stage",
                "Réclamation concernant les stages ou les conventions de stage",
                "CHEF");
        creerCategorie("Réclamation de bourse",
                "Réclamation concernant les bourses d'études ou aides financières",
                "CHEF");
        creerCategorie("Réclamation administrative",
                "Réclamation concernant les services administratifs de l'université",
                "SCOLARITE");
        creerCategorie("Réclamation emploi du temps",
                "Réclamation concernant les emplois du temps et la planification des cours",
                "CHEF");
        creerCategorie("Autre",
                "Autre type de réclamation ne rentrant dans aucune catégorie prédéfinie",
                "CHEF");

        log.info("8 catégories de réclamations créées.");
    }

    /**
     * Crée les départements et filières de l'université.
     */
    private void creerDepartementsEtFilieres() {
        // Département Informatique avec ses filières
        Departement informatique = creerDepartement("Informatique",
                "Département des sciences informatiques et du numérique");
        creerFiliere("SMI", informatique);
        creerFiliere("SMA", informatique);

        // Département Mathématiques
        creerDepartement("Mathématiques",
                "Département des mathématiques pures et appliquées");

        // Département Physique
        creerDepartement("Physique",
                "Département des sciences physiques et de l'ingénierie");

        log.info("3 départements et 2 filières créés.");
    }

    /**
     * Crée une catégorie de réclamation.
     *
     * @param nom              le nom de la catégorie
     * @param description      la description de la catégorie
     * @param destinataireType le type de destinataire (MODULE, ENSEIGNANT, SCOLARITE, CHEF)
     */
    private void creerCategorie(String nom, String description, String destinataireType) {
        Categorie categorie = new Categorie();
        categorie.setNom(nom);
        categorie.setDescription(description);
        categorie.setDestinataireType(destinataireType);
        categorieRepository.save(categorie);
    }

    /**
     * Crée un département.
     *
     * @param nom         le nom du département
     * @param description la description du département
     * @return le département créé et sauvegardé
     */
    private Departement creerDepartement(String nom, String description) {
        Departement departement = new Departement();
        departement.setNom(nom);
        departement.setDescription(description);
        return departementRepository.save(departement);
    }

    /**
     * Crée une filière associée à un département.
     *
     * @param nom         le nom de la filière
     * @param departement le département de rattachement
     * @return la filière créée et sauvegardée
     */
    private Filiere creerFiliere(String nom, Departement departement) {
        Filiere filiere = new Filiere();
        filiere.setNom(nom);
        filiere.setDepartement(departement);
        return filiereRepository.save(filiere);
    }
}
