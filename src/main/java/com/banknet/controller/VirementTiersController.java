package com.banknet.controller;

import com.banknet.dao.ClientDAO;
import com.banknet.dao.CompteDAO;
import com.banknet.exception.SoldeInsuffisantException;
import com.banknet.model.Client;
import com.banknet.model.Compte;
import com.banknet.service.AuthService;
import com.banknet.service.TransactionService;
import com.banknet.util.AccountTypeTranslator;
import com.banknet.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class VirementTiersController {
    
    @FXML
    private ComboBox<Compte> compteSourceCombo;
    
    @FXML
    private ComboBox<Client> clientDestCombo;
    
    @FXML
    private TextField montantField;
    
    @FXML
    private TextField libelleField;
    
    @FXML
    private Label soldeSourceLabel;
    
    @FXML
    private Label compteDestInfoLabel;
    
    @FXML
    private Label destInfoLabel;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Label successLabel;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label subtitleLabel;
    
    @FXML
    private Label sourceAccountLabel;
    
    @FXML
    private Label destAccountLabel;
    
    @FXML
    private Label amountLabel;
    
    @FXML
    private Label labelLabel;
    
    @FXML
    private Button transferButton;
    
    @FXML
    private Button cancelButton;
    
    private AuthService authService;
    private TransactionService transactionService;
    private LanguageManager languageManager;
    private List<Compte> clientComptes;
    private ClientDAO clientDAO;
    private CompteDAO compteDAO;
    private Compte selectedDestCompte;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.transactionService = new TransactionService();
        this.clientDAO = new ClientDAO();
        this.compteDAO = new CompteDAO();
        // Utiliser Platform.runLater pour s'assurer que les champs FXML sont initialisés
        javafx.application.Platform.runLater(() -> {
            try {
                updateTranslations(); // Mettre à jour les traductions
                initializeComptes();
            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation des comptes: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        try {
            if (titleLabel != null) {
                titleLabel.setText(languageManager.getTranslation("transfer.external.title"));
            }
            if (subtitleLabel != null) {
                subtitleLabel.setText(languageManager.getTranslation("transfer.external.subtitle"));
            }
            if (sourceAccountLabel != null) {
                sourceAccountLabel.setText(languageManager.getTranslation("transfer.source.your"));
            }
            if (destAccountLabel != null) {
                destAccountLabel.setText(languageManager.getTranslation("transfer.dest.rib"));
            }
            if (amountLabel != null) {
                amountLabel.setText(languageManager.getTranslation("transfer.amount"));
            }
            if (labelLabel != null) {
                labelLabel.setText(languageManager.getTranslation("transfer.label"));
            }
            if (transferButton != null) {
                transferButton.setText(languageManager.getTranslation("transfer.execute"));
            }
            if (cancelButton != null) {
                cancelButton.setText(languageManager.getTranslation("transfer.cancel"));
            }
            if (compteSourceCombo != null) {
                compteSourceCombo.setPromptText(languageManager.getTranslation("transfer.source.select.your"));
            }
            if (clientDestCombo != null) {
                clientDestCombo.setPromptText(languageManager.getTranslation("transfer.dest.client.select"));
            }
            if (montantField != null) {
                montantField.setPromptText("0.00");
            }
            if (libelleField != null) {
                libelleField.setPromptText(languageManager.getTranslation("transfer.label.example.external"));
            }
            
            // Titre de la fenêtre
            Stage stage = (Stage) (titleLabel != null && titleLabel.getScene() != null ? 
                                  titleLabel.getScene().getWindow() : null);
            if (stage != null) {
                stage.setTitle(languageManager.getTranslation("transfer.external.title"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des traductions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void initialize() {
        try {
            System.out.println("VirementTiersController.initialize() - Début");
            
            languageManager = LanguageManager.getInstance();
            
            if (errorLabel != null) {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
            if (successLabel != null) {
                successLabel.setVisible(false);
                successLabel.setManaged(false);
            }
            if (destInfoLabel != null) {
                destInfoLabel.setVisible(false);
            }
            
            // Charger les traductions
            updateTranslations();
            
            // Configurer l'affichage du ComboBox
            if (compteSourceCombo != null) {
                compteSourceCombo.setCellFactory(param -> new ListCell<Compte>() {
                    @Override
                    protected void updateItem(Compte compte, boolean empty) {
                        super.updateItem(compte, empty);
                        if (empty || compte == null) {
                            setText(null);
                        } else {
                            String typeTranslated = AccountTypeTranslator.translate(compte.getType(), languageManager);
                            setText(typeTranslated + " - " + compte.getNumeroCompte() + " (" + String.format("%.2f MAD", compte.getSolde()) + ")");
                        }
                    }
                });
                compteSourceCombo.setButtonCell(new ListCell<Compte>() {
                    @Override
                    protected void updateItem(Compte compte, boolean empty) {
                        super.updateItem(compte, empty);
                        if (empty || compte == null) {
                            setText(null);
                        } else {
                            String typeTranslated = AccountTypeTranslator.translate(compte.getType(), languageManager);
                            setText(typeTranslated + " - " + compte.getNumeroCompte() + " (" + String.format("%.2f MAD", compte.getSolde()) + ")");
                        }
                    }
                });
                compteSourceCombo.setOnAction(e -> updateSoldeDisplay());
            } else {
                System.err.println("ATTENTION: compteSourceCombo est null dans initialize()");
            }
            
            // Configurer le ComboBox des clients destinataires
            if (clientDestCombo != null) {
                clientDestCombo.setCellFactory(param -> new ListCell<Client>() {
                    @Override
                    protected void updateItem(Client client, boolean empty) {
                        super.updateItem(client, empty);
                        if (empty || client == null) {
                            setText(null);
                        } else {
                            setText(client.getPrenom() + " " + client.getNom());
                        }
                    }
                });
                clientDestCombo.setButtonCell(new ListCell<Client>() {
                    @Override
                    protected void updateItem(Client client, boolean empty) {
                        super.updateItem(client, empty);
                        if (empty || client == null) {
                            setText(null);
                        } else {
                            setText(client.getPrenom() + " " + client.getNom());
                        }
                    }
                });
                clientDestCombo.setOnAction(e -> handleClientDestSelection());
            }
            
            // Initialiser la liste des clients destinataires après un délai pour s'assurer que setAuthService a été appelé
            javafx.application.Platform.runLater(() -> {
                if (authService != null) {
                    initializeClientDestList();
                }
            });
            
            // Initialiser les comptes après un court délai pour s'assurer que setAuthService a été appelé
            javafx.application.Platform.runLater(() -> {
                if (authService != null) {
                    System.out.println("VirementTiersController: Initialisation des comptes...");
                    initializeComptes();
                } else {
                    System.err.println("ATTENTION: authService est null dans initialize()");
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de VirementTiersController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeComptes() {
        try {
            System.out.println("initializeComptes() - Début");
            
            if (authService == null || authService.getCurrentUser() == null) {
                System.err.println("VirementTiersController: authService ou currentUser est null");
                return;
            }
            
            if (compteSourceCombo == null) {
                System.err.println("VirementTiersController: Le ComboBox n'est pas initialisé");
                return;
            }
            
            if (authService.getCurrentUser().getClient() != null) {
                Long clientId = authService.getCurrentUser().getClient().getId();
                System.out.println("Chargement des comptes pour le client ID: " + clientId);
                
                clientComptes = new CompteDAO().findByClientId(clientId);
                
                System.out.println("Nombre de comptes trouvés: " + (clientComptes != null ? clientComptes.size() : 0));
                
                if (clientComptes != null && !clientComptes.isEmpty()) {
                    // Vider le ComboBox d'abord
                    compteSourceCombo.getItems().clear();
                    
                    // Ajouter les comptes
                    compteSourceCombo.getItems().addAll(clientComptes);
                    
                    System.out.println("Comptes ajoutés au ComboBox: " + compteSourceCombo.getItems().size());
                    
                    // Afficher les informations de chaque compte pour debug
                    for (Compte c : clientComptes) {
                        System.out.println("  - Compte: " + c.getNumeroCompte() + " (" + c.getType() + ") - Solde: " + c.getSolde());
                    }
                    
                    updateSoldeDisplay();
                } else {
                    System.err.println("VirementTiersController: Aucun compte trouvé pour le client ID: " + clientId);
                    if (errorLabel != null) {
                        showError(languageManager.getTranslation("transfer.error.no.accounts"));
                    }
                }
            } else {
                System.err.println("VirementTiersController: Le client est null pour l'utilisateur " + authService.getCurrentUser().getLogin());
            }
        } catch (Exception e) {
            System.err.println("Erreur dans initializeComptes: " + e.getMessage());
            e.printStackTrace();
            if (errorLabel != null) {
                showError(languageManager.getTranslation("transfer.error.load.accounts") + ": " + e.getMessage());
            }
        }
    }
    
    private void updateSoldeDisplay() {
        Compte selected = compteSourceCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (languageManager == null) {
                languageManager = LanguageManager.getInstance();
            }
            soldeSourceLabel.setText(languageManager.getTranslation("transfer.solde.available") + " : " + String.format("%.2f MAD", selected.getSolde()));
            soldeSourceLabel.setVisible(true);
        } else {
            soldeSourceLabel.setVisible(false);
        }
    }
    
    private void initializeClientDestList() {
        try {
            if (clientDestCombo == null) {
                return;
            }
            
            List<Client> allClients = clientDAO.findAll();
            
            // Filtrer les clients à exclure (Jean Dupont et Test Dupont)
            List<Client> filteredClients = allClients.stream()
                .filter(client -> {
                    String fullName = (client.getPrenom() + " " + client.getNom()).toLowerCase();
                    return !fullName.contains("jean dupont") && 
                           !fullName.contains("test dupont") &&
                           !fullName.contains("dupont jean") &&
                           !fullName.contains("dupont test");
                })
                // Ignorer le client actuel
                .filter(client -> {
                    if (authService != null && authService.getCurrentUser() != null && 
                        authService.getCurrentUser().getClient() != null) {
                        return !client.getId().equals(authService.getCurrentUser().getClient().getId());
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
            
            clientDestCombo.getItems().clear();
            clientDestCombo.getItems().addAll(filteredClients);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la liste des clients: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleClientDestSelection() {
        Client selectedClient = clientDestCombo.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            compteDestInfoLabel.setVisible(false);
            selectedDestCompte = null;
            return;
        }
        
        // Trouver le compte courant du client sélectionné
        List<Compte> comptes = compteDAO.findByClientId(selectedClient.getId());
        selectedDestCompte = comptes.stream()
            .filter(c -> c.getType() == com.banknet.model.TypeCompte.COURANT)
            .findFirst()
            .orElse(null);
        
        if (selectedDestCompte != null) {
            compteDestInfoLabel.setText("Compte sélectionné : " + selectedDestCompte.getNumeroCompte() + " (" + selectedClient.getPrenom() + " " + selectedClient.getNom() + ")");
            compteDestInfoLabel.setVisible(true);
        } else {
            compteDestInfoLabel.setText("Aucun compte courant trouvé pour ce client");
            compteDestInfoLabel.setVisible(true);
            selectedDestCompte = null;
        }
    }
    
    @FXML
    private void handleVirement() {
        hideMessages();
        
        Compte compteSource = compteSourceCombo.getSelectionModel().getSelectedItem();
        String montantStr = montantField.getText().trim();
        String libelle = libelleField.getText().trim();
        
        // Validations
        if (compteSource == null) {
            showError("Veuillez sélectionner votre compte source");
            return;
        }
        
        if (selectedDestCompte == null) {
            showError("Veuillez sélectionner un client destinataire");
            return;
        }
        
        if (montantStr.isEmpty()) {
            showError("Veuillez saisir un montant");
            return;
        }
        
        BigDecimal montant;
        try {
            montant = new BigDecimal(montantStr);
            if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Le montant doit être positif");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Montant invalide");
            return;
        }
        
        // Vérifier que ce n'est pas un de nos propres comptes
        boolean isOwnAccount = clientComptes.stream()
            .anyMatch(c -> c.getId().equals(selectedDestCompte.getId()));
        
        if (isOwnAccount) {
            showError(languageManager != null ? languageManager.getTranslation("transfer.error.own.account") : 
                     "Ce compte vous appartient. Utilisez 'Virement Interne' pour transférer entre vos comptes.");
            return;
        }
        
        // Effectuer le virement
        try {
            transactionService.virement(
                compteSource.getId(),
                selectedDestCompte.getId(),
                montant,
                libelle.isEmpty() ? (languageManager != null ? languageManager.getTranslation("transaction.transfer.to") : "Virement vers") + " " + selectedDestCompte.getNumeroCompte() : libelle
            );
            
            String successMsg = String.format(languageManager.getTranslation("transfer.success.format.external"), 
                montant, selectedDestCompte.getNumeroCompte(),
                selectedDestCompte.getClient().getPrenom(), selectedDestCompte.getClient().getNom());
            showSuccess(successMsg);
            
            // Rafraîchir le compte source
            compteSource = new CompteDAO().findById(compteSource.getId());
            updateComboBoxes();
            updateSoldeDisplay();
            
            // Fermer après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) montantField.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
        } catch (SoldeInsuffisantException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError(languageManager.getTranslation("transfer.error.generic") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateComboBoxes() {
        if (authService.getCurrentUser().getClient() != null) {
            clientComptes = new CompteDAO().findByClientId(authService.getCurrentUser().getClient().getId());
            Compte sourceSelected = compteSourceCombo.getSelectionModel().getSelectedItem();
            
            compteSourceCombo.getItems().clear();
            compteSourceCombo.getItems().addAll(clientComptes);
            
            if (sourceSelected != null) {
                compteSourceCombo.getSelectionModel().select(sourceSelected);
            }
        }
    }
    
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) montantField.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }
    
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    private void hideMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }
}
