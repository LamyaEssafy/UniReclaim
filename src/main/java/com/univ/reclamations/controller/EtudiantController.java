package com.univ.reclamations.controller;

import com.univ.reclamations.model.*;
import com.univ.reclamations.repository.CategorieRepository;
import com.univ.reclamations.repository.ModuleRepository;
import com.univ.reclamations.repository.PieceJointeRepository;
import com.univ.reclamations.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur gérant l'espace étudiant.
 * Permet aux étudiants de consulter leur tableau de bord,
 * gérer leur profil, créer et suivre leurs réclamations.
 */
@Controller
@RequestMapping("/etudiant")
public class EtudiantController {

    private final UtilisateurService utilisateurService;
    private final ReclamationService reclamationService;
    private final ReponseService reponseService;
    private final StorageService storageService;
    private final NotificationService notificationService;
    private final StatistiqueService statistiqueService;
    private final CategorieRepository categorieRepository;
    private final ModuleRepository moduleRepository;
    private final PieceJointeRepository pieceJointeRepository;

    /**
     * Constructeur avec injection de toutes les dépendances.
     *
     * @param utilisateurService service de gestion des utilisateurs
     * @param reclamationService service de gestion des réclamations
     * @param reponseService     service de gestion des réponses
     * @param storageService     service de stockage de fichiers
     * @param notificationService service de notifications
     * @param statistiqueService service de statistiques
     * @param categorieRepository dépôt des catégories
     * @param moduleRepository   dépôt des modules
     * @param pieceJointeRepository dépôt des pièces jointes
     */
    public EtudiantController(UtilisateurService utilisateurService,
                              ReclamationService reclamationService,
                              ReponseService reponseService,
                              StorageService storageService,
                              NotificationService notificationService,
                              StatistiqueService statistiqueService,
                              CategorieRepository categorieRepository,
                              ModuleRepository moduleRepository,
                              PieceJointeRepository pieceJointeRepository) {
        this.utilisateurService = utilisateurService;
        this.reclamationService = reclamationService;
        this.reponseService = reponseService;
        this.storageService = storageService;
        this.notificationService = notificationService;
        this.statistiqueService = statistiqueService;
        this.categorieRepository = categorieRepository;
        this.moduleRepository = moduleRepository;
        this.pieceJointeRepository = pieceJointeRepository;
    }

    /**
     * Récupère l'utilisateur actuellement connecté.
     *
     * @return l'utilisateur courant
     */
    private Utilisateur getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return utilisateurService.findByEmail(email);
    }

    /**
     * Ajoute l'utilisateur courant et le nombre de notifications non lues
     * au modèle pour chaque requête traitée par ce contrôleur.
     *
     * @param model le modèle Spring MVC
     */
    @ModelAttribute
    public void addCommonAttributes(Model model, jakarta.servlet.http.HttpServletRequest request) {
        Utilisateur currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        if (currentUser != null) {
            model.addAttribute("notificationCount", notificationService.countNonLues(currentUser));
        }
        model.addAttribute("currentURI", request.getRequestURI());
    }

    // ======================== DASHBOARD ========================

    /**
     * Affiche le tableau de bord de l'étudiant avec ses statistiques personnelles
     * et ses réclamations récentes (les 5 dernières).
     *
     * @param model le modèle Spring MVC
     * @return la vue etudiant/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            Utilisateur currentUser = getCurrentUser();
            Map<String, Long> statsEtudiant = statistiqueService.getStatsEtudiant(currentUser);
            model.addAttribute("statsEtudiant", statsEtudiant);

            List<Reclamation> reclamations = reclamationService.findByEtudiant(currentUser);
            List<Reclamation> recentReclamations = reclamations.stream()
                    .limit(5)
                    .toList();
            model.addAttribute("recentReclamations", recentReclamations);
            model.addAttribute("reclamationsRecentes", recentReclamations);
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
        return "etudiant/dashboard";
    }

    // ======================== PROFIL ========================

    /**
     * Affiche le profil de l'étudiant connecté.
     *
     * @param model le modèle Spring MVC
     * @return la vue etudiant/profil
     */
    @GetMapping("/profil")
    public String profil(Model model) {
        model.addAttribute("utilisateur", getCurrentUser());
        return "etudiant/profil";
    }

    /**
     * Met à jour le profil de l'étudiant.
     *
     * @param utilisateur      les données mises à jour
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la page de profil
     */
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
        return "redirect:/etudiant/profil";
    }

    // ======================== RECLAMATIONS ========================

    /**
     * Affiche le formulaire de création d'une nouvelle réclamation.
     *
     * @param model le modèle Spring MVC
     * @return la vue etudiant/reclamation-form
     */
    @GetMapping("/reclamations/nouvelle")
    public String nouvelleReclamation(Model model) {
        model.addAttribute("reclamation", new Reclamation());
        model.addAttribute("categories", categorieRepository.findAll());
        model.addAttribute("modules", moduleRepository.findAll());
        return "etudiant/reclamation-form";
    }

    /**
     * Crée une nouvelle réclamation avec possibilité de joindre des fichiers.
     * Les fichiers sont stockés via le StorageService et des entités PieceJointe
     * sont créées pour chaque fichier.
     *
     * @param reclamation      la réclamation à créer
     * @param fichiers         les fichiers joints (optionnels)
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des réclamations
     */
    @PostMapping("/reclamations")
    public String creerReclamation(@ModelAttribute("reclamation") Reclamation reclamation,
                                    @RequestParam("categorieId") Long categorieId,
                                    @RequestParam(value = "moduleId", required = false) Long moduleId,
                                    @RequestParam(value = "files", required = false) MultipartFile[] fichiers,
                                    RedirectAttributes redirectAttributes) {
        try {
            Utilisateur currentUser = getCurrentUser();
            reclamation.setEtudiant(currentUser);
            
            if (categorieId != null) {
                reclamation.setCategorie(categorieRepository.findById(categorieId).orElse(null));
            }
            if (moduleId != null) {
                reclamation.setModule(moduleRepository.findById(moduleId).orElse(null));
            }
            
            Reclamation savedReclamation = reclamationService.creerReclamation(reclamation);

            // Traitement des pièces jointes
            if (fichiers != null) {
                for (MultipartFile fichier : fichiers) {
                    if (!fichier.isEmpty()) {
                        String filename = storageService.store(fichier);
                        PieceJointe pieceJointe = new PieceJointe();
                        pieceJointe.setNomFichier(filename);
                        pieceJointe.setNomOriginal(fichier.getOriginalFilename());
                        pieceJointe.setCheminFichier(filename);
                        pieceJointe.setTypeMime(fichier.getContentType());
                        pieceJointe.setTaille(fichier.getSize());
                        pieceJointe.setReclamation(savedReclamation);
                        pieceJointeRepository.save(pieceJointe);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success", "Réclamation créée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
        }
        return "redirect:/etudiant/reclamations";
    }

    /**
     * Affiche la liste de toutes les réclamations de l'étudiant connecté.
     *
     * @param model le modèle Spring MVC
     * @return la vue etudiant/mes-reclamations
     */
    @GetMapping("/reclamations")
    public String mesReclamations(Model model) {
        try {
            Utilisateur currentUser = getCurrentUser();
            List<Reclamation> reclamations = reclamationService.findByEtudiant(currentUser);
            model.addAttribute("reclamations", reclamations);
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des réclamations : " + e.getMessage());
        }
        return "etudiant/mes-reclamations";
    }

    /**
     * Affiche le détail d'une réclamation avec ses réponses et pièces jointes.
     *
     * @param id    identifiant de la réclamation
     * @param model le modèle Spring MVC
     * @return la vue etudiant/reclamation-detail
     */
    @GetMapping("/reclamations/{id}")
    public String detailReclamation(@PathVariable Long id, Model model) {
        try {
            Reclamation reclamation = reclamationService.findById(id);

            // Vérification que la réclamation appartient bien à l'étudiant
            Utilisateur currentUser = getCurrentUser();
            if (!reclamation.getEtudiant().getId().equals(currentUser.getId())) {
                return "redirect:/etudiant/reclamations";
            }

            model.addAttribute("reclamation", reclamation);
            model.addAttribute("reponses", reponseService.findByReclamation(reclamation));
            model.addAttribute("piecesJointes", pieceJointeRepository.findByReclamation(reclamation));
        } catch (Exception e) {
            return "redirect:/etudiant/reclamations";
        }
        return "etudiant/reclamation-detail";
    }
}
