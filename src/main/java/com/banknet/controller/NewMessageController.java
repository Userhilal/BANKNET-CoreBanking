package com.banknet.controller;

import com.banknet.model.Client;
import com.banknet.model.Role;
import com.banknet.model.UserAccount;
import com.banknet.service.AuthService;
import com.banknet.service.MessageService;
import com.banknet.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class NewMessageController {
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label sujetLabel;
    
    @FXML
    private TextField sujetField;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private TextArea contenuArea;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private Button cancelButton;
    
    private AuthService authService;
    private MessageService messageService;
    private LanguageManager languageManager;
    private MessagerieController parentController;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.messageService = new MessageService();
        this.languageManager = LanguageManager.getInstance();
        javafx.application.Platform.runLater(() -> updateTranslations());
    }
    
    public void setParentController(MessagerieController parentController) {
        this.parentController = parentController;
    }
    
    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
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
            titleLabel.setText("✉️ " + languageManager.getTranslation("messagerie.new.message") + " " + languageManager.getTranslation("messages.bank"));
        }
        if (sujetLabel != null) {
            sujetLabel.setText(languageManager.getTranslation("messages.subject"));
        }
        if (sujetField != null) {
            sujetField.setPromptText(languageManager.getTranslation("messages.subject.placeholder"));
        }
        if (messageLabel != null) {
            messageLabel.setText(languageManager.getTranslation("messages.message"));
        }
        if (contenuArea != null) {
            contenuArea.setPromptText(languageManager.getTranslation("messages.message.placeholder"));
        }
        if (sendButton != null) {
            sendButton.setText(languageManager.getTranslation("messages.send"));
        }
        if (cancelButton != null) {
            cancelButton.setText(languageManager.getTranslation("objectif.cancel"));
        }
        
        // Mettre à jour le titre de la fenêtre
        if (sendButton != null && sendButton.getScene() != null) {
            Stage stage = (Stage) sendButton.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(languageManager.getTranslation("messagerie.new.message"));
            }
        }
    }
    
    @FXML
    private void handleSend() {
        hideError();
        
        if (sujetField.getText().trim().isEmpty()) {
            String msg = languageManager != null ? 
                languageManager.getTranslation("messagerie.error.empty.subject") : "Veuillez entrer un sujet";
            if (msg.startsWith("[") && msg.endsWith("]")) {
                msg = languageManager.getCurrentLocale().getLanguage().equals("ar") ? "يرجى إدخال موضوع" :
                      languageManager.getCurrentLocale().getLanguage().equals("en") ? "Please enter a subject" : 
                      "Veuillez entrer un sujet";
            }
            showError(msg);
            return;
        }
        
        if (contenuArea.getText().trim().isEmpty()) {
            String msg = languageManager != null ? 
                languageManager.getTranslation("messagerie.error.empty.message") : "Veuillez entrer un message";
            if (msg.startsWith("[") && msg.endsWith("]")) {
                msg = languageManager.getCurrentLocale().getLanguage().equals("ar") ? "يرجى إدخال رسالة" :
                      languageManager.getCurrentLocale().getLanguage().equals("en") ? "Please enter a message" : 
                      "Veuillez entrer un message";
            }
            showError(msg);
            return;
        }
        
        try {
            UserAccount currentUser = authService.getCurrentUser();
            Client client = currentUser.getClient();
            
            messageService.envoyerMessage(client, Role.CLIENT, sujetField.getText().trim(), contenuArea.getText().trim());
            
            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            String errorMsg = languageManager != null ? 
                languageManager.getTranslation("messagerie.error.send") : "Erreur lors de l'envoi";
            if (errorMsg.startsWith("[") && errorMsg.endsWith("]")) {
                errorMsg = languageManager.getCurrentLocale().getLanguage().equals("ar") ? "خطأ في الإرسال" :
                          languageManager.getCurrentLocale().getLanguage().equals("en") ? "Error sending" : 
                          "Erreur lors de l'envoi";
            }
            showError(errorMsg + " : " + e.getMessage());
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

