package com.banknet.controller;

import com.banknet.MainApp;
import com.banknet.model.Client;
import com.banknet.model.Notification;
import com.banknet.model.UserAccount;
import com.banknet.service.AuthService;
import com.banknet.service.NotificationService;
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

public class NotificationsController {
    
    @FXML
    private ListView<Notification> notificationsList;
    
    @FXML
    private Label headerLabel;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button markAllReadButton;
    
    @FXML
    private Label countLabel;
    
    @FXML
    private Label titleLabel;
    
    private AuthService authService;
    private NotificationService notificationService;
    private LanguageManager languageManager;
    private ObservableList<Notification> notifications;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.notificationService = new NotificationService();
        this.languageManager = LanguageManager.getInstance();
        initializeData();
        javafx.application.Platform.runLater(() -> updateTranslations());
    }
    
    @FXML
    public void initialize() {
        notifications = FXCollections.observableArrayList();
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        setupListView();
        updateTranslations();
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        if (headerLabel != null) {
            headerLabel.setText("🔔 " + languageManager.getTranslation("notification.title"));
        }
        if (titleLabel != null) {
            titleLabel.setText(languageManager.getTranslation("notification.your.notifications"));
        }
        if (refreshButton != null) {
            refreshButton.setText("🔄 " + languageManager.getTranslation("notification.refresh"));
        }
        if (markAllReadButton != null) {
            markAllReadButton.setText(languageManager.getTranslation("notification.mark.all.read"));
        }
        
        // Mettre à jour le titre de la fenêtre
        if (headerLabel != null && headerLabel.getScene() != null) {
            Stage stage = (Stage) headerLabel.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(languageManager.getTranslation("notification.title"));
            }
        }
    }
    
    private void setupListView() {
        notificationsList.setItems(notifications);
        notificationsList.setCellFactory(param -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification notification, boolean empty) {
                super.updateItem(notification, empty);
                if (empty || notification == null) {
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);
                    vbox.setStyle("-fx-padding: 10px; -fx-background-color: " + 
                        (notification.getLu() ? "#ffffff" : "#EBF4FF") + "; -fx-background-radius: 8px;");
                    
                    Label titreLabel = new Label(
                        (notification.getType() == Notification.NotificationType.ALERTE ? "🚨 " : 
                         notification.getType() == Notification.NotificationType.AVERTISSEMENT ? "⚠️ " :
                         notification.getType() == Notification.NotificationType.SUCCES ? "✅ " : "ℹ️ ") +
                        notification.getTitre());
                    titreLabel.setStyle("-fx-font-weight: " + (notification.getLu() ? "normal" : "bold") + "; -fx-font-size: 14px;");
                    
                    Label messageLabel = new Label(notification.getMessage());
                    messageLabel.setWrapText(true);
                    messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
                    
                    Label dateLabel = new Label(notification.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");
                    
                    vbox.getChildren().addAll(titreLabel, messageLabel, dateLabel);
                    setGraphic(vbox);
                }
            }
        });
        
        notificationsList.setOnMouseClicked(event -> {
            Notification selected = notificationsList.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.getLu()) {
                notificationService.marquerCommeLu(selected.getId());
                refreshNotifications();
            }
        });
    }
    
    private void initializeData() {
        refreshNotifications();
    }
    
    private void refreshNotifications() {
        notifications.clear();
        if (authService != null && authService.getCurrentUser() != null) {
            UserAccount currentUser = authService.getCurrentUser();
            Client client = currentUser.getClient();
            if (client != null) {
                List<Notification> notifs = notificationService.getNotificationsClient(client.getId());
                notifications.addAll(notifs);
                String notificationText = languageManager != null ? languageManager.getTranslation("notification.count") : "notification(s)";
                countLabel.setText(notifs.size() + " " + notificationText);
            }
        }
        notificationsList.refresh();
    }
    
    @FXML
    private void handleRefresh() {
        refreshNotifications();
    }
    
    @FXML
    private void handleMarkAllRead() {
        if (authService != null && authService.getCurrentUser() != null) {
            Client client = authService.getCurrentUser().getClient();
            if (client != null) {
                List<Notification> notifs = notificationService.getNotificationsNonLues(client.getId());
                for (Notification notif : notifs) {
                    notificationService.marquerCommeLu(notif.getId());
                }
                refreshNotifications();
            }
        }
    }
}

