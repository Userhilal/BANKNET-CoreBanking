package com.banknet.controller;

import com.banknet.dao.CompteDAO;
import com.banknet.exception.SoldeInsuffisantException;
import com.banknet.model.Compte;
import com.banknet.model.TypeCompte;
import com.banknet.service.AuthService;
import com.banknet.service.TransactionService;
import com.banknet.util.AccountTypeTranslator;
import com.banknet.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class VirementInterneController {
    
    @FXML
    private ComboBox<Compte> compteSourceCombo;
    
    @FXML
    private ComboBox<Compte> compteDestCombo;
    
    @FXML
    private TextField montantField;
    
    @FXML
    private TextField libelleField;
    
    @FXML
    private Label soldeSourceLabel;
    
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
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.transactionService = new TransactionService();
        // Utiliser Platform.runLater pour s'assurer que les champs FXML sont initialisés
        javafx.application.Platform.runLater(() -> {
            try {
                updateTranslations(); // Mettre à jour les traductions
                initializeComptes();
            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
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
                titleLabel.setText(languageManager.getTranslation("transfer.internal.title"));
            }
            if (subtitleLabel != null) {
                subtitleLabel.setText(languageManager.getTranslation("transfer.internal.subtitle"));
            }
            if (sourceAccountLabel != null) {
                sourceAccountLabel.setText(languageManager.getTranslation("transfer.source.account"));
            }
            if (destAccountLabel != null) {
                destAccountLabel.setText(languageManager.getTranslation("transfer.dest.account"));
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
                compteSourceCombo.setPromptText(languageManager.getTranslation("transfer.source.select"));
            }
            if (compteDestCombo != null) {
                compteDestCombo.setPromptText(languageManager.getTranslation("transfer.dest.select"));
            }
            if (montantField != null) {
                montantField.setPromptText("0.00");
            }
            if (libelleField != null) {
                libelleField.setPromptText(languageManager.getTranslation("transfer.label.example"));
            }
            
            // Titre de la fenêtre
            Stage stage = (Stage) (titleLabel != null && titleLabel.getScene() != null ? 
                                  titleLabel.getScene().getWindow() : null);
            if (stage != null) {
                stage.setTitle(languageManager.getTranslation("transfer.internal.title"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des traductions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void initialize() {
        try {
            System.out.println("VirementInterneController.initialize() - Début");
            
            languageManager = LanguageManager.getInstance();
            
            if (errorLabel != null) {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
            if (successLabel != null) {
                successLabel.setVisible(false);
                successLabel.setManaged(false);
            }
            
            // Charger les traductions
            updateTranslations();
            
            // Configurer l'affichage des ComboBox
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
            
            if (compteDestCombo != null) {
                compteDestCombo.setCellFactory(param -> new ListCell<Compte>() {
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
                compteDestCombo.setButtonCell(new ListCell<Compte>() {
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
            }
            
            // Initialiser les comptes après un court délai pour s'assurer que setAuthService a été appelé
            javafx.application.Platform.runLater(() -> {
                if (authService != null) {
                    System.out.println("VirementInterneController: Initialisation des comptes...");
                    initializeComptes();
                } else {
                    System.err.println("ATTENTION: authService est null dans initialize()");
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de VirementInterneController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeComptes() {
        try {
            System.out.println("initializeComptes() - Début");
            
            if (authService == null || authService.getCurrentUser() == null) {
                System.err.println("VirementInterneController: authService ou currentUser est null");
                return;
            }
            
            if (compteSourceCombo == null || compteDestCombo == null) {
                System.err.println("VirementInterneController: Les ComboBox ne sont pas initialisées");
                System.err.println("  compteSourceCombo: " + (compteSourceCombo != null ? "OK" : "NULL"));
                System.err.println("  compteDestCombo: " + (compteDestCombo != null ? "OK" : "NULL"));
                return;
            }
            
            if (authService.getCurrentUser().getClient() != null) {
                Long clientId = authService.getCurrentUser().getClient().getId();
                System.out.println("Chargement des comptes pour le client ID: " + clientId);
                
                clientComptes = new CompteDAO().findByClientId(clientId);
                
                System.out.println("Nombre de comptes trouvés: " + (clientComptes != null ? clientComptes.size() : 0));
                
                if (clientComptes != null && !clientComptes.isEmpty()) {
                    // Vider les ComboBox d'abord
                    compteSourceCombo.getItems().clear();
                    compteDestCombo.getItems().clear();
                    
                    // Ajouter les comptes
                    compteSourceCombo.getItems().addAll(clientComptes);
                    compteDestCombo.getItems().addAll(clientComptes);
                    
                    System.out.println("Comptes ajoutés aux ComboBox: " + compteSourceCombo.getItems().size());
                    
                    // Afficher les informations de chaque compte pour debug
                    for (Compte c : clientComptes) {
                        System.out.println("  - Compte: " + c.getNumeroCompte() + " (" + c.getType() + ") - Solde: " + c.getSolde());
                    }
                    
                    updateSoldeDisplay();
                } else {
                    System.err.println("VirementInterneController: Aucun compte trouvé pour le client ID: " + clientId);
                    if (languageManager == null) {
                        languageManager = LanguageManager.getInstance();
                    }
                    showError(languageManager.getTranslation("transfer.error.no.accounts"));
                }
            } else {
                System.err.println("VirementInterneController: Le client est null pour l'utilisateur " + authService.getCurrentUser().getLogin());
            }
        } catch (Exception e) {
            System.err.println("Erreur dans initializeComptes: " + e.getMessage());
            e.printStackTrace();
            showError(languageManager.getTranslation("transfer.error.load.accounts") + ": " + e.getMessage());
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
    
    @FXML
    private void handleVirement() {
        hideMessages();
        
        Compte compteSource = compteSourceCombo.getSelectionModel().getSelectedItem();
        Compte compteDest = compteDestCombo.getSelectionModel().getSelectedItem();
        String montantStr = montantField.getText().trim();
        String libelle = libelleField.getText().trim();
        
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        // Validations
        if (compteSource == null || compteDest == null) {
            showError(languageManager.getTranslation("transfer.error.select.both"));
            return;
        }
        
        if (compteSource.getId().equals(compteDest.getId())) {
            showError(languageManager.getTranslation("transfer.error.same.account"));
            return;
        }
        
        if (montantStr.isEmpty()) {
            showError(languageManager.getTranslation("transfer.error.enter.amount"));
            return;
        }
        
        BigDecimal montant;
        try {
            montant = new BigDecimal(montantStr);
            if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                showError(languageManager.getTranslation("transfer.error.positive.amount"));
                return;
            }
        } catch (NumberFormatException e) {
            showError(languageManager.getTranslation("transfer.error.invalid.amount"));
            return;
        }
        
        // Effectuer le virement
        try {
            transactionService.virement(
                compteSource.getId(),
                compteDest.getId(),
                montant,
                libelle.isEmpty() ? (languageManager != null ? languageManager.getTranslation("transaction.internal.transfer") : "Virement interne") : libelle
            );
            
            if (languageManager == null) {
                languageManager = LanguageManager.getInstance();
            }
            String successMsg = String.format(languageManager.getTranslation("transfer.success.format"), 
                montant, compteSource.getNumeroCompte(), compteDest.getNumeroCompte());
            showSuccess(successMsg);
            
            // Rafraîchir les comptes
            compteSource = new CompteDAO().findById(compteSource.getId());
            compteDest = new CompteDAO().findById(compteDest.getId());
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
            if (languageManager == null) {
                languageManager = LanguageManager.getInstance();
            }
            showError(e.getMessage());
        } catch (Exception e) {
            if (languageManager == null) {
                languageManager = LanguageManager.getInstance();
            }
            showError(languageManager.getTranslation("transfer.error.generic") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateComboBoxes() {
        if (authService.getCurrentUser().getClient() != null) {
            clientComptes = new CompteDAO().findByClientId(authService.getCurrentUser().getClient().getId());
            Compte sourceSelected = compteSourceCombo.getSelectionModel().getSelectedItem();
            Compte destSelected = compteDestCombo.getSelectionModel().getSelectedItem();
            
            compteSourceCombo.getItems().clear();
            compteDestCombo.getItems().clear();
            compteSourceCombo.getItems().addAll(clientComptes);
            compteDestCombo.getItems().addAll(clientComptes);
            
            if (sourceSelected != null) {
                compteSourceCombo.getSelectionModel().select(sourceSelected);
            }
            if (destSelected != null) {
                compteDestCombo.getSelectionModel().select(destSelected);
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
