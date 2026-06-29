package com.banknet.controller;

import com.banknet.dao.CompteDAO;
import com.banknet.model.Client;
import com.banknet.model.Compte;
import com.banknet.model.UserAccount;
import com.banknet.service.AuthService;
import com.banknet.service.ObjectifEpargneService;
import com.banknet.util.AccountTypeTranslator;
import com.banknet.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AddObjectifController {
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label libelleLabel;
    
    @FXML
    private TextField libelleField;
    
    @FXML
    private Label compteLabel;
    
    @FXML
    private ComboBox<Compte> compteCombo;
    
    @FXML
    private Label montantLabel;
    
    @FXML
    private TextField montantField;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private DatePicker dateCiblePicker;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button createButton;
    
    @FXML
    private Button cancelButton;
    
    private AuthService authService;
    private ObjectifEpargneService objectifService;
    private CompteDAO compteDAO;
    private LanguageManager languageManager;
    private ObjectifsEpargneController parentController;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.objectifService = new ObjectifEpargneService();
        this.compteDAO = new CompteDAO();
        this.languageManager = LanguageManager.getInstance();
        initializeComptes();
        javafx.application.Platform.runLater(() -> updateTranslations());
    }
    
    public void setParentController(ObjectifsEpargneController parentController) {
        this.parentController = parentController;
    }
    
    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        dateCiblePicker.setValue(LocalDate.now().plusMonths(6)); // Date par défaut : 6 mois
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        updateTranslations();
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        if (titleLabel != null) {
            titleLabel.setText("🎯 " + languageManager.getTranslation("objectif.new.title"));
        }
        if (libelleLabel != null) {
            libelleLabel.setText(languageManager.getTranslation("objectif.label.text"));
        }
        if (libelleField != null) {
            libelleField.setPromptText(languageManager.getTranslation("objectif.label.placeholder"));
        }
        if (compteLabel != null) {
            compteLabel.setText(languageManager.getTranslation("objectif.account"));
        }
        if (compteCombo != null) {
            compteCombo.setPromptText(languageManager.getTranslation("objectif.account.select"));
        }
        if (montantLabel != null) {
            montantLabel.setText(languageManager.getTranslation("objectif.amount"));
        }
        if (montantField != null) {
            montantField.setPromptText("Ex: 50000");
        }
        if (dateLabel != null) {
            dateLabel.setText(languageManager.getTranslation("objectif.target.date"));
        }
        if (createButton != null) {
            createButton.setText(languageManager.getTranslation("objectif.create"));
        }
        if (cancelButton != null) {
            cancelButton.setText(languageManager.getTranslation("objectif.cancel"));
        }
        
        // Mettre à jour le titre de la fenêtre
        if (createButton != null && createButton.getScene() != null) {
            Stage stage = (Stage) createButton.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(languageManager.getTranslation("objectif.new.title"));
            }
        }
    }
    
    private void initializeComptes() {
        if (authService != null && authService.getCurrentUser() != null) {
            UserAccount currentUser = authService.getCurrentUser();
            Client client = currentUser.getClient();
            if (client != null) {
                List<Compte> comptes = compteDAO.findByClientId(client.getId());
                compteCombo.setItems(FXCollections.observableArrayList(comptes));
                compteCombo.setCellFactory(param -> new ListCell<Compte>() {
                    @Override
                    protected void updateItem(Compte compte, boolean empty) {
                        super.updateItem(compte, empty);
                        if (empty || compte == null) {
                            setText(null);
                        } else {
                            String typeTranslated = AccountTypeTranslator.translate(compte.getType(), languageManager);
                            setText(compte.getNumeroCompte() + " - " + typeTranslated + " (" + compte.getSolde() + " MAD)");
                        }
                    }
                });
                compteCombo.setButtonCell(new ListCell<Compte>() {
                    @Override
                    protected void updateItem(Compte compte, boolean empty) {
                        super.updateItem(compte, empty);
                        if (empty || compte == null) {
                            setText(null);
                        } else {
                            String typeTranslated = AccountTypeTranslator.translate(compte.getType(), languageManager);
                            setText(compte.getNumeroCompte() + " - " + typeTranslated);
                        }
                    }
                });
            }
        }
    }
    
    @FXML
    private void handleCreate() {
        hideError();
        
        // Validations
        if (libelleField.getText().trim().isEmpty()) {
            showError(languageManager != null ? languageManager.getTranslation("objectif.enter.label") : "Veuillez entrer un libellé");
            return;
        }
        
        if (compteCombo.getValue() == null) {
            showError(languageManager != null ? languageManager.getTranslation("objectif.select.account") : "Veuillez sélectionner un compte");
            return;
        }
        
        BigDecimal montantCible;
        try {
            montantCible = new BigDecimal(montantField.getText().trim());
            if (montantCible.compareTo(BigDecimal.ZERO) <= 0) {
                showError(languageManager != null ? languageManager.getTranslation("objectif.target.must.positive") : "Le montant cible doit être positif");
                return;
            }
        } catch (NumberFormatException e) {
            showError(languageManager != null ? languageManager.getTranslation("objectif.invalid.amount") : "Montant invalide");
            return;
        }
        
        if (dateCiblePicker.getValue() == null || dateCiblePicker.getValue().isBefore(LocalDate.now())) {
            showError(languageManager != null ? languageManager.getTranslation("objectif.invalid.date") : "Date cible invalide (doit être dans le futur)");
            return;
        }
        
        // Créer l'objectif
        try {
            UserAccount currentUser = authService.getCurrentUser();
            Client client = currentUser.getClient();
            
            objectifService.creerObjectif(
                client,
                compteCombo.getValue().getId(),
                libelleField.getText().trim(),
                montantCible,
                dateCiblePicker.getValue()
            );
            
            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showError((languageManager != null ? languageManager.getTranslation("objectif.create.error") : "Erreur lors de la création") + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}

