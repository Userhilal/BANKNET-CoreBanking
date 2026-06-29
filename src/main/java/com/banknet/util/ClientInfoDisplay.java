package com.banknet.util;

import com.banknet.dao.ClientDAO;
import com.banknet.dao.CompteDAO;
import com.banknet.model.Client;
import com.banknet.model.Compte;

import java.util.List;
import java.util.Optional;

/**
 * Utilitaire pour afficher les informations des clients spécifiques
 */
public class ClientInfoDisplay {
    
    public static void displayClientInfo(String[] clientNames) {
        if (!HibernateUtil.isInitialized()) {
            System.err.println("❌ La base de données n'est pas disponible.");
            return;
        }
        
        try {
            ClientDAO clientDAO = new ClientDAO();
            CompteDAO compteDAO = new CompteDAO();
            
            System.out.println("\n" + "═".repeat(80));
            System.out.println("📋 INFORMATIONS DES CLIENTS");
            System.out.println("═".repeat(80) + "\n");
            
            boolean foundAny = false;
            
            for (String fullName : clientNames) {
                String trimmed = fullName.trim();
                // Trouver le premier espace pour séparer prénom et nom (le nom peut contenir plusieurs mots)
                int firstSpace = trimmed.indexOf(' ');
                if (firstSpace == -1) {
                    System.out.println("⚠️  Format invalide pour: " + fullName + " (attendu: Prénom Nom)");
                    continue;
                }
                
                String prenom = trimmed.substring(0, firstSpace);
                String nom = trimmed.substring(firstSpace + 1); // Tout le reste est le nom
                
                // Rechercher le client par nom et prénom
                Optional<Client> clientOpt = clientDAO.findAll().stream()
                    .filter(c -> c.getPrenom().equalsIgnoreCase(prenom) && 
                                c.getNom().equalsIgnoreCase(nom))
                    .findFirst();
                
                if (clientOpt.isPresent()) {
                    foundAny = true;
                    Client client = clientOpt.get();
                    List<Compte> comptes = compteDAO.findByClientId(client.getId());
                    
                    System.out.println("─".repeat(80));
                    System.out.println("👤 CLIENT: " + client.getNomComplet().toUpperCase());
                    System.out.println("─".repeat(80));
                    System.out.println("ID Client        : " + client.getId());
                    System.out.println("Prénom           : " + client.getPrenom());
                    System.out.println("Nom              : " + client.getNom());
                    System.out.println("Nom complet      : " + client.getNomComplet());
                    System.out.println("CIN              : " + client.getCin());
                    System.out.println("Email            : " + client.getEmail());
                    
                    if (client.getUserAccount() != null) {
                        System.out.println("Compte utilisateur: ✓ ACTIVÉ");
                        System.out.println("  → Login          : " + client.getUserAccount().getLogin());
                        System.out.println("  → Rôle           : " + client.getUserAccount().getRole());
                        System.out.println("  → Statut         : " + client.getUserAccount().getStatus());
                    } else {
                        System.out.println("Compte utilisateur: ✗ NON ACTIVÉ");
                        System.out.println("  → Peut être activé via l'interface d'activation");
                        System.out.println("  → Nécessite: Client ID = " + client.getId() + " et CIN = " + client.getCin());
                    }
                    
                    if (!comptes.isEmpty()) {
                        System.out.println("\n💳 COMPTES BANCAIRES:");
                        for (Compte compte : comptes) {
                            System.out.println("  → " + compte.getType() + " - RIB: " + compte.getNumeroCompte() + 
                                " | Solde: " + String.format("%.2f", compte.getSolde()) + " MAD");
                        }
                        
                        // Calculer le solde total
                        double soldeTotal = comptes.stream()
                            .mapToDouble(c -> c.getSolde().doubleValue())
                            .sum();
                        System.out.println("  📊 Solde total: " + String.format("%.2f", soldeTotal) + " MAD");
                    } else {
                        System.out.println("\n💳 COMPTES: Aucun compte bancaire trouvé");
                    }
                    
                    System.out.println();
                } else {
                    System.out.println("❌ Client non trouvé: " + prenom + " " + nom);
                    System.out.println();
                }
            }
            
            if (!foundAny) {
                System.out.println("⚠️  Aucun client trouvé avec les noms fournis.");
            }
            
            System.out.println("═".repeat(80) + "\n");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des informations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        // Hibernate est déjà initialisé par MainApp, on vérifie juste
        if (!HibernateUtil.isInitialized()) {
            System.err.println("❌ Hibernate n'est pas initialisé. Veuillez lancer l'application d'abord.");
            return;
        }
        
        // Afficher les informations des clients demandés
        String[] clients = {
            "Mourad Bensaid",
            "Moncef El Amrani",
            "Rajaa Fadili"
        };
        
        displayClientInfo(clients);
    }
}

