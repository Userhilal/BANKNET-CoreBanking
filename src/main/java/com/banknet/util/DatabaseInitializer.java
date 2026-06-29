package com.banknet.util;

import com.banknet.dao.ClientDAO;
import com.banknet.dao.CompteDAO;
import com.banknet.dao.UserAccountDAO;
import com.banknet.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class DatabaseInitializer {
    
    private final ClientDAO clientDAO;
    private final CompteDAO compteDAO;
    private final UserAccountDAO userAccountDAO;
    
    public DatabaseInitializer() {
        this.clientDAO = new ClientDAO();
        this.compteDAO = new CompteDAO();
        this.userAccountDAO = new UserAccountDAO();
    }
    
    /**
     * Initialise la base de données avec des données de test si elle est vide
     */
    public void initializeIfEmpty() {
        if (!HibernateUtil.isInitialized()) {
            System.err.println("DatabaseInitializer: La base de données n'est pas disponible. Impossible d'initialiser.");
            return;
        }
        
        try {
            // Toujours créer le compte Admin s'il n'existe pas
            createAdminAccount();
            
            // Vérifier si la base est vide
            if (isDatabaseEmpty()) {
                System.out.println("DatabaseInitializer: Base de données vide détectée. Initialisation en cours...");
                
                // Créer les clients de test
                createTestClientAndAccount();
                
                System.out.println("DatabaseInitializer: Initialisation terminée avec succès.");
            } else {
                System.out.println("DatabaseInitializer: La base de données contient déjà des données.");
                // Même si la base n'est pas vide, créer les clients s'ils n'existent pas
                System.out.println("DatabaseInitializer: Vérification et création des clients manquants...");
                createTestClientAndAccount();
            }
            
            // Afficher les RIB disponibles pour les virements tiers
            displayAvailableRIBs();
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifie si la base de données est vide
     */
    private boolean isDatabaseEmpty() {
        try {
            long userCount = userAccountDAO.findAll().size();
            long clientCount = clientDAO.findAll().size();
            return userCount == 0 && clientCount == 0;
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Erreur lors de la vérification de la base : " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crée le compte administrateur par défaut
     */
    private void createAdminAccount() {
        try {
            if (!userAccountDAO.existsByLogin("admin")) {
                UserAccount admin = new UserAccount();
                admin.setLogin("admin");
                admin.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw("admin123", org.mindrot.jbcrypt.BCrypt.gensalt()));
                admin.setRole(Role.ADMIN);
                admin.setStatus(AccountStatus.ACTIF);
                admin.setFailedLoginAttempts(0);
                admin.setClient(null);
                
                userAccountDAO.save(admin);
                System.out.println("DatabaseInitializer: Compte admin créé (login: admin, password: admin123)");
            } else {
                System.out.println("DatabaseInitializer: Le compte admin existe déjà.");
            }
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Erreur lors de la création du compte admin : " + e.getMessage());
        }
    }
    
    /**
     * Crée des clients de test avec des comptes bancaires provisionnés
     */
    private void createTestClientAndAccount() {
        try {
            // Liste des clients à créer (sans compte utilisateur pour pouvoir tester l'activation)
            String[][] clientsData = {
                {"Yasser", "Alami", "YC123456", "yasser.alami@example.com", "1000.00"},
                {"Mourad", "Bensaid", "MB789012", "mourad.bensaid@example.com", "1500.00"},
                {"Moncef", "El Amrani", "ME345678", "moncef.elamrani@example.com", "2000.00"},
                {"Rajaa", "Fadili", "RF901234", "rajaa.fadili@example.com", "1200.00"}
            };
            
            System.out.println("DatabaseInitializer: Création de " + clientsData.length + " clients...");
            
            for (String[] clientData : clientsData) {
                String prenom = clientData[0];
                String nom = clientData[1];
                String cin = clientData[2];
                String email = clientData[3];
                String solde = clientData[4];
                
                // Vérifier si le client existe déjà (par CIN)
                Optional<Client> existingClient = clientDAO.findByCin(cin);
                if (existingClient.isPresent()) {
                    System.out.println("DatabaseInitializer: Client " + prenom + " " + nom + " (CIN: " + cin + ") existe déjà, ignoré.");
                    continue;
                }
                
                // Créer le client
                Client newClient = new Client();
                newClient.setNom(nom);
                newClient.setPrenom(prenom);
                newClient.setCin(cin);
                newClient.setEmail(email);
                
                newClient = clientDAO.save(newClient);
                System.out.println("DatabaseInitializer: ✓ Client créé - ID: " + newClient.getId() + 
                    ", Nom: " + prenom + " " + nom + ", CIN: " + cin);
                
                // Créer un compte courant pour ce client
                Compte compteCourant = new Compte();
                compteCourant.setNumeroCompte(generateAccountNumber());
                compteCourant.setSolde(new BigDecimal(solde));
                compteCourant.setType(TypeCompte.COURANT);
                compteCourant.setClient(newClient);
                compteDAO.save(compteCourant);
                System.out.println("DatabaseInitializer:   → Compte COURANT créé: " + compteCourant.getNumeroCompte() + " (Solde: " + solde + " MAD)");
                
                // Créer un compte épargne pour ce client (solde initial à 0)
                Compte compteEpargne = new Compte();
                compteEpargne.setNumeroCompte(generateAccountNumber());
                compteEpargne.setSolde(BigDecimal.ZERO);
                compteEpargne.setType(TypeCompte.EPARGNE);
                compteEpargne.setClient(newClient);
                compteDAO.save(compteEpargne);
                System.out.println("DatabaseInitializer:   → Compte ÉPARGNE créé: " + compteEpargne.getNumeroCompte() + " (Solde: 0.00 MAD)");
                
                System.out.println("DatabaseInitializer:   → ⚠️  Compte utilisateur NON créé - Peut être activé via l'interface");
                System.out.println();
            }
            
            // Vérifier et créer les comptes épargne manquants pour les clients existants
            ensureAllClientsHaveBothAccounts();
            
            // Ne plus créer le client de test "Test Dupont" - supprimé pour des raisons de confidentialité
            // createTestClientForVirementTiers();
            
            System.out.println("DatabaseInitializer: ✓ Tous les clients ont été créés avec succès !");
            System.out.println("DatabaseInitializer: 💡 Vous pouvez maintenant activer leurs comptes via l'interface d'activation.");
            
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Erreur lors de la création des clients : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * S'assure que tous les clients ont à la fois un compte courant et un compte épargne
     */
    private void ensureAllClientsHaveBothAccounts() {
        try {
            List<Client> allClients = clientDAO.findAll();
            int comptesCrees = 0;
            
            for (Client client : allClients) {
                List<Compte> comptes = compteDAO.findByClientId(client.getId());
                
                boolean hasCourant = comptes.stream().anyMatch(c -> c.getType() == TypeCompte.COURANT);
                boolean hasEpargne = comptes.stream().anyMatch(c -> c.getType() == TypeCompte.EPARGNE);
                
                // Créer le compte épargne s'il manque
                if (hasCourant && !hasEpargne) {
                    Compte compteEpargne = new Compte();
                    compteEpargne.setNumeroCompte(generateAccountNumber());
                    compteEpargne.setSolde(BigDecimal.ZERO);
                    compteEpargne.setType(TypeCompte.EPARGNE);
                    compteEpargne.setClient(client);
                    compteDAO.save(compteEpargne);
                    comptesCrees++;
                    System.out.println("DatabaseInitializer: ✓ Compte ÉPARGNE ajouté pour le client " + 
                        client.getPrenom() + " " + client.getNom() + " (ID: " + client.getId() + ")");
                }
                
                // Créer le compte courant s'il manque (cas rare mais possible)
                if (!hasCourant && hasEpargne) {
                    Compte compteCourant = new Compte();
                    compteCourant.setNumeroCompte(generateAccountNumber());
                    compteCourant.setSolde(BigDecimal.ZERO);
                    compteCourant.setType(TypeCompte.COURANT);
                    compteCourant.setClient(client);
                    compteDAO.save(compteCourant);
                    comptesCrees++;
                    System.out.println("DatabaseInitializer: ✓ Compte COURANT ajouté pour le client " + 
                        client.getPrenom() + " " + client.getNom() + " (ID: " + client.getId() + ")");
                }
            }
            
            if (comptesCrees > 0) {
                System.out.println("DatabaseInitializer: ✓ " + comptesCrees + " compte(s) manquant(s) créé(s) pour les clients existants.");
            }
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Erreur lors de la vérification des comptes manquants : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crée un client de test spécifiquement pour tester les virements vers tiers
     */
    private void createTestClientForVirementTiers() {
        try {
            // Vérifier si le client de test existe déjà
            Optional<Client> existingClient = clientDAO.findByCin("VT999999");
            if (existingClient.isPresent()) {
                System.out.println("DatabaseInitializer: Client de test pour virement tiers existe déjà.");
                // Afficher le RIB du compte courant
                List<Compte> comptes = compteDAO.findByClientId(existingClient.get().getId());
                Compte compteCourant = comptes.stream()
                    .filter(c -> c.getType() == TypeCompte.COURANT)
                    .findFirst()
                    .orElse(null);
                if (compteCourant != null) {
                    System.out.println("DatabaseInitializer: 📋 RIB pour virement tiers: " + compteCourant.getNumeroCompte());
                }
                return;
            }
            
            // Créer le client de test
            Client testClient = new Client();
            testClient.setNom("Dupont");
            testClient.setPrenom("Test");
            testClient.setCin("VT999999");
            testClient.setEmail("test.virement@example.com");
            
            testClient = clientDAO.save(testClient);
            System.out.println("DatabaseInitializer: ✓ Client de test créé - ID: " + testClient.getId() + 
                ", Nom: Test Dupont, CIN: VT999999");
            
            // Créer un compte courant avec un solde de test
            Compte compteCourant = new Compte();
            compteCourant.setNumeroCompte("RIB" + String.format("%010d", System.currentTimeMillis() % 10000000000L));
            compteCourant.setSolde(new BigDecimal("5000.00"));
            compteCourant.setType(TypeCompte.COURANT);
            compteCourant.setClient(testClient);
            compteDAO.save(compteCourant);
            System.out.println("DatabaseInitializer:   → Compte COURANT créé: " + compteCourant.getNumeroCompte() + 
                " (Solde: 5000.00 MAD)");
            
            // Créer un compte épargne
            Compte compteEpargne = new Compte();
            compteEpargne.setNumeroCompte("RIB" + String.format("%010d", (System.currentTimeMillis() + 1000) % 10000000000L));
            compteEpargne.setSolde(new BigDecimal("2000.00"));
            compteEpargne.setType(TypeCompte.EPARGNE);
            compteEpargne.setClient(testClient);
            compteDAO.save(compteEpargne);
            System.out.println("DatabaseInitializer:   → Compte ÉPARGNE créé: " + compteEpargne.getNumeroCompte() + 
                " (Solde: 2000.00 MAD)");
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("📋 INFOS POUR TESTER LE VIREMENT VERS TIERS:");
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("RIB Compte Courant Destination: " + compteCourant.getNumeroCompte());
            System.out.println("RIB Compte Épargne Destination: " + compteEpargne.getNumeroCompte());
            System.out.println("Client: Test Dupont (CIN: VT999999)");
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Erreur lors de la création du client de test pour virement tiers : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche les RIB disponibles pour tester les virements vers tiers
     */
    private void displayAvailableRIBs() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("📋 RIB DISPONIBLES POUR TESTER LES VIREMENTS VERS TIERS:");
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            List<Client> allClients = clientDAO.findAll();
            boolean foundAny = false;
            
            for (Client client : allClients) {
                List<Compte> comptes = compteDAO.findByClientId(client.getId());
                if (!comptes.isEmpty()) {
                    foundAny = true;
                    System.out.println("\n👤 Client: " + client.getPrenom() + " " + client.getNom() + 
                        " (ID: " + client.getId() + ", CIN: " + client.getCin() + ")");
                    for (Compte compte : comptes) {
                        System.out.println("   💳 " + compte.getType() + ": " + compte.getNumeroCompte() + 
                            " - Solde: " + String.format("%.2f", compte.getSolde()) + " MAD");
                    }
                }
            }
            
            if (!foundAny) {
                System.out.println("Aucun compte trouvé dans la base de données.");
            }
            
            System.out.println();
            System.out.println("💡 Pour effectuer un virement vers tiers, utilisez l'un des RIB ci-dessus.");
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Erreur lors de l'affichage des RIB : " + e.getMessage());
        }
    }
    
    /**
     * Génère un numéro de compte unique
     */
    private String generateAccountNumber() {
        long timestamp = System.currentTimeMillis();
        return "ACC" + String.format("%010d", timestamp % 10000000000L);
    }
    
    /**
     * Attend que la base de données soit prête (utile après le démarrage de Docker)
     */
    public void waitForDatabase(int maxAttempts, long delayMs) {
        for (int i = 0; i < maxAttempts; i++) {
            if (HibernateUtil.isInitialized()) {
                try {
                    // Test de connexion simple
                    userAccountDAO.findAll().size();
                    System.out.println("DatabaseInitializer: Base de données prête.");
                    return;
                } catch (Exception e) {
                    // Base non prête, attendre
                }
            }
            
            if (i < maxAttempts - 1) {
                System.out.println("DatabaseInitializer: Attente de la base de données... (" + (i + 1) + "/" + maxAttempts + ")");
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        System.err.println("DatabaseInitializer: Timeout - La base de données n'est pas accessible après " + maxAttempts + " tentatives.");
    }
}
