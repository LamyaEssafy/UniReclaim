package com.univ.reclamations.controller;

import com.univ.reclamations.model.Role;
import com.univ.reclamations.model.Utilisateur;
import com.univ.reclamations.repository.DepartementRepository;
import com.univ.reclamations.repository.FiliereRepository;
import com.univ.reclamations.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Contrôleur gérant l'authentification et l'inscription des utilisateurs.
 * Fournit les pages de connexion et d'inscription pour les étudiants.
 */
@Controller
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final DepartementRepository departementRepository;
    private final FiliereRepository filiereRepository;

    /**
     * Constructeur avec injection des dépendances.
     *
     * @param utilisateurService service de gestion des utilisateurs
     * @param departementRepository dépôt des départements
     * @param filiereRepository dépôt des filières
     */
    public AuthController(UtilisateurService utilisateurService,
                          DepartementRepository departementRepository,
                          FiliereRepository filiereRepository) {
        this.utilisateurService = utilisateurService;
        this.departementRepository = departementRepository;
        this.filiereRepository = filiereRepository;
    }

    /**
     * Affiche la page de connexion.
     *
     * @param error   paramètre optionnel indiquant une erreur de connexion
     * @param logout  paramètre optionnel indiquant une déconnexion réussie
     * @param registered paramètre optionnel indiquant une inscription réussie
     * @param model   le modèle Spring MVC
     * @return la vue auth/login
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect.");
        }
        if (logout != null) {
            model.addAttribute("logout", "Vous avez été déconnecté avec succès.");
        }
        if (registered != null) {
            model.addAttribute("registered", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
        }
        return "auth/login";
    }

    /**
     * Affiche le formulaire d'inscription.
     *
     * @param model le modèle Spring MVC
     * @return la vue auth/register
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("departements", departementRepository.findAll());
        model.addAttribute("filieres", filiereRepository.findAll());
        return "auth/register";
    }

    /**
     * Traite la soumission du formulaire d'inscription.
     * L'utilisateur est automatiquement inscrit avec le rôle ETUDIANT.
     *
     * @param utilisateur      les données de l'utilisateur à inscrire
     * @param result           résultat de la validation
     * @param model            le modèle Spring MVC
     * @param redirectAttributes attributs de redirection pour les messages flash
     * @return redirection vers la page de connexion en cas de succès
     */
    @PostMapping("/register")
    public String registerPost(@Valid @ModelAttribute("utilisateur") Utilisateur utilisateur,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("departements", departementRepository.findAll());
            model.addAttribute("filieres", filiereRepository.findAll());
            return "auth/register";
        }

        try {
            // Vérifier si l'email existe déjà
            if (utilisateurService.existsByEmail(utilisateur.getEmail())) {
                model.addAttribute("emailError", "Cet email est déjà utilisé.");
                model.addAttribute("departements", departementRepository.findAll());
                model.addAttribute("filieres", filiereRepository.findAll());
                return "auth/register";
            }

            utilisateur.setRole(Role.ETUDIANT);
            utilisateur.setActif(true);
            utilisateurService.save(utilisateur);
            redirectAttributes.addFlashAttribute("success", "Inscription réussie !");
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Une erreur est survenue lors de l'inscription : " + e.getMessage());
            model.addAttribute("departements", departementRepository.findAll());
            model.addAttribute("filieres", filiereRepository.findAll());
            return "auth/register";
        }
    }
}
