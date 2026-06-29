package com.banknet.controller;

import com.banknet.MainApp;
import com.banknet.dao.CompteDAO;
import com.banknet.model.Client;
import com.banknet.model.Compte;
import com.banknet.model.ObjectifEpargne;
import com.banknet.model.UserAccount;
import com.banknet.service.AuthService;
import com.banknet.service.ObjectifEpargneService;
import com.banknet.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ObjectifsEpargneController {
    
    @FXML
    private VBox objectifsContainer;
    
    @FXML
    private Button addObjectifButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label emptyLabel;
    
    @FXML
    private Label headerTitleLabel;
    
    private AuthService authService;
    private ObjectifEpargneService objectifService;
    private CompteDAO compteDAO;
    private LanguageManager languageManager;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.objectifService = new ObjectifEpargneService();
        this.compteDAO = new CompteDAO();
        this.languageManager = LanguageManager.getInstance();
        refreshObjectifs();
    }
    
    @FXML
    public void initialize() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        updateTranslations();
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        // Traduire les éléments du FXML
        if (headerTitleLabel != null) {
            headerTitleLabel.setText("🎯 " + languageManager.getTranslation("objectif.title"));
        }
        if (titleLabel != null) {
            titleLabel.setText(languageManager.getTranslation("objectif.your.goals"));
        }
        if (emptyLabel != null) {
            String emptyText = languageManager.getTranslation("objectif.empty.message");
            if (emptyText.startsWith("[") && emptyText.endsWith("]")) {
                emptyText = "Aucun objectif d'épargne défini. Cliquez sur 'Nouvel objectif' pour en créer un.";
            }
            emptyLabel.setText(emptyText);
        }
        if (addObjectifButton != null) {
            addObjectifButton.setText("➕ " + languageManager.getTranslation("objectif.new.goal"));
        }
        if (refreshButton != null) {
            refreshButton.setText("🔄 " + languageManager.getTranslation("dashboard.admin.refresh"));
        }
        
        // Mettre à jour le titre de la fenêtre
        Stage stage = (Stage) (titleLabel != null && titleLabel.getScene() != null ? 
                              titleLabel.getScene().getWindow() : null);
        if (stage != null) {
            stage.setTitle(languageManager.getTranslation("objectif.title"));
        }
    }
    
    private void refreshObjectifs() {
        // Mettre à jour les traductions avant de rafraîchir
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        updateTranslations();
        
        objectifsContainer.getChildren().clear();
        
        if (authService != null && authService.getCurrentUser() != null) {
            UserAccount currentUser = authService.getCurrentUser();
            Client client = currentUser.getClient();
            if (client != null) {
                List<ObjectifEpargne> objectifs = objectifService.getObjectifsClient(client.getId());
                
                if (objectifs.isEmpty()) {
                    emptyLabel.setVisible(true);
                } else {
                    emptyLabel.setVisible(false);
                    for (ObjectifEpargne objectif : objectifs) {
                        objectifsContainer.getChildren().add(createObjectifCard(objectif));
                    }
                }
            }
        }
    }
    
    private VBox createObjectifCard(ObjectifEpargne objectif) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 20px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 16, 0, 0, 4);");
        
        // En-tête
        HBox header = new HBox(10);
        Label libelleLabel = new Label("🎯 " + objectif.getLibelle());
        libelleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0A1931;");
        
        // Traduire le statut
        String statutText;
        if (objectif.getStatut() == ObjectifEpargne.StatutObjectif.ATTEINT) {
            statutText = languageManager != null ? languageManager.getTranslation("objectif.status.atteint") : "ATTEINT";
        } else if (objectif.getStatut() == ObjectifEpargne.StatutObjectif.DEPASSE) {
            statutText = languageManager != null ? languageManager.getTranslation("objectif.status.depasse") : "DEPASSE";
        } else {
            statutText = languageManager != null ? languageManager.getTranslation("objectif.status.en.cours") : "EN_COURS";
        }
        Label statutLabel = new Label(statutText);
        statutLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4px 12px; -fx-background-radius: 12px; " +
            (objectif.getStatut() == ObjectifEpargne.StatutObjectif.ATTEINT ? 
                "-fx-background-color: #ECFDF5; -fx-text-fill: #059669;" :
                objectif.getStatut() == ObjectifEpargne.StatutObjectif.DEPASSE ?
                "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;" :
                "-fx-background-color: #EBF4FF; -fx-text-fill: #0056D2;"));
        
        header.getChildren().addAll(libelleLabel, statutLabel);
        HBox.setHgrow(header, javafx.scene.layout.Priority.ALWAYS);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Progression
        BigDecimal pourcentage = objectif.getPourcentageProgression();
        // Essayer d'abord objectif.progress.label, sinon objectif.progress
        String progressKey = "objectif.progress.label";
        String progressText = languageManager != null ? languageManager.getTranslation(progressKey) : "Progression";
        if (progressText.startsWith("[") && progressText.endsWith("]")) {
            progressText = languageManager != null ? languageManager.getTranslation("objectif.progress") : "Progression";
        }
        Label progressionLabel = new Label(String.format(progressText + " : %.1f%%", pourcentage));
        progressionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(pourcentage.doubleValue() / 100.0);
        progressBar.setStyle("-fx-accent: #0056D2; -fx-pref-width: 100%;");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        
        // Montants
        HBox montantsBox = new HBox(20);
        // Essayer objectif.actual, sinon objectif.current
        String actualKey = "objectif.actual";
        String actualText = languageManager != null ? languageManager.getTranslation(actualKey) : "Actuel";
        if (actualText.startsWith("[") && actualText.endsWith("]")) {
            actualText = languageManager != null ? languageManager.getTranslation("objectif.current") : "Actuel";
        }
        // Essayer objectif.target.label, sinon objectif.target
        String targetKey = "objectif.target.label";
        String targetText = languageManager != null ? languageManager.getTranslation(targetKey) : "Cible";
        if (targetText.startsWith("[") && targetText.endsWith("]")) {
            targetText = languageManager != null ? languageManager.getTranslation("objectif.target") : "Cible";
        }
        Label montantActuelLabel = new Label(String.format(actualText + " : %.2f MAD", objectif.getMontantActuel()));
        montantActuelLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        Label montantCibleLabel = new Label(String.format(targetText + " : %.2f MAD", objectif.getMontantCible()));
        montantCibleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0056D2;");
        montantsBox.getChildren().addAll(montantActuelLabel, montantCibleLabel);
        
        // Date cible
        String targetDateKey = "objectif.target.date.label";
        String targetDateText = languageManager != null ? languageManager.getTranslation(targetDateKey) : "Date cible";
        if (targetDateText.startsWith("[") && targetDateText.endsWith("]")) {
            targetDateText = languageManager != null ? languageManager.getTranslation("objectif.target.date") : "Date cible";
        }
        Label dateLabel = new Label(targetDateText + " : " + objectif.getDateCible().toString());
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");
        
        // Actions
        HBox actionsBox = new HBox(10);
        String deleteKey = "objectif.delete.button";
        String deleteText = languageManager != null ? languageManager.getTranslation(deleteKey) : "Supprimer";
        if (deleteText.startsWith("[") && deleteText.endsWith("]")) {
            deleteText = languageManager != null ? languageManager.getTranslation("objectif.delete") : "Supprimer";
        }
        Button deleteButton = new Button(deleteText);
        deleteButton.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-padding: 6px 12px;");
        deleteButton.setOnAction(e -> handleDeleteObjectif(objectif));
        actionsBox.getChildren().add(deleteButton);
        
        card.getChildren().addAll(header, statutLabel, progressionLabel, progressBar, montantsBox, dateLabel, actionsBox);
        
        return card;
    }
    
    @FXML
    private void handleAddObjectif() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/AddObjectifView.fxml"));
            Stage addStage = new Stage();
            addStage.setScene(new Scene(loader.load()));
            addStage.setTitle(languageManager != null ? languageManager.getTranslation("objectif.new.title") : "Nouvel objectif d'épargne");
            addStage.setResizable(false);
            addStage.initOwner(addObjectifButton.getScene().getWindow());
            
            AddObjectifController controller = loader.getController();
            controller.setAuthService(authService);
            controller.setParentController(this);
            
            addStage.showAndWait();
            
            refreshObjectifs();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("objectif.add.window.error") : "Impossible d'ouvrir la fenêtre d'ajout d'objectif") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    private void handleDeleteObjectif(ObjectifEpargne objectif) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(languageManager != null ? languageManager.getTranslation("objectif.delete.confirmation.title") : "Confirmation");
        confirmAlert.setHeaderText(languageManager != null ? languageManager.getTranslation("objectif.confirm.delete") : "Supprimer l'objectif ?");
        confirmAlert.setContentText(String.format(languageManager != null ? languageManager.getTranslation("objectif.delete.confirm.text") : "Êtes-vous sûr de vouloir supprimer l'objectif : %s ?", objectif.getLibelle()));
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                objectifService.supprimerObjectif(objectif.getId());
                refreshObjectifs();
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        refreshObjectifs();
    }
    
    public void refresh() {
        refreshObjectifs();
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
            } else if (title.equals("Confirmation")) {
                translatedTitle = languageManager.getTranslation("alert.confirmation");
            }
        }
        
        Alert alert = new Alert(type);
        alert.setTitle(translatedTitle);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

