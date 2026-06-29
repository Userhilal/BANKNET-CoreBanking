package com.banknet.controller;

import com.banknet.MainApp;
import com.banknet.dao.CompteDAO;
import com.banknet.dao.TransactionDAO;
import com.banknet.dao.UserAccountDAO;
import com.banknet.model.AccountStatus;
import com.banknet.model.Transaction;
import com.banknet.model.UserAccount;
import com.banknet.dao.MessageDAO;
import com.banknet.model.Message;
import com.banknet.model.Role;
import com.banknet.service.AuthService;
import com.banknet.service.NotificationService;
import com.banknet.service.StatistiqueService;
import com.banknet.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {
    
    @FXML
    private Label userLabel;
    
    @FXML
    private Label soldeTotalLabel;
    
    @FXML
    private Label nombreComptesLabel;
    
    @FXML
    private Label soldeMoyenLabel;
    
    @FXML
    private TableView<Transaction> transactionTable;
    
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    
    @FXML
    private TableColumn<Transaction, String> typeColumn;
    
    @FXML
    private TableColumn<Transaction, String> montantColumn;
    
    @FXML
    private TableColumn<Transaction, String> libelleColumn;
    
    @FXML
    private TableColumn<Transaction, String> compteSourceColumn;
    
    @FXML
    private TableColumn<Transaction, String> compteDestColumn;
    
    private AuthService authService;
    private StatistiqueService statistiqueService;
    private NotificationService notificationService;
    private LanguageManager languageManager;
    private ObservableList<Transaction> transactions;
    
    @FXML
    private Button messagesButton;
    
    @FXML
    private Label messagesBadge;
    
    @FXML
    private Label soldeTotalTitleLabel;
    
    @FXML
    private Label nombreComptesTitleLabel;
    
    @FXML
    private Label soldeMoyenTitleLabel;
    
    @FXML
    private Label transactionsTitleLabel;
    
    @FXML
    private Button activateButton;
    
    @FXML
    private Button blockedAccountsButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button logoutButton;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.statistiqueService = new StatistiqueService();
        this.notificationService = new NotificationService();
        this.languageManager = LanguageManager.getInstance();
        updateTranslations();
        initializeData();
    }
    
    @FXML
    public void initialize() {
        transactions = FXCollections.observableArrayList();
        setupTableColumns();
        setupFullscreenToggle();
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        updateTranslations();
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        try {
            // Titre de la fenêtre
            if (userLabel != null && userLabel.getScene() != null) {
                Stage stage = (Stage) userLabel.getScene().getWindow();
                if (stage != null) {
                    stage.setTitle("BANKNET - " + languageManager.getTranslation("dashboard.admin.title"));
                }
            }
            
            // Labels de cartes
            if (soldeTotalTitleLabel != null) {
                soldeTotalTitleLabel.setText(languageManager.getTranslation("dashboard.admin.total"));
            }
            if (nombreComptesTitleLabel != null) {
                nombreComptesTitleLabel.setText(languageManager.getTranslation("dashboard.admin.accounts"));
            }
            if (soldeMoyenTitleLabel != null) {
                soldeMoyenTitleLabel.setText(languageManager.getTranslation("dashboard.admin.average"));
            }
            
            // Boutons
            if (activateButton != null) {
                activateButton.setText(languageManager.getTranslation("dashboard.admin.activate"));
            }
            if (blockedAccountsButton != null) {
                blockedAccountsButton.setText(languageManager.getTranslation("dashboard.admin.blocked"));
            }
            if (messagesButton != null) {
                messagesButton.setText("💬 " + languageManager.getTranslation("dashboard.admin.messages"));
            }
            if (refreshButton != null) {
                refreshButton.setText(languageManager.getTranslation("dashboard.admin.refresh"));
            }
            if (logoutButton != null) {
                logoutButton.setText(languageManager.getTranslation("dashboard.logout"));
            }
            
            // Label utilisateur
            if (userLabel != null && authService != null && authService.getCurrentUser() != null) {
                String connectedText = languageManager.getTranslation("dashboard.admin.connected");
                userLabel.setText(connectedText + " : " + authService.getCurrentUser().getLogin() + 
                    " (" + authService.getCurrentUser().getRole() + ")");
            }
            
            // Titre transactions
            if (transactionsTitleLabel != null) {
                transactionsTitleLabel.setText(languageManager.getTranslation("dashboard.admin.transactions"));
            }
            
            // Colonnes du tableau
            if (dateColumn != null) {
                dateColumn.setText(languageManager.getTranslation("dashboard.table.date"));
            }
            if (typeColumn != null) {
                typeColumn.setText(languageManager.getTranslation("dashboard.table.type"));
            }
            if (montantColumn != null) {
                montantColumn.setText(languageManager.getTranslation("dashboard.table.amount"));
            }
            if (libelleColumn != null) {
                libelleColumn.setText(languageManager.getTranslation("dashboard.table.label"));
            }
            if (compteSourceColumn != null) {
                compteSourceColumn.setText(languageManager.getTranslation("dashboard.table.source"));
            }
            if (compteDestColumn != null) {
                compteDestColumn.setText(languageManager.getTranslation("dashboard.table.destination"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des traductions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupFullscreenToggle() {
        // Ajouter le double-clic sur la scène pour activer/désactiver le mode plein écran
        if (userLabel != null && userLabel.getScene() != null) {
            userLabel.getScene().setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Stage stage = (Stage) userLabel.getScene().getWindow();
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
        }
    }
    
    private void setupTableColumns() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDate().format(formatter)
            ));
        
        // CellFactory pour afficher le texte complet
        dateColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                }
            }
        });
        
        typeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getType().toString()
            ));
        
        typeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                }
            }
        });
        
        montantColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getMontant().toString() + " MAD"
            ));
        
        montantColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px; -fx-font-weight: 500;");
                }
            }
        });
        
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        
        libelleColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                }
            }
        });
        
        compteSourceColumn.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                t.getCompteSource() != null ? t.getCompteSource().getNumeroCompte() : "-"
            );
        });
        
        compteSourceColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                }
            }
        });
        
        compteDestColumn.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                t.getCompteDest() != null ? t.getCompteDest().getNumeroCompte() : "-"
            );
        });
        
        compteDestColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                }
            }
        });
        
        // Configurer la hauteur des lignes
        transactionTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Transaction> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-min-height: 40px; -fx-pref-height: 40px;");
            return row;
        });
        
        transactionTable.setItems(transactions);
    }
    
    private void initializeData() {
        if (authService == null) return;
        
        // Afficher l'utilisateur connecté (sera traduit dans updateTranslations)
        if (authService.getCurrentUser() != null) {
            updateTranslations(); // Mettre à jour avec les traductions
        }
        
        // Actualiser les statistiques
        refreshStatistics();
        refreshTransactions();
        updateMessagesBadge();
    }
    
    private void refreshStatistics() {
        BigDecimal soldeTotal = statistiqueService.calculerSoldeTotalBanque();
        BigDecimal soldeMoyen = statistiqueService.calculerSoldeMoyen();
        int nombreComptes = new CompteDAO().findAll().size();
        
        soldeTotalLabel.setText(String.format("%.2f MAD", soldeTotal));
        soldeMoyenLabel.setText(String.format("%.2f MAD", soldeMoyen));
        nombreComptesLabel.setText(String.valueOf(nombreComptes));
    }
    
    private void refreshTransactions() {
        transactions.clear();
        transactions.addAll(new TransactionDAO().findAll());
    }
    
    @FXML
    private void handleRefresh() {
        refreshStatistics();
        refreshTransactions();
        updateMessagesBadge();
    }
    
    private void updateMessagesBadge() {
        if (messagesBadge == null) return;
        
        try {
            int nonLus = notificationService.compterMessagesNonLusClients();
            if (nonLus > 0) {
                messagesBadge.setText(String.valueOf(nonLus));
                messagesBadge.setVisible(true);
            } else {
                messagesBadge.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            messagesBadge.setVisible(false);
        }
    }
    
    @FXML
    private void handleMessages() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/MessagesAdminView.fxml"));
            Stage messagesStage = new Stage();
            Scene messagesScene = new Scene(loader.load());
            messagesStage.setScene(messagesScene);
            messagesStage.setTitle(languageManager != null ? languageManager.getTranslation("dashboard.admin.messages") : "Messages des clients");
            messagesStage.setResizable(true);
            messagesStage.setWidth(1000);
            messagesStage.setHeight(700);
            
            // Ajouter le double-clic pour le mode plein écran
            messagesScene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    messagesStage.setFullScreen(!messagesStage.isFullScreen());
                }
            });
            
            MessagesAdminController controller = loader.getController();
            controller.setAuthService(authService);
            
            messagesStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                languageManager != null ? languageManager.getTranslation("messages.admin.error") : "Impossible d'ouvrir la fenêtre des messages", 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleBlockedAccounts() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/BlockedAccountsView.fxml"));
            Stage blockedStage = new Stage();
            Scene blockedScene = new Scene(loader.load());
            blockedStage.setScene(blockedScene);
            blockedStage.setTitle(languageManager != null ? languageManager.getTranslation("dashboard.admin.blocked") : "Gestion des comptes bloqués");
            blockedStage.setResizable(true);
            blockedStage.setWidth(900);
            blockedStage.setHeight(600);
            
            // Ajouter le double-clic pour le mode plein écran
            blockedScene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    blockedStage.setFullScreen(!blockedStage.isFullScreen());
                }
            });
            
            BlockedAccountsController controller = loader.getController();
            controller.setAuthService(authService);
            
            blockedStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                languageManager != null ? languageManager.getTranslation("dashboard.admin.error.blocked") : "Impossible d'ouvrir la fenêtre de gestion des comptes bloqués", 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleActivation() {
        try {
            // Afficher d'abord les clients disponibles pour aider l'utilisateur
            try {
                List<com.banknet.model.Client> clients = new com.banknet.dao.ClientDAO().findAll();
                if (!clients.isEmpty()) {
                    String headerText = languageManager != null ? languageManager.getTranslation("dashboard.admin.available.clients") : "Clients disponibles";
                    StringBuilder message = new StringBuilder(headerText + " :\n\n");
                    for (com.banknet.model.Client c : clients) {
                        boolean hasAccount = c.getUserAccount() != null;
                        if (!hasAccount) {
                            message.append(String.format("✓ ID: %d | CIN: %s | Nom: %s %s\n",
                                c.getId(), c.getCin(), c.getPrenom(), c.getNom()));
                        }
                    }
                    showAlert(languageManager != null ? languageManager.getTranslation("alert.info") : "Information", 
                        message.toString(), Alert.AlertType.INFORMATION);
                }
            } catch (Exception e) {
                // Ignorer les erreurs de récupération des clients
            }
            
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/ActivationView.fxml"));
            Stage activationStage = new Stage();
            Scene activationScene = new Scene(loader.load());
            activationStage.setScene(activationScene);
            activationStage.setTitle(languageManager != null ? 
                (languageManager.getCurrentLocale().getLanguage().equals("ar") ? 
                 "تفعيل الحساب" : 
                 (languageManager.getCurrentLocale().getLanguage().equals("en") ? 
                  "Account Activation" : "Activation de compte")) : 
                "Activation de compte");
            activationStage.setResizable(true);
            
            // Ajouter le double-clic pour le mode plein écran
            activationScene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    activationStage.setFullScreen(!activationStage.isFullScreen());
                }
            });
            
            ActivationController controller = loader.getController();
            controller.setAuthService(authService);
            
            activationStage.showAndWait();
            
            // Rafraîchir après fermeture
            handleRefresh();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                languageManager != null ? languageManager.getTranslation("activation.error") : "Impossible d'ouvrir la fenêtre d'activation", 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleLogout() {
        authService.logout();
        
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/LoginView.fxml"));
            Scene loginScene = new Scene(loader.load());
            
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("BANKNET - Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        // Utiliser les traductions si languageManager est disponible
        String translatedTitle = title;
        if (languageManager != null) {
            // Essayer de traduire le titre s'il s'agit d'une clé de traduction
            if (title.equals("Erreur")) {
                translatedTitle = languageManager.getTranslation("alert.error");
            } else if (title.equals("Succès")) {
                translatedTitle = languageManager.getTranslation("alert.success");
            } else if (title.equals("Avertissement")) {
                translatedTitle = languageManager.getTranslation("alert.warning");
            } else if (title.equals("Information") || title.equals("Clients disponibles")) {
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
