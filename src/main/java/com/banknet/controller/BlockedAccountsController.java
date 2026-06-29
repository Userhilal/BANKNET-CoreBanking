package com.banknet.controller;

import com.banknet.dao.UserAccountDAO;
import com.banknet.model.AccountStatus;
import com.banknet.model.Role;
import com.banknet.model.UserAccount;
import com.banknet.service.AuthService;
import com.banknet.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class BlockedAccountsController {
    
    @FXML
    private TableView<UserAccount> blockedAccountsTable;
    
    @FXML
    private TableColumn<UserAccount, Long> idColumn;
    
    @FXML
    private TableColumn<UserAccount, String> loginColumn;
    
    @FXML
    private TableColumn<UserAccount, String> roleColumn;
    
    @FXML
    private TableColumn<UserAccount, String> clientColumn;
    
    @FXML
    private TableColumn<UserAccount, Integer> failedAttemptsColumn;
    
    @FXML
    private TableColumn<UserAccount, String> statusColumn;
    
    @FXML
    private TableColumn<UserAccount, String> actionsColumn;
    
    @FXML
    private Button unblockButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label countLabel;
    
    @FXML
    private Label headerLabel;
    
    @FXML
    private Label subtitleLabel;
    
    private AuthService authService;
    private UserAccountDAO userAccountDAO;
    private LanguageManager languageManager;
    private ObservableList<UserAccount> blockedAccounts;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.userAccountDAO = new UserAccountDAO();
        this.languageManager = LanguageManager.getInstance();
        initializeData();
        javafx.application.Platform.runLater(() -> updateTranslations());
    }
    
    @FXML
    public void initialize() {
        blockedAccounts = FXCollections.observableArrayList();
        setupTableColumns();
        setupFullscreenToggle();
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        // Désactiver le bouton de déblocage si aucun compte n'est sélectionné
        unblockButton.setDisable(true);
        blockedAccountsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            unblockButton.setDisable(newSelection == null);
        });
        
        updateTranslations();
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        if (headerLabel != null) {
            headerLabel.setText("🔒 " + languageManager.getTranslation("blocked.title"));
        }
        if (subtitleLabel != null) {
            subtitleLabel.setText(languageManager.getTranslation("blocked.subtitle"));
        }
        if (idColumn != null) {
            idColumn.setText(languageManager.getTranslation("blocked.table.id"));
        }
        if (loginColumn != null) {
            loginColumn.setText(languageManager.getTranslation("blocked.table.login"));
        }
        if (roleColumn != null) {
            roleColumn.setText(languageManager.getTranslation("blocked.table.role"));
        }
        if (clientColumn != null) {
            clientColumn.setText(languageManager.getTranslation("blocked.table.client"));
        }
        if (failedAttemptsColumn != null) {
            failedAttemptsColumn.setText(languageManager.getTranslation("blocked.table.failed.attempts"));
        }
        if (statusColumn != null) {
            statusColumn.setText(languageManager.getTranslation("blocked.table.status"));
        }
        if (actionsColumn != null) {
            actionsColumn.setText(languageManager.getTranslation("blocked.table.actions"));
        }
        if (unblockButton != null) {
            unblockButton.setText(languageManager.getTranslation("blocked.unblock"));
        }
        if (refreshButton != null) {
            refreshButton.setText("🔄 " + languageManager.getTranslation("messagerie.refresh"));
        }
        
        // Mettre à jour le titre de la fenêtre
        if (headerLabel != null && headerLabel.getScene() != null) {
            Stage stage = (Stage) headerLabel.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(languageManager.getTranslation("blocked.title"));
            }
        }
    }
    
    private void setupFullscreenToggle() {
        // Ajouter le double-clic sur la scène pour activer/désactiver le mode plein écran
        if (blockedAccountsTable != null && blockedAccountsTable.getScene() != null) {
            blockedAccountsTable.getScene().setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Stage stage = (Stage) blockedAccountsTable.getScene().getWindow();
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
        }
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setCellFactory(column -> new TableCell<UserAccount, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                }
            }
        });
        
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        loginColumn.setCellFactory(column -> new TableCell<UserAccount, String>() {
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
        
        roleColumn.setCellValueFactory(cellData -> {
            UserAccount user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                user.getRole() != null ? user.getRole().toString() : "-"
            );
        });
        roleColumn.setCellFactory(column -> new TableCell<UserAccount, String>() {
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
        
        clientColumn.setCellValueFactory(cellData -> {
            UserAccount user = cellData.getValue();
            if (user.getClient() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    user.getClient().getPrenom() + " " + user.getClient().getNom()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        clientColumn.setCellFactory(column -> new TableCell<UserAccount, String>() {
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
        
        failedAttemptsColumn.setCellValueFactory(new PropertyValueFactory<>("failedLoginAttempts"));
        failedAttemptsColumn.setCellFactory(column -> new TableCell<UserAccount, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                }
            }
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            UserAccount user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                user.getStatus() != null ? user.getStatus().toString() : "-"
            );
        });
        
        // Colonne Actions avec badge de statut
        statusColumn.setCellFactory(column -> new TableCell<UserAccount, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (AccountStatus.BLOQUE.toString().equals(item)) {
                        setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 600; -fx-font-size: 14px; -fx-padding: 12px 15px;");
                    } else {
                        setStyle("-fx-text-fill: #059669; -fx-font-weight: 600; -fx-font-size: 14px; -fx-padding: 12px 15px;");
                    }
                }
            }
        });
        
        actionsColumn.setCellValueFactory(cellData -> {
            String text = languageManager != null ? 
                languageManager.getTranslation("blocked.action.unblock") : "Débloquer";
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        actionsColumn.setCellFactory(column -> new TableCell<UserAccount, String>() {
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
        blockedAccountsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<UserAccount> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-min-height: 40px; -fx-pref-height: 40px;");
            return row;
        });
        
        blockedAccountsTable.setItems(blockedAccounts);
    }
    
    private void initializeData() {
        refreshBlockedAccounts();
    }
    
    private void refreshBlockedAccounts() {
        blockedAccounts.clear();
        List<UserAccount> blocked = userAccountDAO.findBlockedAccounts();
        blockedAccounts.addAll(blocked);
        
        if (countLabel != null && languageManager != null) {
            countLabel.setText(blocked.size() + " " + languageManager.getTranslation("blocked.count"));
        }
    }
    
    @FXML
    private void handleUnblock() {
        UserAccount selected = blockedAccountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                languageManager != null ? languageManager.getTranslation("blocked.select") : "Veuillez sélectionner un compte à débloquer", 
                Alert.AlertType.WARNING);
            return;
        }
        
        // Confirmer le déblocage
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(languageManager != null ? languageManager.getTranslation("alert.confirmation") : "Confirmation de déblocage");
        confirmAlert.setHeaderText(languageManager != null ? languageManager.getTranslation("blocked.unblock") : "Débloquer le compte ?");
        confirmAlert.setContentText(String.format(languageManager != null ? languageManager.getTranslation("blocked.unblock.confirm") : "Êtes-vous sûr de vouloir débloquer le compte : %s ?", selected.getLogin()));
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = authService.unblockAccount(selected.getId());
                if (success) {
                    showAlert(languageManager != null ? languageManager.getTranslation("alert.success") : "Succès", 
                        String.format(languageManager != null ? languageManager.getTranslation("blocked.unblock.success") : "Le compte %s a été débloqué avec succès", selected.getLogin()), 
                        Alert.AlertType.INFORMATION);
                    refreshBlockedAccounts();
                } else {
                    showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                        languageManager != null ? languageManager.getTranslation("blocked.unblock.error") : "Impossible de débloquer le compte. Il est peut-être déjà débloqué.", 
                        Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        refreshBlockedAccounts();
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
            } else if (title.equals("Confirmation de déblocage")) {
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

