package com.banknet.controller;

import com.banknet.dao.ClientDAO;
import com.banknet.dao.UserAccountDAO;
import com.banknet.model.Client;
import com.banknet.model.Role;
import com.banknet.model.UserAccount;
import com.banknet.service.AuthService;
import com.banknet.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class ActivationController {
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label subtitleLabel;
    
    @FXML
    private Label clientIdLabel;
    
    @FXML
    private TextField clientIdField;
    
    @FXML
    private Label cinLabel;
    
    @FXML
    private TextField cinField;
    
    @FXML
    private Label loginLabel;
    
    @FXML
    private TextField loginField;
    
    @FXML
    private Label passwordLabel;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label confirmPasswordLabel;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Label successLabel;
    
    @FXML
    private Button activateButton;
    
    @FXML
    private Button cancelButton;
    
    private AuthService authService;
    private ClientDAO clientDAO;
    private UserAccountDAO userAccountDAO;
    private LanguageManager languageManager;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.clientDAO = new ClientDAO();
        this.userAccountDAO = new UserAccountDAO();
        this.languageManager = LanguageManager.getInstance();
        javafx.application.Platform.runLater(() -> updateTranslations());
    }
    
    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
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
            titleLabel.setText(languageManager.getTranslation("activation.title"));
        }
        if (subtitleLabel != null) {
            subtitleLabel.setText(languageManager.getTranslation("activation.subtitle"));
        }
        if (clientIdLabel != null) {
            clientIdLabel.setText(languageManager.getTranslation("activation.client.id"));
        }
        if (clientIdField != null) {
            clientIdField.setPromptText(languageManager.getTranslation("activation.client.id.placeholder"));
        }
        if (cinLabel != null) {
            cinLabel.setText(languageManager.getTranslation("activation.cin"));
        }
        if (cinField != null) {
            cinField.setPromptText(languageManager.getTranslation("activation.cin.placeholder"));
        }
        if (loginLabel != null) {
            loginLabel.setText(languageManager.getTranslation("activation.login"));
        }
        if (loginField != null) {
            loginField.setPromptText(languageManager.getTranslation("activation.login.placeholder"));
        }
        if (passwordLabel != null) {
            passwordLabel.setText(languageManager.getTranslation("activation.password"));
        }
        if (passwordField != null) {
            passwordField.setPromptText(languageManager.getTranslation("activation.password.placeholder"));
        }
        if (confirmPasswordLabel != null) {
            confirmPasswordLabel.setText(languageManager.getTranslation("activation.confirm.password"));
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText(languageManager.getTranslation("activation.confirm.password.placeholder"));
        }
        if (activateButton != null) {
            activateButton.setText(languageManager.getTranslation("activation.activate"));
        }
        if (cancelButton != null) {
            cancelButton.setText(languageManager.getTranslation("objectif.cancel"));
        }
        
        // Mettre à jour le titre de la fenêtre
        if (clientIdField != null && clientIdField.getScene() != null) {
            Stage stage = (Stage) clientIdField.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(languageManager.getTranslation("activation.title"));
            }
        }
    }
    
    @FXML
    private void handleActivation() {
        // Réinitialiser les messages
        hideMessages();
        
        // Validation des champs
        String clientIdStr = clientIdField.getText().trim();
        String cin = cinField.getText().trim();
        String login = loginField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (clientIdStr.isEmpty() || cin.isEmpty() || login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError(languageManager != null ? languageManager.getTranslation("activation.empty.fields") : 
                     "Veuillez remplir tous les champs");
            return;
        }
        
        // Validation de l'ID client
        Long clientId;
        try {
            clientId = Long.parseLong(clientIdStr);
        } catch (NumberFormatException e) {
            showError(languageManager != null ? languageManager.getTranslation("activation.invalid.client.id") : 
                     "L'ID Client doit être un nombre valide");
            return;
        }
        
        // Vérifier que les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            showError(languageManager != null ? languageManager.getTranslation("activation.password.mismatch") : 
                     "Les mots de passe ne correspondent pas");
            return;
        }
        
        // Vérifier que le login n'existe pas déjà
        if (userAccountDAO.existsByLogin(login)) {
            showError(languageManager != null ? languageManager.getTranslation("activation.login.exists") : 
                     "Cet identifiant est déjà utilisé");
            return;
        }
        
        // Normaliser le CIN (supprimer les espaces, mettre en majuscules)
        cin = cin.trim().toUpperCase();
        
        // Vérifier que le client existe et correspond au CIN
        Optional<Client> clientOpt = clientDAO.findByIdAndCin(clientId, cin);
        
        if (clientOpt.isEmpty()) {
            // Essayons aussi de chercher uniquement par CIN pour aider au débogage
            Optional<Client> clientByCin = clientDAO.findByCin(cin);
            if (clientByCin.isPresent()) {
                Client foundClient = clientByCin.get();
                String msg = languageManager != null ? 
                    String.format(
                        languageManager.getTranslation("activation.client.not.found") + ". " + 
                        (languageManager.getCurrentLocale().getLanguage().equals("ar") ? "رقم البطاقة الوطنية" : 
                         languageManager.getCurrentLocale().getLanguage().equals("en") ? "CIN" : "Le CIN") + 
                        " '%s' " + (languageManager.getCurrentLocale().getLanguage().equals("ar") ? "يتطابق مع العميل" : 
                         languageManager.getCurrentLocale().getLanguage().equals("en") ? "corresponds to client" : "correspond au client") + 
                        " ID=%d (%s %s).",
                        cin, foundClient.getId(), foundClient.getPrenom(), foundClient.getNom()
                    ) :
                    String.format(
                        "Client introuvable avec cet ID (%d). Le CIN '%s' correspond au client ID=%d (%s %s).",
                        clientId, cin, foundClient.getId(), foundClient.getPrenom(), foundClient.getNom()
                    );
                showError(msg);
            } else {
                // Chercher tous les clients pour aider
                List<Client> allClients = clientDAO.findAll();
                if (!allClients.isEmpty()) {
                    String headerMsg = languageManager != null ? languageManager.getTranslation("activation.client.not.found.message") : "Client introuvable. Clients disponibles:";
                    String activatedText = languageManager != null ? languageManager.getTranslation("activation.already.activated") : "(déjà activé)";
                    String availableText = languageManager != null ? languageManager.getTranslation("activation.available") : "(disponible)";
                    StringBuilder clientsInfo = new StringBuilder(headerMsg + "\n");
                    for (Client c : allClients) {
                        boolean hasAccount = c.getUserAccount() != null;
                        clientsInfo.append(String.format("- ID: %d, CIN: %s, Nom: %s %s %s\n",
                            c.getId(), c.getCin(), c.getPrenom(), c.getNom(),
                            hasAccount ? activatedText : availableText));
                    }
                    showError(clientsInfo.toString());
                } else {
                    showError(languageManager != null ? languageManager.getTranslation("error.no.data") : "Aucun client trouvé dans la base de données. Vérifiez votre ID Client et votre CIN.");
                }
            }
            return;
        }
        
        Client client = clientOpt.get();
        
        // Vérifier que le client n'a pas déjà un compte utilisateur
        if (client.getUserAccount() != null) {
            showError(languageManager != null ? languageManager.getTranslation("alert.error.account.already.activated") : "Ce client a déjà un compte utilisateur activé");
            return;
        }
        
        // Créer le compte utilisateur
        UserAccount userAccount = new UserAccount();
        userAccount.setLogin(login);
        userAccount.setStatus(com.banknet.model.AccountStatus.ACTIF);
        userAccount.setFailedLoginAttempts(0);
        userAccount.setPasswordHash(authService.hashPassword(password));
        userAccount.setRole(Role.CLIENT);
        userAccount.setClient(client);
        
        userAccountDAO.save(userAccount);
        
        String successMsg = languageManager != null ? languageManager.getTranslation("activation.activation.success") : "Compte activé avec succès";
        String connectMsg = languageManager != null ? 
            (languageManager.getCurrentLocale().getLanguage().equals("ar") ? "يمكنك الآن تسجيل الدخول" :
             languageManager.getCurrentLocale().getLanguage().equals("en") ? "You can now log in" : "Vous pouvez maintenant vous connecter") : 
            "Vous pouvez maintenant vous connecter";
        showSuccess(successMsg + " ! " + connectMsg + ".");
        
        // Fermer la fenêtre après 2 secondes
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    Stage stage = (Stage) clientIdField.getScene().getWindow();
                    stage.close();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) clientIdField.getScene().getWindow();
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
