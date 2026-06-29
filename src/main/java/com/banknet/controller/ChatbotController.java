package com.banknet.controller;

import com.banknet.service.ChatbotService;
import com.banknet.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ChatbotController {
    
    @FXML
    private VBox chatContainer;
    
    @FXML
    private TextField questionField;
    
    @FXML
    private ScrollPane chatScrollPane;
    
    @FXML
    private javafx.scene.control.Button sendButton;
    
    private ChatbotService chatbotService;
    private LanguageManager languageManager;
    
    @FXML
    public void initialize() {
        this.chatbotService = new ChatbotService();
        this.languageManager = LanguageManager.getInstance();
        String welcomeMsg = languageManager != null ? languageManager.getTranslation("chatbot.welcome") : "Bonjour ! Je suis votre assistant bancaire. Comment puis-je vous aider ?";
        addBotMessage(welcomeMsg);
    }
    
    @FXML
    private void handleSendQuestion() {
        String question = questionField.getText().trim();
        if (question.isEmpty()) {
            return;
        }
        
        // Afficher la question de l'utilisateur
        addUserMessage(question);
        questionField.clear();
        
        // Obtenir la réponse du chatbot
        String response = chatbotService.obtenirReponse(question);
        
        // Simuler un délai pour un effet plus naturel
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            javafx.application.Platform.runLater(() -> addBotMessage(response));
        });
    }
    
    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-background-color: #0056D2; -fx-background-radius: 12px; " +
            "-fx-text-fill: white; -fx-padding: 10px 15px; -fx-max-width: 400px;");
        
        messageBox.getChildren().add(messageLabel);
        chatContainer.getChildren().add(messageBox);
        
        scrollToBottom();
    }
    
    private void addBotMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));
        
        Label botIcon = new Label("🤖");
        botIcon.setStyle("-fx-font-size: 24px; -fx-padding: 0 10px 0 0;");
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 12px; " +
            "-fx-text-fill: #1F2937; -fx-padding: 10px 15px; -fx-max-width: 400px;");
        
        messageBox.getChildren().addAll(botIcon, messageLabel);
        chatContainer.getChildren().add(messageBox);
        
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        javafx.application.Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }
}

