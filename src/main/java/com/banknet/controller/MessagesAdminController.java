package com.banknet.controller;

import com.banknet.model.Client;
import com.banknet.model.Message;
import com.banknet.model.Role;
import com.banknet.model.UserAccount;
import com.banknet.service.AuthService;
import com.banknet.service.MessageService;
import com.banknet.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessagesAdminController {
    
    @FXML
    private ListView<Message> messagesList;
    
    @FXML
    private VBox messageDetailContainer;
    
    @FXML
    private VBox replyContainer;
    
    @FXML
    private TextArea replyTextArea;
    
    @FXML
    private Button sendReplyButton;
    
    @FXML
    private Button cancelReplyButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label headerLabel;
    
    @FXML
    private Label messagesReceivedLabel;
    
    @FXML
    private Label selectMessageLabel;
    
    @FXML
    private Label replyTitleLabel;
    
    private AuthService authService;
    private MessageService messageService;
    private LanguageManager languageManager;
    private ObservableList<Message> messages;
    private Message selectedMessage;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.messageService = new MessageService();
        this.languageManager = LanguageManager.getInstance();
        initializeData();
        javafx.application.Platform.runLater(() -> updateTranslations());
    }
    
    @FXML
    public void initialize() {
        messages = FXCollections.observableArrayList();
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        setupListView();
        replyContainer.setVisible(false);
        replyContainer.setManaged(false);
        updateTranslations();
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        if (headerLabel != null) {
            headerLabel.setText("💬 " + languageManager.getTranslation("messages.admin.title"));
        }
        if (messagesReceivedLabel != null) {
            messagesReceivedLabel.setText(languageManager.getTranslation("messages.admin.received"));
        }
        if (selectMessageLabel != null) {
            selectMessageLabel.setText(languageManager.getTranslation("messages.admin.select.message"));
        }
        if (replyTitleLabel != null) {
            replyTitleLabel.setText(languageManager.getTranslation("messages.admin.reply"));
        }
        if (replyTextArea != null) {
            replyTextArea.setPromptText(languageManager.getTranslation("messagerie.reply.placeholder"));
        }
        if (sendReplyButton != null) {
            sendReplyButton.setText(languageManager.getTranslation("messages.admin.send.reply"));
        }
        if (cancelReplyButton != null) {
            cancelReplyButton.setText(languageManager.getTranslation("objectif.cancel"));
        }
        if (refreshButton != null) {
            refreshButton.setText("🔄 " + languageManager.getTranslation("messagerie.refresh"));
        }
        
        // Mettre à jour le titre de la fenêtre
        if (headerLabel != null && headerLabel.getScene() != null) {
            javafx.stage.Stage stage = (javafx.stage.Stage) headerLabel.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(languageManager.getTranslation("messages.admin.title"));
            }
        }
    }
    
    private void setupListView() {
        messagesList.setItems(messages);
        messagesList.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Client client = message.getClient();
                    String fromText = languageManager != null ? languageManager.getTranslation("messages.from") : "De";
                    String cinText = languageManager != null ? "CIN" : "CIN";
                    String clientUnknown = languageManager != null ? languageManager.getTranslation("messages.client.unknown") : "Client inconnu";
                    String clientInfo = client != null ? 
                        client.getPrenom() + " " + client.getNom() + " (" + cinText + ": " + client.getCin() + ")" : 
                        clientUnknown;
                    String displayText = (message.getLu() ? "" : "🔴 ") + message.getSujet() + 
                        "\n" + fromText + ": " + clientInfo +
                        "\n" + message.getDateEnvoi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    setText(displayText);
                    setStyle("-fx-padding: 10px; -fx-background-color: " + 
                        (message.getLu() ? "#ffffff" : "#EBF4FF") + "; -fx-background-radius: 8px;");
                }
            }
        });
        
        messagesList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                displayMessage(newValue);
                if (!newValue.getLu()) {
                    messageService.marquerCommeLu(newValue.getId());
                    refreshMessages();
                }
            }
        });
    }
    
    private void initializeData() {
        refreshMessages();
    }
    
    private void refreshMessages() {
        messages.clear();
        // Récupérer tous les messages des clients (expéditeur = CLIENT) qui ne sont pas des réponses
        List<Message> allMessages = messageService.findAllMessagesFromClients();
        // Filtrer pour n'afficher que les messages principaux (pas les réponses)
        List<Message> messagesPrincipaux = allMessages.stream()
            .filter(m -> !m.getEstReponse())
            .collect(java.util.stream.Collectors.toList());
        messages.addAll(messagesPrincipaux);
        messagesList.refresh();
    }
    
    private void displayMessage(Message message) {
        selectedMessage = message;
        messageDetailContainer.getChildren().clear();
        
        Client client = message.getClient();
        
        // Informations du client
        String clientInfoText = languageManager != null ? languageManager.getTranslation("messages.client.info") : "Informations du client";
        Label clientLabel = new Label("📧 " + clientInfoText);
        clientLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0A1931;");
        
        VBox clientInfoBox = new VBox(8);
        clientInfoBox.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 15px; -fx-background-radius: 8px;");
        
        if (client != null) {
            String fullNameText = languageManager != null ? languageManager.getTranslation("messages.full.name") : "Nom complet";
            Label nomLabel = new Label(fullNameText + " : " + client.getPrenom() + " " + client.getNom());
            nomLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
            
            String cinLabelText = languageManager != null ? languageManager.getTranslation("messages.cin") : "CIN";
            Label cinLabel = new Label(cinLabelText + " : " + client.getCin());
            cinLabel.setStyle("-fx-font-size: 13px;");
            
            String emailText = languageManager != null ? languageManager.getTranslation("messages.email") : "Email";
            String notAvailable = languageManager != null ? languageManager.getTranslation("messages.not.available") : "Non renseigné";
            Label emailLabel = new Label(emailText + " : " + (client.getEmail() != null ? client.getEmail() : notAvailable));
            emailLabel.setStyle("-fx-font-size: 13px;");
            
            clientInfoBox.getChildren().addAll(nomLabel, cinLabel, emailLabel);
        } else {
            String notAvailableText = languageManager != null ? languageManager.getTranslation("messages.client.not.available") : "Informations client non disponibles";
            Label noClientLabel = new Label(notAvailableText);
            noClientLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
            clientInfoBox.getChildren().add(noClientLabel);
        }
        
        Separator separator1 = new Separator();
        
        // Sujet du message
        Label sujetLabel = new Label("📨 " + message.getSujet());
        sujetLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0A1931;");
        
        String receivedText = languageManager != null ? languageManager.getTranslation("messages.received") : "Reçu le";
        Label dateLabel = new Label(receivedText + " : " + message.getDateEnvoi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        // Contenu du message
        String messageText = languageManager != null ? languageManager.getTranslation("messages.message") : "Message";
        Label contenuLabel = new Label(messageText + " :");
        contenuLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextArea contenuArea = new TextArea(message.getContenu());
        contenuArea.setEditable(false);
        contenuArea.setWrapText(true);
        contenuArea.setStyle("-fx-font-size: 14px; -fx-min-height: 100px;");
        
        messageDetailContainer.getChildren().addAll(
            clientLabel, clientInfoBox, separator1, sujetLabel, dateLabel, contenuLabel, contenuArea
        );
        
        // Afficher les réponses
        try {
            List<Message> reponses = messageService.getReponses(message.getId());
            if (reponses != null && !reponses.isEmpty()) {
                Separator separator2 = new Separator();
                String repliesText = languageManager != null ? languageManager.getTranslation("messages.replies") : "Réponses";
                Label reponsesLabel = new Label(repliesText + " :");
                reponsesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                messageDetailContainer.getChildren().addAll(separator2, reponsesLabel);
                
                for (Message reponse : reponses) {
                    VBox reponseBox = new VBox(5);
                    reponseBox.setStyle("-fx-background-color: #EBF4FF; -fx-padding: 10px; -fx-background-radius: 8px;");
                    
                    String fromText = languageManager != null ? languageManager.getTranslation("messages.from") : "De";
                    String bankText = languageManager != null ? languageManager.getTranslation("messages.bank") : "Banque";
                    String clientText = languageManager != null ? languageManager.getTranslation("messages.client") : "Client";
                    Label reponseLabel = new Label(fromText + " : " + (reponse.getExpediteur() == Role.ADMIN ? bankText : clientText) + 
                        " - " + reponse.getDateEnvoi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    reponseLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
                    
                    TextArea reponseContenu = new TextArea(reponse.getContenu());
                    reponseContenu.setEditable(false);
                    reponseContenu.setWrapText(true);
                    reponseContenu.setStyle("-fx-font-size: 13px; -fx-min-height: 50px;");
                    
                    reponseBox.getChildren().addAll(reponseLabel, reponseContenu);
                    messageDetailContainer.getChildren().add(reponseBox);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Afficher le formulaire de réponse si le message vient d'un client
        if (message.getExpediteur() == Role.CLIENT) {
            replyContainer.setVisible(true);
            replyContainer.setManaged(true);
        } else {
            replyContainer.setVisible(false);
            replyContainer.setManaged(false);
        }
    }
    
    @FXML
    private void handleSendReply() {
        if (selectedMessage == null || replyTextArea.getText().trim().isEmpty()) {
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                languageManager != null ? languageManager.getTranslation("messagerie.error.empty") : "Veuillez taper une réponse", 
                Alert.AlertType.WARNING);
            return;
        }
        
        try {
            messageService.repondreMessage(selectedMessage, Role.ADMIN, replyTextArea.getText().trim());
            replyTextArea.clear();
            refreshMessages();
            displayMessage(selectedMessage);
            showAlert(languageManager != null ? languageManager.getTranslation("alert.success") : "Succès", 
                languageManager != null ? languageManager.getTranslation("messagerie.success.sent") : "Réponse envoyée avec succès", 
                Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("messagerie.error.send") : "Erreur lors de l'envoi") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleCancelReply() {
        replyTextArea.clear();
    }
    
    @FXML
    private void handleRefresh() {
        refreshMessages();
        if (selectedMessage != null) {
            // Recharger le message depuis la base pour avoir les dernières réponses
            selectedMessage = messageService.getMessage(selectedMessage.getId());
            displayMessage(selectedMessage);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        // Utiliser les traductions si languageManager est disponible
        String translatedTitle = title;
        if (languageManager != null) {
            if (title.equals("Erreur")) {
                translatedTitle = languageManager.getTranslation("alert.error");
            } else if (title.equals("Succès")) {
                translatedTitle = languageManager.getTranslation("alert.success");
            } else if (title.equals("Avertissement")) {
                translatedTitle = languageManager.getTranslation("alert.warning");
            } else if (title.equals("Information")) {
                translatedTitle = languageManager.getTranslation("alert.info");
            }
        }
        
        Alert alert = new Alert(type);
        alert.setTitle(translatedTitle);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

