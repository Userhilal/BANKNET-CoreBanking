package com.banknet.controller;

import com.banknet.MainApp;
import com.banknet.model.Role;
import com.banknet.model.UserAccount;
import com.banknet.service.AuthService;
import com.banknet.util.HibernateUtil;
import com.banknet.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Properties;

public class LoginController {
    
    @FXML
    private TextField loginField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private TextField passwordVisibleField; // Champ texte pour afficher le mot de passe
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label usernameLabel;
    
    @FXML
    private Label passwordLabel;
    
    @FXML
    private Label rememberMeLabel;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button togglePasswordButton;
    
    @FXML
    private CheckBox rememberMeCheckbox;
    
    @FXML
    private Button menuButton;
    
    @FXML
    private ContextMenu menuContextMenu;
    
    @FXML
    private MenuItem languageMenuItem;
    
    @FXML
    private MenuItem exchangeMenuItem;
    
    @FXML
    private MenuItem contactMenuItem;
    
    private AuthService authService;
    private LanguageManager languageManager;
    private boolean isPasswordVisible = false;
    
    @FXML
    public void initialize() {
        authService = new AuthService();
        languageManager = LanguageManager.getInstance();
        
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        // Initialiser le champ de mot de passe visible (caché par défaut)
        if (passwordVisibleField != null) {
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        }
        
        // Créer et configurer le menu contextuel
        menuContextMenu = new ContextMenu();
        languageMenuItem = new MenuItem();
        exchangeMenuItem = new MenuItem();
        contactMenuItem = new MenuItem();
        
        languageMenuItem.setOnAction(e -> handleLanguageMenu());
        exchangeMenuItem.setOnAction(e -> handleExchangeRates());
        contactMenuItem.setOnAction(e -> handleContact());
        
        menuContextMenu.getItems().addAll(languageMenuItem, exchangeMenuItem, contactMenuItem);
        menuContextMenu.getStyleClass().add("context-menu");
        
        if (menuButton != null) {
            menuButton.setContextMenu(menuContextMenu);
        }
        
        // Charger les traductions
        updateTranslations();
        
        // Configurer le double-clic pour le mode plein écran
        setupFullscreenToggle();
        
        // Vérifier si la base de données est disponible
        if (!HibernateUtil.isInitialized()) {
            showDatabaseWarning();
        }
    }
    
    private void setupFullscreenToggle() {
        // Ajouter le double-clic sur la scène pour activer/désactiver le mode plein écran
        if (loginButton != null && loginButton.getScene() != null) {
            loginButton.getScene().setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
        }
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        try {
            // Mettre à jour tous les éléments de l'interface
            if (titleLabel != null) {
                titleLabel.setText(languageManager.getTranslation("login.title"));
            }
            if (welcomeLabel != null) {
                welcomeLabel.setText(languageManager.getTranslation("login.welcome"));
            }
            if (usernameLabel != null) {
                usernameLabel.setText(languageManager.getTranslation("login.username"));
            }
            if (passwordLabel != null) {
                passwordLabel.setText(languageManager.getTranslation("login.password"));
            }
            if (loginField != null) {
                loginField.setPromptText(languageManager.getTranslation("login.username.placeholder"));
            }
            if (passwordField != null) {
                passwordField.setPromptText(languageManager.getTranslation("login.password.placeholder"));
            }
            if (passwordVisibleField != null) {
                passwordVisibleField.setPromptText(languageManager.getTranslation("login.password.placeholder"));
            }
            if (loginButton != null) {
                loginButton.setText(languageManager.getTranslation("login.button"));
            }
            if (rememberMeLabel != null) {
                rememberMeLabel.setText(languageManager.getTranslation("login.remember"));
            }
            if (languageMenuItem != null) {
                languageMenuItem.setText(languageManager.getTranslation("menu.language"));
            }
            if (exchangeMenuItem != null) {
                exchangeMenuItem.setText(languageManager.getTranslation("menu.exchange"));
            }
            if (contactMenuItem != null) {
                contactMenuItem.setText(languageManager.getTranslation("menu.contact"));
            }
            
            // Mettre à jour le tooltip du bouton toggle password
            if (togglePasswordButton != null) {
                if (isPasswordVisible) {
                    togglePasswordButton.setTooltip(new Tooltip(languageManager.getTranslation("login.hidePassword")));
                } else {
                    togglePasswordButton.setTooltip(new Tooltip(languageManager.getTranslation("login.showPassword")));
                }
            }
            
            // Mettre à jour le titre de la fenêtre
            Stage stage = (Stage) (loginField != null ? loginField.getScene() : 
                                  (menuButton != null ? menuButton.getScene() : null)).getWindow();
            if (stage != null) {
                stage.setTitle(languageManager.getTranslation("login.title") + " - " + languageManager.getTranslation("login.welcome"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des traductions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleTogglePassword() {
        isPasswordVisible = !isPasswordVisible;
        
        if (isPasswordVisible) {
            // Afficher le texte en clair
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.requestFocus();
            togglePasswordButton.setText("🙈");
            togglePasswordButton.setTooltip(new Tooltip(languageManager.getTranslation("login.hidePassword")));
        } else {
            // Masquer le texte
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.requestFocus();
            togglePasswordButton.setText("👁");
            togglePasswordButton.setTooltip(new Tooltip(languageManager.getTranslation("login.showPassword")));
        }
    }
    
    @FXML
    private void handleMenuToggle() {
        if (menuContextMenu != null && menuButton != null) {
            menuContextMenu.show(menuButton, menuButton.getScene().getWindow().getX() + menuButton.getLayoutX(), 
                                menuButton.getScene().getWindow().getY() + menuButton.getLayoutY() + menuButton.getHeight());
        }
    }
    
    @FXML
    private void handleLanguageMenu() {
        // Créer un dialogue personnalisé pour choisir la langue
        Dialog<LanguageManager.Language> dialog = new Dialog<>();
        dialog.setTitle(languageManager.getTranslation("menu.language"));
        dialog.setHeaderText(null);
        
        // Créer les boutons de langue
        VBox languageBox = new VBox(10);
        languageBox.setPadding(new Insets(20));
        languageBox.setStyle("-fx-background-color: white;");
        
        Button frenchButton = new Button(LanguageManager.Language.FRENCH.getDisplayName());
        frenchButton.setStyle("-fx-min-width: 200px; -fx-pref-height: 40px; -fx-font-size: 14px;");
        frenchButton.setOnAction(e -> {
            dialog.setResult(LanguageManager.Language.FRENCH);
            dialog.close();
        });
        
        Button arabicButton = new Button(LanguageManager.Language.ARABIC.getDisplayName());
        arabicButton.setStyle("-fx-min-width: 200px; -fx-pref-height: 40px; -fx-font-size: 14px;");
        arabicButton.setOnAction(e -> {
            dialog.setResult(LanguageManager.Language.ARABIC);
            dialog.close();
        });
        
        Button englishButton = new Button(LanguageManager.Language.ENGLISH.getDisplayName());
        englishButton.setStyle("-fx-min-width: 200px; -fx-pref-height: 40px; -fx-font-size: 14px;");
        englishButton.setOnAction(e -> {
            dialog.setResult(LanguageManager.Language.ENGLISH);
            dialog.close();
        });
        
        languageBox.getChildren().addAll(frenchButton, arabicButton, englishButton);
        dialog.getDialogPane().setContent(languageBox);
        
        // Ajouter un bouton fermer (traduit)
        ButtonType closeButtonType = new ButtonType(languageManager.getTranslation("contact.close"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        
        // Positionner le dialogue par rapport à la fenêtre principale
        if (menuButton != null && menuButton.getScene() != null) {
            Stage ownerStage = (Stage) menuButton.getScene().getWindow();
            dialog.initOwner(ownerStage);
        }
        
        // Gérer le résultat
        dialog.showAndWait().ifPresent(language -> {
            if (language != null) {
                // Changer la langue
                languageManager.setLanguage(language);
                
                // La méthode setLanguage() définit déjà la locale système pour JavaFX
                
                // Mettre à jour immédiatement toutes les traductions de la page de connexion
                updateTranslations();
                
                // Sauvegarder la préférence (optionnel, dans les propriétés système)
                System.setProperty("banknet.language", language.getCode());
                
                // Affichage de confirmation (optionnel)
                System.out.println("Langue changée vers: " + language.getDisplayName());
                System.out.println("Locale définie: " + java.util.Locale.getDefault());
            }
        });
    }
    
    @FXML
    private void handleExchangeRates() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/ExchangeRatesView.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage exchangeStage = new Stage();
            exchangeStage.setTitle(languageManager.getTranslation("exchange.title"));
            exchangeStage.setScene(scene);
            exchangeStage.setResizable(false);
            exchangeStage.initOwner(menuButton.getScene().getWindow());
            exchangeStage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement du cours de change: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleContact() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/ContactView.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage contactStage = new Stage();
            contactStage.setTitle(languageManager.getTranslation("contact.title"));
            contactStage.setScene(scene);
            contactStage.setResizable(false);
            contactStage.initOwner(menuButton.getScene().getWindow());
            contactStage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de contact: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showDatabaseWarning() {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement - Base de données");
            alert.setHeaderText("Connexion à la base de données impossible");
            alert.setContentText(
                "L'application ne peut pas se connecter à MySQL.\n\n" +
                "Vérifiez que :\n" +
                "1. MySQL est démarré\n" +
                "2. La base 'banknet_db' existe\n" +
                "3. Les identifiants dans hibernate.cfg.xml sont corrects\n\n" +
                "L'application va démarrer mais les fonctionnalités de base de données seront indisponibles."
            );
            alert.showAndWait();
        } catch (Exception e) {
            // Ignorer si JavaFX n'est pas encore complètement initialisé
            System.err.println("Warning: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogin() {
        String login = loginField.getText().trim();
        String password = isPasswordVisible ? passwordVisibleField.getText() : passwordField.getText();
        
        if (login.isEmpty() || password.isEmpty()) {
            showError(languageManager.getTranslation("login.error.empty"));
            return;
        }
        
        // Gérer "Se souvenir de moi" (pour l'instant, juste logique, pas de stockage persistant)
        boolean rememberMe = rememberMeCheckbox.isSelected();
        if (rememberMe) {
            // TODO: Sauvegarder les préférences utilisateur si nécessaire
            System.out.println("Se souvenir de moi activé pour: " + login);
        }
        
        try {
            // Vérifier si le compte est bloqué avant de tenter la connexion
            if (authService.isAccountBlocked(login)) {
                showError(languageManager.getTranslation("login.error.account.blocked"));
                return;
            }
            
            boolean loginSuccess = authService.login(login, password);
            if (loginSuccess) {
                try {
                    UserAccount currentUser = authService.getCurrentUser();
                    FXMLLoader loader;
                    Scene dashboardScene;
                    Stage stage = (Stage) loginField.getScene().getWindow();
                    
                    // Rediriger selon le rôle
                    if (currentUser.getRole() == Role.CLIENT) {
                        // Dashboard pour les clients
                        java.net.URL resource = MainApp.class.getResource("/com/banknet/view/DashboardClientView.fxml");
                        if (resource == null) {
                            throw new IOException("Le fichier DashboardClientView.fxml est introuvable");
                        }
                        loader = new FXMLLoader(resource);
                        dashboardScene = new Scene(loader.load());
                        
                        DashboardClientController controller = loader.getController();
                        if (controller == null) {
                            throw new IOException("Le contrôleur DashboardClientController n'a pas pu être chargé");
                        }
                        controller.setAuthService(authService);
                        
                        stage.setTitle("BANKNET - " + languageManager.getTranslation("dashboard.title"));
                    } else {
                        // Dashboard pour Admin/Agent
                        java.net.URL resource = MainApp.class.getResource("/com/banknet/view/DashboardView.fxml");
                        if (resource == null) {
                            throw new IOException("Le fichier DashboardView.fxml est introuvable");
                        }
                        loader = new FXMLLoader(resource);
                        dashboardScene = new Scene(loader.load());
                        
                        DashboardController controller = loader.getController();
                        if (controller == null) {
                            throw new IOException("Le contrôleur DashboardController n'a pas pu être chargé");
                        }
                        controller.setAuthService(authService);
                        
                        stage.setTitle("BANKNET - Tableau de bord Admin");
                    }
                    
                    stage.setScene(dashboardScene);
                    stage.centerOnScreen();
                    stage.setResizable(true);
                    // Définir une taille par défaut appropriée pour le dashboard client
                    if (currentUser.getRole() == Role.CLIENT) {
                        stage.setWidth(1400);
                        stage.setHeight(650);
                        stage.setMinWidth(1300);
                        stage.setMinHeight(600);
                        stage.setMaxWidth(1920);
                        stage.setMaxHeight(800);
                    } else {
                        stage.setWidth(1400);
                        stage.setHeight(650);
                        stage.setMinWidth(1200);
                        stage.setMinHeight(600);
                        stage.setMaxWidth(1920);
                        stage.setMaxHeight(800);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("=== ERREUR IO ===");
                    System.err.println("Message: " + e.getMessage());
                    System.err.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Aucune"));
                    if (e.getCause() != null) {
                        e.getCause().printStackTrace();
                    }
                    String errorMsg = e.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "Erreur inconnue lors du chargement";
                    }
                    showError("Erreur lors du chargement du tableau de bord : " + errorMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("=== ERREUR INATTENDUE ===");
                    System.err.println("Type: " + e.getClass().getName());
                    System.err.println("Message: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Cause: " + e.getCause().getMessage());
                        e.getCause().printStackTrace();
                    }
                    showError("Erreur inattendue : " + e.getClass().getSimpleName() + " - " + 
                        (e.getMessage() != null ? e.getMessage() : "Erreur inconnue"));
                }
            } else {
                showError(languageManager.getTranslation("login.error.invalid"));
                if (isPasswordVisible) {
                    passwordVisibleField.clear();
                } else {
                    passwordField.clear();
                }
            }
        } catch (IllegalStateException e) {
            showError("La base de données n'est pas disponible. Vérifiez votre connexion MySQL.");
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
