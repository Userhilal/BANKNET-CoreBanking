package com.banknet;

import com.banknet.util.DatabaseInitializer;
import com.banknet.util.HibernateUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    
    @Override
    public void init() {
        // Attendre que la base de données Docker soit prête
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.waitForDatabase(10, 2000); // 10 tentatives, 2 secondes entre chaque
        
        // Vérifier la connexion à la base de données
        if (!HibernateUtil.isInitialized()) {
            System.err.println("ATTENTION: Impossible de se connecter à la base de données MySQL.");
            System.err.println("Vérifiez que le conteneur Docker est démarré avec : docker-compose up -d");
            System.err.println("Ou démarrez-le manuellement avec : docker-compose up");
            return;
        }
        
        // Initialiser la base de données avec des données de test si elle est vide
        try {
            initializer.initializeIfEmpty();
        } catch (Exception e) {
            System.err.println("ATTENTION: Erreur lors de l'initialisation de la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Afficher un message d'erreur si la base de données n'est pas disponible
            if (!HibernateUtil.isInitialized()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de connexion");
                    alert.setHeaderText("Impossible de se connecter à la base de données");
                    alert.setContentText(
                        "Le conteneur Docker MySQL n'est pas accessible.\n\n" +
                        "Actions à effectuer :\n" +
                        "1. Vérifiez que Docker est démarré\n" +
                        "2. Lancez le conteneur avec : docker-compose up -d\n" +
                        "3. Attendez quelques secondes que MySQL démarre\n" +
                        "4. Relancez l'application\n\n" +
                        "L'application va se fermer."
                    );
                    alert.showAndWait();
                    Platform.exit();
                });
                return;
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/LoginView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Appliquer le style CSS
            try {
                scene.getStylesheets().add(getClass().getResource("/com/banknet/view/style.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Warning: Impossible de charger le fichier CSS : " + e.getMessage());
            }
            
            primaryStage.setTitle("BANKNET - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            
            // Ajouter le double-clic pour activer/désactiver le mode plein écran
            scene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    primaryStage.setFullScreen(!primaryStage.isFullScreen());
                }
            });
            
            primaryStage.show();
            
            // Fermer Hibernate proprement à la fermeture de l'application
            primaryStage.setOnCloseRequest(e -> {
                HibernateUtil.shutdown();
                System.exit(0);
            });
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'interface : " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors du démarrage de l'application");
            alert.setContentText("Une erreur est survenue : " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
