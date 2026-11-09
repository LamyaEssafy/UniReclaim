package com.univ.reclamations.controller;

import com.univ.reclamations.model.*;
import com.univ.reclamations.repository.PieceJointeRepository;
import com.univ.reclamations.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur gérant l'espace du chef de département.
 * Permet au chef de consulter les réclamations de son département,
 * d'affecter des enseignants, de valider les réponses, clôturer ou rejeter.
 */
@Controller
@RequestMapping("/chef")
public class ChefController {

    private final UtilisateurService utilisateurService;
    private final ReclamationService reclamationService;
    private final ReponseService reponseService;
    private final NotificationService notificationService;
    private final StatistiqueService statistiqueService;
    private final PieceJointeRepository pieceJointeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructeur avec injection de toutes les dépendances.
     *
     * @param utilisateurService service de gestion des utilisateurs
     * @param reclamationService service de gestion des réclamations
     * @param reponseService     service de gestion des réponses
     * @param notificationService service de notifications
     * @param statistiqueService service de statistiques
     * @param pieceJointeRepository dépôt des pièces jointes
     */
    public ChefController(UtilisateurService utilisateurService,
                          ReclamationService reclamationService,
                          ReponseService reponseService,
                          NotificationService notificationService,
                          StatistiqueService statistiqueService,
                          PieceJointeRepository pieceJointeRepository) {
        this.utilisateurService = utilisateurService;
        this.reclamationService = reclamationService;
        this.reponseService = reponseService;
        this.notificationService = notificationService;
        this.statistiqueService = statistiqueService;
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
     * Affiche le tableau de bord du chef de département avec les statistiques
     * de son département, les enseignants les plus sollicités et la répartition
     * des réclamations par statut.
     *
     * @param model le modèle Spring MVC
     * @return la vue chef/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            Utilisateur currentUser = getCurrentUser();
            Departement departement = currentUser.getDepartement();

            if (departement != null) {
                Map<String, Long> statsDepartement = statistiqueService.getStatsDepartement(departement);
                model.addAttribute("statsDepartement", statsDepartement);

                Map<String, Long> enseignantsLesPlusSollicites =
                        statistiqueService.getEnseignantsLesPlusSollicites(departement);
                model.addAttribute("enseignantsLesPlusSollicites", enseignantsLesPlusSollicites);

                Map<String, Long> reclamationsParStatut = statistiqueService.getReclamationsParStatut();
                model.addAttribute("reclamationsParStatutLabels", toJson(reclamationsParStatut.keySet()));
                model.addAttribute("reclamationsParStatutValues", toJson(reclamationsParStatut.values()));
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
        return "chef/dashboard";
    }

    @GetMapping("/statistiques")
    public String statistiques(Model model) {
        try {
            Utilisateur currentUser = getCurrentUser();
            Departement departement = currentUser.getDepartement();

            if (departement != null) {
                Map<String, Long> statsDepartement = statistiqueService.getStatsDepartement(departement);
                model.addAttribute("statsGlobales", statsDepartement);

                Map<String, Long> enseignantsLesPlusSollicites =
                        statistiqueService.getEnseignantsLesPlusSollicites(departement);
                model.addAttribute("enseignantsLesPlusSollicites", enseignantsLesPlusSollicites);

                Map<String, Long> reclamationsParMois = statistiqueService.getReclamationsParMois(departement);
                model.addAttribute("reclamationsParMoisJson", toJson(reclamationsParMois));

                Map<String, Long> reclamationsParCategorie = statistiqueService.getReclamationsParCategorie(departement);
                model.addAttribute("reclamationsParCategorieJson", toJson(reclamationsParCategorie));
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des statistiques : " + e.getMessage());
        }
        return "chef/statistiques";
    }

    // ======================== RECLAMATIONS ========================

    /**
     * Affiche la liste de toutes les réclamations du département du chef.
     *
     * @param model le modèle Spring MVC
     * @return la vue chef/reclamations
     */
    @GetMapping("/reclamations")
    public String listeReclamations(Model model) {
        try {
            Utilisateur currentUser = getCurrentUser();
            Departement departement = currentUser.getDepartement();

            if (departement != null) {
                List<Reclamation> reclamations = reclamationService.findByDepartement(departement);
                model.addAttribute("reclamations", reclamations);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des réclamations : " + e.getMessage());
        }
        return "chef/reclamations";
    }

    /**
     * Affiche le détail d'une réclamation avec ses réponses, pièces jointes
     * et la liste des enseignants du département pour réaffectation.
     *
     * @param id    identifiant de la réclamation
     * @param model le modèle Spring MVC
     * @return la vue chef/reclamation-detail
     */
    @GetMapping("/reclamations/{id}")
    public String detailReclamation(@PathVariable Long id, Model model) {
        try {
            Reclamation reclamation = reclamationService.findById(id);
            Utilisateur currentUser = getCurrentUser();
            Departement departement = currentUser.getDepartement();

            model.addAttribute("reclamation", reclamation);
            model.addAttribute("reponses", reponseService.findByReclamation(reclamation));
            model.addAttribute("piecesJointes", pieceJointeRepository.findByReclamation(reclamation));

            // Charger les enseignants du département pour l'affectation (avec repli sur tous les enseignants si vide)
            List<Utilisateur> enseignants = null;
            if (departement != null) {
                enseignants = utilisateurService.findEnseignantsByDepartement(departement.getId());
            }
            if (enseignants == null || enseignants.isEmpty()) {
                enseignants = utilisateurService.findByRole(Role.ENSEIGNANT);
            }
            model.addAttribute("enseignants", enseignants);
        } catch (Exception e) {
            return "redirect:/chef/reclamations";
        }
        return "chef/reclamation-detail";
    }

    /**
     * Affecte une réclamation à un enseignant du département.
     *
     * @param id                identifiant de la réclamation
     * @param enseignantId      identifiant de l'enseignant à affecter
     * @param redirectAttributes attributs de redirection
     * @return redirection vers le détail de la réclamation
     */
    @PostMapping("/reclamations/{id}/affecter")
    public String affecterEnseignant(@PathVariable Long id,
                                      @RequestParam("enseignantId") Long enseignantId,
                                      RedirectAttributes redirectAttributes) {
        try {
            reclamationService.affecterEnseignant(id, enseignantId);
            redirectAttributes.addFlashAttribute("success", "Réclamation affectée à l'enseignant avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'affectation : " + e.getMessage());
        }
        return "redirect:/chef/reclamations/" + id;
    }

    /**
     * Valide la réponse de l'enseignant pour une réclamation.
     *
     * @param id                identifiant de la réclamation
     * @param redirectAttributes attributs de redirection
     * @return redirection vers le détail de la réclamation
     */
    @PostMapping("/reclamations/{id}/valider")
    public String validerReponse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.validerReponseChef(id);
            redirectAttributes.addFlashAttribute("success", "Réponse validée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la validation : " + e.getMessage());
        }
        return "redirect:/chef/reclamations/" + id;
    }

    /**
     * Clôture une réclamation.
     *
     * @param id                identifiant de la réclamation
     * @param redirectAttributes attributs de redirection
     * @return redirection vers le détail de la réclamation
     */
    @PostMapping("/reclamations/{id}/cloturer")
    public String cloturerReclamation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.cloturerReclamation(id);
            redirectAttributes.addFlashAttribute("success", "Réclamation clôturée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la clôture : " + e.getMessage());
        }
        return "redirect:/chef/reclamations/" + id;
    }

    /**
     * Rejette une réclamation avec un motif de rejet.
     *
     * @param id                identifiant de la réclamation
     * @param motif             motif du rejet
     * @param redirectAttributes attributs de redirection
     * @return redirection vers le détail de la réclamation
     */
    @PostMapping("/reclamations/{id}/rejeter")
    public String rejeterReclamation(@PathVariable Long id,
                                      @RequestParam("motif") String motif,
                                      RedirectAttributes redirectAttributes) {
        try {
            reclamationService.rejeterReclamation(id, motif);
            redirectAttributes.addFlashAttribute("success", "Réclamation rejetée.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du rejet : " + e.getMessage());
        }
        return "redirect:/chef/reclamations/" + id;
    }

    @PostMapping("/reclamations/{id}/retourner")
    public String retournerReclamation(@PathVariable Long id,
                                        @RequestParam("commentaire") String commentaire,
                                        RedirectAttributes redirectAttributes) {
        try {
            reclamationService.retournerReclamation(id, commentaire);
            redirectAttributes.addFlashAttribute("success", "Réclamation renvoyée pour compléments avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du renvoi pour compléments : " + e.getMessage());
        }
        return "redirect:/chef/reclamations/" + id;
    }

    @PostMapping("/reclamations/{id}/archiver")
    public String archiverReclamation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.archiverReclamation(id);
            redirectAttributes.addFlashAttribute("success", "Réclamation archivée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'archivage : " + e.getMessage());
        }
        return "redirect:/chef/reclamations/" + id;
    }

    private String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @GetMapping("/profil")
    public String profil(Model model) {
        model.addAttribute("utilisateur", getCurrentUser());
        return "chef/profil";
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
        return "redirect:/chef/profil";
    }
}
