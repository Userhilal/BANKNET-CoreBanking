package com.banknet.controller;

import com.banknet.MainApp;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessagerieController {
    
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
    private Button newMessageButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label headerTitleLabel;
    
    @FXML
    private Label messagesTitleLabel;
    
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
        
        // Traduire les éléments du FXML
        if (headerTitleLabel != null) {
            headerTitleLabel.setText("💬 " + languageManager.getTranslation("messagerie.title"));
        }
        if (messagesTitleLabel != null) {
            messagesTitleLabel.setText(languageManager.getTranslation("messagerie.messages"));
        }
        if (selectMessageLabel != null) {
            selectMessageLabel.setText(languageManager.getTranslation("messagerie.select.message"));
        }
        if (replyTitleLabel != null) {
            replyTitleLabel.setText(languageManager.getTranslation("messagerie.reply"));
        }
        if (newMessageButton != null) {
            newMessageButton.setText("✉️ " + languageManager.getTranslation("messagerie.new.message"));
        }
        if (refreshButton != null) {
            refreshButton.setText("🔄 " + languageManager.getTranslation("messagerie.refresh"));
        }
        if (sendReplyButton != null) {
            sendReplyButton.setText(languageManager.getTranslation("messagerie.send"));
        }
        if (cancelReplyButton != null) {
            cancelReplyButton.setText(languageManager.getTranslation("objectif.cancel"));
        }
        if (replyTextArea != null) {
            String promptText = languageManager.getTranslation("messagerie.reply.placeholder");
            if (promptText.startsWith("[") && promptText.endsWith("]")) {
                promptText = "Tapez votre réponse...";
            }
            replyTextArea.setPromptText(promptText);
        }
        
        // Mettre à jour le titre de la fenêtre
        Stage stage = (Stage) (headerTitleLabel != null && headerTitleLabel.getScene() != null ? 
                              headerTitleLabel.getScene().getWindow() : null);
        if (stage != null) {
            stage.setTitle(languageManager.getTranslation("messagerie.title"));
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
                    String displayText = (message.getLu() ? "" : "🔴 ") + message.getSujet() + 
                        "\n" + message.getDateEnvoi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    setText(displayText);
                    setStyle("-fx-padding: 8px; -fx-background-color: " + 
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
        // Mettre à jour les traductions après initialisation
        if (languageManager != null) {
            javafx.application.Platform.runLater(() -> updateTranslations());
        }
    }
    
    public void refresh() {
        refreshMessages();
        updateTranslations();
    }
    
    private void refreshMessages() {
        messages.clear();
        if (authService != null && authService.getCurrentUser() != null) {
            UserAccount currentUser = authService.getCurrentUser();
            Client client = currentUser.getClient();
            if (client != null) {
                List<Message> msgs = messageService.getMessagesClient(client.getId());
                messages.addAll(msgs);
            }
        }
        messagesList.refresh();
    }
    
    private void displayMessage(Message message) {
        // S'assurer que languageManager est initialisé
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        selectedMessage = message;
        messageDetailContainer.getChildren().clear();
        
        Label sujetLabel = new Label("📧 " + message.getSujet());
        sujetLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0A1931;");
        
        // Obtenir les traductions avec vérification
        String fromText = languageManager.getTranslation("messages.from");
        if (fromText.startsWith("[") && fromText.endsWith("]")) {
            fromText = "De"; // Fallback
        }
        String youText = languageManager.getTranslation("messages.you");
        if (youText.startsWith("[") && youText.endsWith("]")) {
            youText = "Vous"; // Fallback
        }
        String bankText = languageManager.getTranslation("messages.bank");
        if (bankText.startsWith("[") && bankText.endsWith("]")) {
            bankText = "Banque"; // Fallback
        }
        Label expediteurLabel = new Label(fromText + " : " + (message.getExpediteur() == Role.CLIENT ? youText : bankText));
        expediteurLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        String onKey = "messages.on";
        String onText = languageManager != null ? languageManager.getTranslation(onKey) : "Le";
        if (onText.startsWith("[") && onText.endsWith("]")) {
            // Fallback selon la langue
            String lang = languageManager != null ? languageManager.getCurrentLocale().getLanguage() : "fr";
            onText = "ar".equals(lang) ? "في" : "en".equals(lang) ? "On" : "Le";
        }
        Label dateLabel = new Label(onText + " : " + message.getDateEnvoi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        TextArea contenuArea = new TextArea(message.getContenu());
        contenuArea.setEditable(false);
        contenuArea.setWrapText(true);
        contenuArea.setStyle("-fx-font-size: 14px; -fx-min-height: 150px;");
        
        messageDetailContainer.getChildren().addAll(sujetLabel, expediteurLabel, dateLabel, contenuArea);
        
        // Afficher les réponses
        List<Message> reponses = messageService.getReponses(message.getId());
        if (!reponses.isEmpty()) {
            Separator separator = new Separator();
            String repliesText = languageManager.getTranslation("messages.replies");
            if (repliesText.startsWith("[") && repliesText.endsWith("]")) {
                repliesText = "Réponses"; // Fallback
            }
            Label reponsesLabel = new Label(repliesText + " :");
            reponsesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            messageDetailContainer.getChildren().addAll(separator, reponsesLabel);
            
            for (Message reponse : reponses) {
                VBox reponseBox = new VBox(5);
                reponseBox.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 10px; -fx-background-radius: 8px;");
                
                // Réutiliser les variables déjà définies plus haut dans la méthode
                Label reponseLabel = new Label(fromText + " : " + (reponse.getExpediteur() == Role.CLIENT ? youText : bankText) + 
                    " - " + reponse.getDateEnvoi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                reponseLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
                
                Label reponseContenu = new Label(reponse.getContenu());
                reponseContenu.setWrapText(true);
                reponseContenu.setStyle("-fx-font-size: 13px;");
                
                reponseBox.getChildren().addAll(reponseLabel, reponseContenu);
                messageDetailContainer.getChildren().add(reponseBox);
            }
        }
        
        // Afficher le formulaire de réponse si l'utilisateur est client
        if (authService.getCurrentUser().getRole() == Role.CLIENT && message.getExpediteur() != Role.CLIENT) {
            replyContainer.setVisible(true);
            replyContainer.setManaged(true);
        } else {
            replyContainer.setVisible(false);
            replyContainer.setManaged(false);
        }
    }
    
    @FXML
    private void handleNewMessage() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/NewMessageView.fxml"));
            Stage newMessageStage = new Stage();
            newMessageStage.setScene(new Scene(loader.load()));
            newMessageStage.setTitle(languageManager != null ? languageManager.getTranslation("messages.new.message") : "Nouveau message");
            newMessageStage.setResizable(false);
            newMessageStage.initOwner(newMessageButton.getScene().getWindow());
            
            NewMessageController controller = loader.getController();
            controller.setAuthService(authService);
            controller.setParentController(this);
            
            newMessageStage.showAndWait();
            
            refreshMessages();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("messagerie.error.new") : "Impossible d'ouvrir la fenêtre de nouveau message") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
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
            UserAccount currentUser = authService.getCurrentUser();
            messageService.repondreMessage(selectedMessage, currentUser.getRole(), replyTextArea.getText().trim());
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

