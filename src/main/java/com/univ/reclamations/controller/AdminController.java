package com.univ.reclamations.controller;

import com.univ.reclamations.model.*;
import com.univ.reclamations.repository.CategorieRepository;
import com.univ.reclamations.repository.DepartementRepository;
import com.univ.reclamations.repository.FiliereRepository;
import com.univ.reclamations.repository.ModuleRepository;
import com.univ.reclamations.service.NotificationService;
import com.univ.reclamations.service.StatistiqueService;
import com.univ.reclamations.service.UtilisateurService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Contrôleur d'administration de la plateforme.
 * Gère le tableau de bord, les utilisateurs, les départements,
 * les filières, les modules, les catégories et les statistiques.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UtilisateurService utilisateurService;
    private final StatistiqueService statistiqueService;
    private final NotificationService notificationService;
    private final DepartementRepository departementRepository;
    private final FiliereRepository filiereRepository;
    private final ModuleRepository moduleRepository;
    private final CategorieRepository categorieRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructeur avec injection de toutes les dépendances nécessaires.
     *
     * @param utilisateurService   service de gestion des utilisateurs
     * @param statistiqueService   service de statistiques
     * @param notificationService  service de notifications
     * @param departementRepository dépôt des départements
     * @param filiereRepository    dépôt des filières
     * @param moduleRepository     dépôt des modules
     * @param categorieRepository  dépôt des catégories
     * @param objectMapper         sérialiseur JSON
     */
    public AdminController(UtilisateurService utilisateurService,
                           StatistiqueService statistiqueService,
                           NotificationService notificationService,
                           DepartementRepository departementRepository,
                           FiliereRepository filiereRepository,
                           ModuleRepository moduleRepository,
                           CategorieRepository categorieRepository,
                           ObjectMapper objectMapper) {
        this.utilisateurService = utilisateurService;
        this.statistiqueService = statistiqueService;
        this.notificationService = notificationService;
        this.departementRepository = departementRepository;
        this.filiereRepository = filiereRepository;
        this.moduleRepository = moduleRepository;
        this.categorieRepository = categorieRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Ajoute l'utilisateur courant et le nombre de notifications non lues
     * au modèle pour chaque requête traitée par ce contrôleur.
     *
     * @param model le modèle Spring MVC
     */
    @ModelAttribute
    public void addCommonAttributes(Model model, jakarta.servlet.http.HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Utilisateur currentUser = utilisateurService.findByEmail(email);
        model.addAttribute("currentUser", currentUser);
        if (currentUser != null) {
            model.addAttribute("notificationCount", notificationService.countNonLues(currentUser));
        }
        model.addAttribute("currentURI", request.getRequestURI());
    }

    // ======================== DASHBOARD ========================

    /**
     * Affiche le tableau de bord administrateur avec les statistiques globales.
     *
     * @param model le modèle Spring MVC
     * @return la vue admin/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            Map<String, Long> statsGlobales = statistiqueService.getStatsGlobales();
            model.addAttribute("statsGlobales", statsGlobales);

            Map<String, Long> reclamationsParCategorie = statistiqueService.getReclamationsParCategorie();
            Map<String, Long> reclamationsParStatut = statistiqueService.getReclamationsParStatut();
            Map<String, Long> reclamationsParDepartement = statistiqueService.getReclamationsParDepartement();
            Map<String, Long> reclamationsParMois = statistiqueService.getReclamationsParMois();

            model.addAttribute("reclamationsParCategorieLabels", toJson(reclamationsParCategorie.keySet()));
            model.addAttribute("reclamationsParCategorieValues", toJson(reclamationsParCategorie.values()));
            model.addAttribute("reclamationsParStatutLabels", toJson(reclamationsParStatut.keySet()));
            model.addAttribute("reclamationsParStatutValues", toJson(reclamationsParStatut.values()));
            model.addAttribute("reclamationsParDepartementLabels", toJson(reclamationsParDepartement.keySet()));
            model.addAttribute("reclamationsParDepartementValues", toJson(reclamationsParDepartement.values()));
            model.addAttribute("reclamationsParMoisLabels", toJson(reclamationsParMois.keySet()));
            model.addAttribute("reclamationsParMoisValues", toJson(reclamationsParMois.values()));
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des statistiques : " + e.getMessage());
        }
        return "admin/dashboard";
    }

    // ======================== UTILISATEURS ========================

    /**
     * Affiche la liste de tous les utilisateurs.
     *
     * @param model le modèle Spring MVC
     * @return la vue admin/utilisateurs
     */
    @GetMapping("/utilisateurs")
    public String listeUtilisateurs(Model model) {
        model.addAttribute("utilisateurs", utilisateurService.findAll());
        model.addAttribute("roles", Role.values());
        model.addAttribute("departements", departementRepository.findAll());
        return "admin/utilisateurs";
    }

    /**
     * Affiche le formulaire de création d'un nouvel utilisateur.
     *
     * @param model le modèle Spring MVC
     * @return la vue admin/utilisateur-form
     */
    @GetMapping("/utilisateurs/nouveau")
    public String nouveauUtilisateur(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("roles", Role.values());
        model.addAttribute("departements", departementRepository.findAll());
        model.addAttribute("filieres", filiereRepository.findAll());
        return "admin/utilisateur-form";
    }

    /**
     * Affiche le formulaire de modification d'un utilisateur existant.
     *
     * @param id    identifiant de l'utilisateur à modifier
     * @param model le modèle Spring MVC
     * @return la vue admin/utilisateur-form
     */
    @GetMapping("/utilisateurs/{id}/modifier")
    public String modifierUtilisateur(@PathVariable Long id, Model model) {
        try {
            Utilisateur utilisateur = utilisateurService.findById(id);
            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("roles", Role.values());
            model.addAttribute("departements", departementRepository.findAll());
            model.addAttribute("filieres", filiereRepository.findAll());
            return "admin/utilisateur-form";
        } catch (Exception e) {
            return "redirect:/admin/utilisateurs";
        }
    }

    /**
     * Enregistre ou met à jour un utilisateur.
     *
     * @param utilisateur      les données de l'utilisateur
     * @param redirectAttributes attributs de redirection pour les messages flash
     * @return redirection vers la liste des utilisateurs
     */
    @PostMapping("/utilisateurs")
    public String sauvegarderUtilisateur(@ModelAttribute("utilisateur") Utilisateur utilisateur,
                                         RedirectAttributes redirectAttributes) {
        try {
            if (utilisateur.getId() != null) {
                utilisateurService.update(utilisateur);
                redirectAttributes.addFlashAttribute("success", "Utilisateur modifié avec succès.");
            } else {
                utilisateurService.save(utilisateur);
                redirectAttributes.addFlashAttribute("success", "Utilisateur créé avec succès.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }

    /**
     * Supprime un utilisateur par son identifiant.
     *
     * @param id                identifiant de l'utilisateur
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des utilisateurs
     */
    @GetMapping("/utilisateurs/{id}/supprimer")
    public String supprimerUtilisateur(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }

    /**
     * Active ou désactive un utilisateur.
     *
     * @param id                identifiant de l'utilisateur
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des utilisateurs
     */
    @GetMapping("/utilisateurs/{id}/toggle")
    public String toggleUtilisateur(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.toggleActive(id);
            redirectAttributes.addFlashAttribute("success", "Statut de l'utilisateur modifié avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }

    // ======================== DEPARTEMENTS ========================

    /**
     * Affiche la liste de tous les départements.
     *
     * @param model le modèle Spring MVC
     * @return la vue admin/departements
     */
    @GetMapping("/departements")
    public String listeDepartements(Model model) {
        model.addAttribute("departements", departementRepository.findAll());
        model.addAttribute("departement", new Departement());
        return "admin/departements";
    }

    /**
     * Enregistre un nouveau département.
     *
     * @param departement      le département à sauvegarder
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des départements
     */
    @PostMapping("/departements")
    public String sauvegarderDepartement(@ModelAttribute("departement") Departement departement,
                                          RedirectAttributes redirectAttributes) {
        try {
            departementRepository.save(departement);
            redirectAttributes.addFlashAttribute("success", "Département créé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/departements";
    }

    /**
     * Supprime un département par son identifiant.
     *
     * @param id                identifiant du département
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des départements
     */
    @GetMapping("/departements/{id}/supprimer")
    public String supprimerDepartement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departementRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Département supprimé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/departements";
    }

    /**
     * Met à jour un département existant.
     *
     * @param id                identifiant du département
     * @param departement      les nouvelles données du département
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des départements
     */
    @PostMapping("/departements/{id}/modifier")
    public String modifierDepartement(@PathVariable Long id,
                                       @ModelAttribute("departement") Departement departement,
                                       RedirectAttributes redirectAttributes) {
        try {
            departement.setId(id);
            departementRepository.save(departement);
            redirectAttributes.addFlashAttribute("success", "Département modifié avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/departements";
    }

    // ======================== FILIERES ========================

    /**
     * Affiche la liste de toutes les filières.
     *
     * @param model le modèle Spring MVC
     * @return la vue admin/filieres
     */
    @GetMapping("/filieres")
    public String listeFilieres(Model model) {
        model.addAttribute("filieres", filiereRepository.findAll());
        model.addAttribute("departements", departementRepository.findAll());
        model.addAttribute("filiere", new Filiere());
        return "admin/filieres";
    }

    /**
     * Enregistre une nouvelle filière associée à un département.
     *
     * @param filiere          la filière à sauvegarder
     * @param departementId    identifiant du département parent
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des filières
     */
    @PostMapping("/filieres")
    public String sauvegarderFiliere(@ModelAttribute("filiere") Filiere filiere,
                                      @RequestParam("departementId") Long departementId,
                                      RedirectAttributes redirectAttributes) {
        try {
            Departement departement = departementRepository.findById(departementId)
                    .orElseThrow(() -> new RuntimeException("Département non trouvé"));
            filiere.setDepartement(departement);
            filiereRepository.save(filiere);
            redirectAttributes.addFlashAttribute("success", "Filière créée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/filieres";
    }

    /**
     * Supprime une filière par son identifiant.
     *
     * @param id                identifiant de la filière
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des filières
     */
    @GetMapping("/filieres/{id}/supprimer")
    public String supprimerFiliere(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            filiereRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Filière supprimée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/filieres";
    }

    // ======================== MODULES ========================

    /**
     * Affiche la liste de tous les modules.
     *
     * @param model le modèle Spring MVC
     * @return la vue admin/modules
     */
    @GetMapping("/modules")
    public String listeModules(Model model) {
        model.addAttribute("modules", moduleRepository.findAll());
        model.addAttribute("filieres", filiereRepository.findAll());
        model.addAttribute("enseignants", utilisateurService.findByRole(Role.ENSEIGNANT));
        model.addAttribute("module", new ModuleUni());
        return "admin/modules";
    }

    /**
     * Enregistre un nouveau module associé à une filière et un enseignant.
     *
     * @param moduleUni        le module à sauvegarder
     * @param filiereId        identifiant de la filière
     * @param enseignantId     identifiant de l'enseignant responsable
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des modules
     */
    @PostMapping("/modules")
    public String sauvegarderModule(@ModelAttribute("module") ModuleUni moduleUni,
                                     @RequestParam("filiereId") Long filiereId,
                                     @RequestParam("enseignantId") Long enseignantId,
                                     RedirectAttributes redirectAttributes) {
        try {
            Filiere filiere = filiereRepository.findById(filiereId)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée"));
            Utilisateur enseignant = utilisateurService.findById(enseignantId);

            moduleUni.setFiliere(filiere);
            moduleUni.setEnseignantResponsable(enseignant); // ✅ Ligne corrigée
            moduleRepository.save(moduleUni);

            redirectAttributes.addFlashAttribute("success", "Module créé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/modules";
    }

    /**
     * Supprime un module par son identifiant.
     *
     * @param id                identifiant du module
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des modules
     */
    @GetMapping("/modules/{id}/supprimer")
    public String supprimerModule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            moduleRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Module supprimé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/modules";
    }

    // ======================== CATEGORIES ========================

    /**
     * Affiche la liste de toutes les catégories de réclamation.
     *
     * @param model le modèle Spring MVC
     * @return la vue admin/categories
     */
    @GetMapping("/categories")
    public String listeCategories(Model model) {
        model.addAttribute("categories", categorieRepository.findAll());
        model.addAttribute("categorie", new Categorie());
        return "admin/categories";
    }

    /**
     * Enregistre une nouvelle catégorie de réclamation.
     *
     * @param categorie        la catégorie à sauvegarder
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des catégories
     */
    @PostMapping("/categories")
    public String sauvegarderCategorie(@ModelAttribute("categorie") Categorie categorie,
                                        RedirectAttributes redirectAttributes) {
        try {
            categorieRepository.save(categorie);
            redirectAttributes.addFlashAttribute("success", "Catégorie créée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * Supprime une catégorie par son identifiant.
     *
     * @param id                identifiant de la catégorie
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des catégories
     */
    @GetMapping("/categories/{id}/supprimer")
    public String supprimerCategorie(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categorieRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Catégorie supprimée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ======================== STATISTIQUES ========================

    /**
     * Affiche la page de statistiques complètes de la plateforme.
     *
     * @param model le modèle Spring MVC
     * @return la vue admin/statistiques
     */
    @GetMapping("/statistiques")
    public String statistiques(Model model) {
        try {
            model.addAttribute("statsGlobales", statistiqueService.getStatsGlobales());
            model.addAttribute("reclamationsParCategorie", statistiqueService.getReclamationsParCategorie());
            model.addAttribute("reclamationsParStatut", statistiqueService.getReclamationsParStatut());
            model.addAttribute("reclamationsParDepartement", statistiqueService.getReclamationsParDepartement());
            model.addAttribute("reclamationsParMois", statistiqueService.getReclamationsParMois());
            model.addAttribute("enseignantsLesPlusSollicites", statistiqueService.getEnseignantsLesPlusSollicites());

            // Versions JSON pour les graphiques
            model.addAttribute("reclamationsParCategorieJson", toJson(statistiqueService.getReclamationsParCategorie()));
            model.addAttribute("reclamationsParStatutJson", toJson(statistiqueService.getReclamationsParStatut()));
            model.addAttribute("reclamationsParDepartementJson", toJson(statistiqueService.getReclamationsParDepartement()));
            model.addAttribute("reclamationsParMoisJson", toJson(statistiqueService.getReclamationsParMois()));
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des statistiques : " + e.getMessage());
        }
        return "admin/statistiques";
    }

    // ======================== UTILITAIRES ========================

    /**
     * Convertit un objet en chaîne JSON pour les graphiques JavaScript.
     *
     * @param data l'objet à sérialiser
     * @return la chaîne JSON correspondante
     */
    private String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Utilisateur getCurrentUser() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return utilisateurService.findByEmail(auth.getName());
    }

    @GetMapping("/profil")
    public String profil(Model model) {
        model.addAttribute("utilisateur", getCurrentUser());
        return "admin/profil";
    }

    @PostMapping("/profil")
    public String updateProfil(@ModelAttribute("utilisateur") Utilisateur utilisateur,
                               @RequestParam(value = "password", required = false) String password,
                               RedirectAttributes redirectAttributes) {
        try {
            Utilisateur currentUser = getCurrentUser();
            currentUser.setNom(utilisateur.getNom());
            currentUser.setPrenom(utilisateur.getPrenom());
            currentUser.setTelephone(utilisateur.getTelephone());
            if (password != null && !password.isBlank()) {
                currentUser.setMotDePasse(password);
            }
            utilisateurService.update(currentUser);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du profil : " + e.getMessage());
        }
        return "redirect:/admin/profil";
    }
}
