package com.univ.reclamations.controller;

import com.univ.reclamations.model.*;
import com.univ.reclamations.repository.PieceJointeRepository;
import com.univ.reclamations.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Contrôleur gérant l'espace enseignant.
 * Permet aux enseignants de consulter les réclamations qui leur sont affectées,
 * d'y répondre, de les valider, rejeter ou transmettre au chef de département.
 */
@Controller
@RequestMapping("/enseignant")
public class EnseignantController {

    private final UtilisateurService utilisateurService;
    private final ReclamationService reclamationService;
    private final ReponseService reponseService;
    private final NotificationService notificationService;
    private final PieceJointeRepository pieceJointeRepository;

    /**
     * Constructeur avec injection de toutes les dépendances.
     *
     * @param utilisateurService service de gestion des utilisateurs
     * @param reclamationService service de gestion des réclamations
     * @param reponseService     service de gestion des réponses
     * @param notificationService service de notifications
     * @param pieceJointeRepository dépôt des pièces jointes
     */
    public EnseignantController(UtilisateurService utilisateurService,
                                ReclamationService reclamationService,
                                ReponseService reponseService,
                                NotificationService notificationService,
                                PieceJointeRepository pieceJointeRepository) {
        this.utilisateurService = utilisateurService;
        this.reclamationService = reclamationService;
        this.reponseService = reponseService;
        this.notificationService = notificationService;
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
     * Affiche le tableau de bord de l'enseignant avec les statistiques
     * de ses réclamations (affectées, en attente, traitées).
     *
     * @param model le modèle Spring MVC
     * @return la vue enseignant/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        Utilisateur currentUser = getCurrentUser();

        List<Reclamation> reclamations = reclamationService.findByEnseignant(currentUser);

        long total = reclamations.size();

        long enAttente = reclamations.stream()
                .filter(r -> r.getStatut() == StatutReclamation.AFFECTEE
                        || r.getStatut() == StatutReclamation.EN_TRAITEMENT)
                .count();

        long traitees = reclamations.stream()
                .filter(r -> r.getStatut() == StatutReclamation.REPONSE_ENSEIGNANT
                        || r.getStatut() == StatutReclamation.VALIDEE_CHEF
                        || r.getStatut() == StatutReclamation.CLOTUREE)
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("en_attente", enAttente);
        stats.put("traitees", traitees);

        model.addAttribute("stats", stats);

        model.addAttribute("reclamationsRecentes",
                reclamations.stream().limit(5).toList());

        return "enseignant/dashboard";
    }

    // ======================== RECLAMATIONS ========================

    /**
     * Affiche la liste des réclamations affectées à l'enseignant connecté.
     *
     * @param model le modèle Spring MVC
     * @return la vue enseignant/reclamations
     */
    @GetMapping("/reclamations")
    public String listeReclamations(Model model) {
        try {
            Utilisateur currentUser = getCurrentUser();
            List<Reclamation> reclamations = reclamationService.findByEnseignant(currentUser);
            model.addAttribute("reclamations", reclamations);
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des réclamations : " + e.getMessage());
        }
        return "enseignant/reclamations";
    }

    /**
     * Affiche le détail d'une réclamation avec les informations de l'étudiant,
     * les réponses et les pièces jointes.
     *
     * @param id    identifiant de la réclamation
     * @param model le modèle Spring MVC
     * @return la vue enseignant/reclamation-detail
     */
    @GetMapping("/reclamations/{id}")
    public String detailReclamation(@PathVariable Long id, Model model) {
        try {
            Reclamation reclamation = reclamationService.findById(id);

            // Vérification que la réclamation est bien affectée à cet enseignant
            Utilisateur currentUser = getCurrentUser();
            if (reclamation.getEnseignantAssigne() == null
                    || !reclamation.getEnseignantAssigne().getId().equals(currentUser.getId())) {
                return "redirect:/enseignant/reclamations";
            }

            model.addAttribute("reclamation", reclamation);
            model.addAttribute("etudiant", reclamation.getEtudiant());
            model.addAttribute("reponses", reponseService.findByReclamation(reclamation));
            model.addAttribute("piecesJointes", pieceJointeRepository.findByReclamation(reclamation));
        } catch (Exception e) {
            return "redirect:/enseignant/reclamations";
        }
        return "enseignant/reclamation-detail";
    }

    /**
     * Soumet une réponse à une réclamation et change son statut en REPONSE_ENSEIGNANT.
     *
     * @param id                identifiant de la réclamation
     * @param contenu           contenu de la réponse
     * @param redirectAttributes attributs de redirection
     * @return redirection vers le détail de la réclamation
     */
    @PostMapping("/reclamations/{id}/repondre")
    public String repondre(@PathVariable Long id,
                           @RequestParam("contenu") String contenu,
                           RedirectAttributes redirectAttributes) {
        try {
            Utilisateur currentUser = getCurrentUser();
            reclamationService.soumettreReponse(id, contenu, currentUser);
            redirectAttributes.addFlashAttribute("success", "Réclamation traitée et transmise au chef avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la soumission : " + e.getMessage());
        }
        return "redirect:/enseignant/reclamations/" + id;
    }

    /**
     * Rejette une réclamation avec un motif de rejet.
     *
     * @param id                identifiant de la réclamation
     * @param motif             motif du rejet
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des réclamations
     */
    @PostMapping("/reclamations/{id}/rejeter")
    public String rejeter(@PathVariable Long id,
                          @RequestParam("motif") String motif,
                          RedirectAttributes redirectAttributes) {
        try {
            reclamationService.rejeterReclamation(id, motif);
            redirectAttributes.addFlashAttribute("success", "Réclamation rejetée.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du rejet : " + e.getMessage());
        }
        return "redirect:/enseignant/reclamations/" + id;
    }

    /**
     * Transmet une réclamation au chef de département en changeant son statut.
     *
     * @param id                identifiant de la réclamation
     * @param redirectAttributes attributs de redirection
     * @return redirection vers la liste des réclamations
     */
    @PostMapping("/reclamations/{id}/transmettre")
    public String transmettre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.changerStatut(id, StatutReclamation.REPONSE_ENSEIGNANT);
            redirectAttributes.addFlashAttribute("success", "Réclamation transmise au chef de département.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la transmission : " + e.getMessage());
        }
        return "redirect:/enseignant/reclamations/" + id;
    }

    @GetMapping("/profil")
    public String profil(Model model) {
        model.addAttribute("utilisateur", getCurrentUser());
        return "enseignant/profil";
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
        return "redirect:/enseignant/profil";
    }
}
