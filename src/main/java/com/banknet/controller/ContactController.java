package com.banknet.controller;

import com.banknet.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ContactController {
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label subtitleLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label emailValueLabel;
    
    @FXML
    private Label phoneLabel;
    
    @FXML
    private Label phoneValueLabel;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private Button closeButtonBottom;
    
    private LanguageManager languageManager;
    
    @FXML
    public void initialize() {
        languageManager = LanguageManager.getInstance();
        updateTranslations();
        
        // Valeurs fixes
        emailValueLabel.setText("BankNetMaroc@gmail.ma");
        phoneValueLabel.setText("0522707008");
    }
    
    private void updateTranslations() {
        titleLabel.setText(languageManager.getTranslation("contact.title"));
        subtitleLabel.setText(languageManager.getTranslation("contact.subtitle"));
        emailLabel.setText(languageManager.getTranslation("contact.email") + ":");
        phoneLabel.setText(languageManager.getTranslation("contact.phone") + ":");
        closeButton.setText(languageManager.getTranslation("contact.close"));
        closeButtonBottom.setText(languageManager.getTranslation("contact.close"));
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}

