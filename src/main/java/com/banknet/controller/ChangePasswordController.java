package com.banknet.controller;

import com.banknet.service.AuthService;
import com.banknet.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ChangePasswordController {
    
    @FXML
    private PasswordField oldPasswordField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField oldPasswordVisibleField;
    
    @FXML
    private TextField newPasswordVisibleField;
    
    @FXML
    private TextField confirmPasswordVisibleField;
    
    @FXML
    private Button toggleOldPasswordButton;
    
    @FXML
    private Button toggleNewPasswordButton;
    
    @FXML
    private Button toggleConfirmPasswordButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Label successLabel;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label subtitleLabel;
    
    @FXML
    private Label oldPasswordLabel;
    
    @FXML
    private Label newPasswordLabel;
    
    @FXML
    private Label confirmPasswordLabel;
    
    @FXML
    private Button changeButton;
    
    @FXML
    private Button cancelButton;
    
    private AuthService authService;
    private LanguageManager languageManager;
    private boolean isOldPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.languageManager = LanguageManager.getInstance();
    }
    
    @FXML
    public void initialize() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        // Initialiser les champs de mot de passe visibles (cachés par défaut)
        oldPasswordVisibleField.setVisible(false);
        oldPasswordVisibleField.setManaged(false);
        oldPasswordVisibleField.textProperty().bindBidirectional(oldPasswordField.textProperty());
        
        newPasswordVisibleField.setVisible(false);
        newPasswordVisibleField.setManaged(false);
        newPasswordVisibleField.textProperty().bindBidirectional(newPasswordField.textProperty());
        
        confirmPasswordVisibleField.setVisible(false);
        confirmPasswordVisibleField.setManaged(false);
        confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
        
        // Mettre à jour les traductions
        updateTranslations();
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        if (titleLabel != null) {
            titleLabel.setText("🔑 " + languageManager.getTranslation("password.change.title"));
        }
        if (subtitleLabel != null) {
            subtitleLabel.setText(languageManager.getTranslation("password.change.subtitle"));
        }
        if (oldPasswordLabel != null) {
            oldPasswordLabel.setText(languageManager.getTranslation("password.change.old.label"));
        }
        if (newPasswordLabel != null) {
            newPasswordLabel.setText(languageManager.getTranslation("password.change.new.label"));
        }
        if (confirmPasswordLabel != null) {
            confirmPasswordLabel.setText(languageManager.getTranslation("password.change.confirm.label"));
        }
        if (oldPasswordField != null) {
            oldPasswordField.setPromptText(languageManager.getTranslation("password.change.old.placeholder"));
            oldPasswordVisibleField.setPromptText(languageManager.getTranslation("password.change.old.placeholder"));
        }
        if (newPasswordField != null) {
            newPasswordField.setPromptText(languageManager.getTranslation("password.change.new.placeholder"));
            newPasswordVisibleField.setPromptText(languageManager.getTranslation("password.change.new.placeholder"));
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText(languageManager.getTranslation("password.change.confirm.placeholder"));
            confirmPasswordVisibleField.setPromptText(languageManager.getTranslation("password.change.confirm.placeholder"));
        }
        if (changeButton != null) {
            changeButton.setText(languageManager.getTranslation("password.change.button"));
        }
        if (cancelButton != null) {
            cancelButton.setText(languageManager.getTranslation("password.change.cancel"));
        }
        
        // Mettre à jour le titre de la fenêtre
        Stage stage = (Stage) (titleLabel != null && titleLabel.getScene() != null ? 
                              titleLabel.getScene().getWindow() : null);
        if (stage != null) {
            stage.setTitle(languageManager.getTranslation("password.change.title"));
        }
    }
    
    @FXML
    private void handleToggleOldPassword() {
        isOldPasswordVisible = !isOldPasswordVisible;
        togglePasswordVisibility(oldPasswordField, oldPasswordVisibleField, isOldPasswordVisible, toggleOldPasswordButton);
    }
    
    @FXML
    private void handleToggleNewPassword() {
        isNewPasswordVisible = !isNewPasswordVisible;
        togglePasswordVisibility(newPasswordField, newPasswordVisibleField, isNewPasswordVisible, toggleNewPasswordButton);
    }
    
    @FXML
    private void handleToggleConfirmPassword() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        togglePasswordVisibility(confirmPasswordField, confirmPasswordVisibleField, isConfirmPasswordVisible, toggleConfirmPasswordButton);
    }
    
    private void togglePasswordVisibility(PasswordField passwordField, TextField visibleField, boolean isVisible, Button toggleButton) {
        if (isVisible) {
            visibleField.setText(passwordField.getText());
            visibleField.setVisible(true);
            visibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visibleField.requestFocus();
            toggleButton.setText("🙈");
        } else {
            passwordField.setText(visibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visibleField.setVisible(false);
            visibleField.setManaged(false);
            passwordField.requestFocus();
            toggleButton.setText("👁");
        }
    }
    
    @FXML
    private void handleChange() {
        hideMessages();
        
        String oldPassword = isOldPasswordVisible ? oldPasswordVisibleField.getText() : oldPasswordField.getText();
        String newPassword = isNewPasswordVisible ? newPasswordVisibleField.getText() : newPasswordField.getText();
        String confirmPassword = isConfirmPasswordVisible ? confirmPasswordVisibleField.getText() : confirmPasswordField.getText();
        
        // Validations
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError(languageManager != null ? languageManager.getTranslation("password.change.empty.fields") : "Veuillez remplir tous les champs");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError(languageManager != null ? languageManager.getTranslation("password.change.mismatch") : "Les nouveaux mots de passe ne correspondent pas");
            return;
        }
        
        if (newPassword.length() < 6) {
            showError(languageManager != null ? languageManager.getTranslation("password.change.too.short") : "Le nouveau mot de passe doit contenir au moins 6 caractères");
            return;
        }
        
        if (oldPassword.equals(newPassword)) {
            showError(languageManager != null ? languageManager.getTranslation("password.change.same") : "Le nouveau mot de passe doit être différent de l'ancien");
            return;
        }
        
        // Changer le mot de passe
        try {
            Long userId = authService.getCurrentUser().getId();
            boolean success = authService.changePassword(userId, oldPassword, newPassword);
            
            if (success) {
                showSuccess(languageManager != null ? languageManager.getTranslation("password.change.success") : "Mot de passe changé avec succès !");
                
                // Fermer après 2 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            Stage stage = (Stage) changeButton.getScene().getWindow();
                            stage.close();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } else {
                showError(languageManager != null ? languageManager.getTranslation("password.change.incorrect") : "Ancien mot de passe incorrect. Veuillez réessayer.");
            }
        } catch (Exception e) {
            showError((languageManager != null ? languageManager.getTranslation("password.change.error.generic") : "Erreur lors du changement de mot de passe") + " : " + e.getMessage());
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

