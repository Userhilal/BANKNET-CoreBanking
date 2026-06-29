package com.banknet.util;

import com.banknet.dao.ClientDAO;
import com.banknet.dao.CompteDAO;
import com.banknet.model.Client;
import com.banknet.model.Compte;

import java.util.List;

/**
 * Utilitaire pour afficher les informations des clients disponibles
 */
public class ClientInfoUtil {
    
    public static void printAvailableClients() {
        if (!HibernateUtil.isInitialized()) {
            System.err.println("La base de données n'est pas disponible.");
            return;
        }
        
        try {
            ClientDAO clientDAO = new ClientDAO();
            List<Client> clients = clientDAO.findAll();
            
            if (clients.isEmpty()) {
                System.out.println("\n=== Aucun client trouvé dans la base de données ===");
                System.out.println("Vous devez d'abord créer un client avant de pouvoir activer un compte.");
                return;
            }
            
            System.out.println("\n=== CLIENTS DISPONIBLES POUR L'ACTIVATION ===\n");
            
            for (Client client : clients) {
                boolean hasAccount = client.getUserAccount() != null;
                String status = hasAccount ? "✓ COMPTE ACTIVÉ" : "○ COMPTE NON ACTIVÉ";
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("ID Client      : " + client.getId());
                System.out.println("Nom complet    : " + client.getNomComplet());
                System.out.println("CIN            : " + client.getCin());
                System.out.println("Email          : " + client.getEmail());
                System.out.println("Statut         : " + status);
                
                if (hasAccount) {
                    System.out.println("Login          : " + client.getUserAccount().getLogin());
                    System.out.println("Rôle           : " + client.getUserAccount().getRole());
                }
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            }
            
            // Afficher un client sans compte
            long clientsWithoutAccount = clients.stream()
                .filter(c -> c.getUserAccount() == null)
                .count();
            
            if (clientsWithoutAccount > 0) {
                System.out.println("ℹ️  " + clientsWithoutAccount + " client(s) disponible(s) pour l'activation de compte.");
                System.out.println("   Utilisez leur ID Client et CIN pour activer leur compte digital.\n");
            } else {
                System.out.println("⚠️  Tous les clients ont déjà un compte activé.");
                System.out.println("   Créez un nouveau client pour pouvoir activer un compte.\n");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des clients : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void createSampleClient() {
        if (!HibernateUtil.isInitialized()) {
            System.err.println("La base de données n'est pas disponible.");
            return;
        }
        
        try {
            ClientDAO clientDAO = new ClientDAO();
            CompteDAO compteDAO = new CompteDAO();
            
            // Créer un nouveau client de démonstration
            Client newClient = new Client();
            newClient.setNom("Martin");
            newClient.setPrenom("Sophie");
            newClient.setCin("XY987654");
            newClient.setEmail("sophie.martin@example.com");
            
            newClient = clientDAO.save(newClient);
            
            // Créer un compte bancaire pour ce client
            Compte compte = new Compte();
            compte.setNumeroCompte("ACC" + String.format("%010d", System.currentTimeMillis() % 10000000000L));
            compte.setSolde(new java.math.BigDecimal("500.00"));
            compte.setType(com.banknet.model.TypeCompte.COURANT);
            compte.setClient(newClient);
            compteDAO.save(compte);
            
            System.out.println("\n=== NOUVEAU CLIENT CRÉÉ ===");
            System.out.println("ID Client      : " + newClient.getId());
            System.out.println("Nom complet    : " + newClient.getNomComplet());
            System.out.println("CIN            : " + newClient.getCin());
            System.out.println("Email          : " + newClient.getEmail());
            System.out.println("Compte bancaire: " + compte.getNumeroCompte() + " (Solde: " + compte.getSolde() + " MAD)");
            System.out.println("\nVous pouvez maintenant activer le compte digital avec :");
            System.out.println("  - ID Client: " + newClient.getId());
            System.out.println("  - CIN: " + newClient.getCin());
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du client : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        // Initialiser Hibernate
        try {
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.waitForDatabase(5, 2000);
            
            if (args.length > 0 && args[0].equals("--create")) {
                createSampleClient();
            } else {
                printAvailableClients();
            }
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
