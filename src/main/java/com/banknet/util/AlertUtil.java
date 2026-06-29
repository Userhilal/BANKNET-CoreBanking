package com.banknet.util;

import javafx.scene.control.Alert;

/**
 * Utilitaire pour afficher des alertes traduites
 */
public class AlertUtil {
    
    private static LanguageManager languageManager = LanguageManager.getInstance();
    
    /**
     * Affiche une alerte traduite
     */
    public static void showTranslatedAlert(String titleKey, String messageKey, Alert.AlertType type) {
        String title = languageManager.getTranslation(titleKey);
        String message = languageManager.getTranslation(messageKey);
        showAlert(title, message, type);
    }
    
    /**
     * Affiche une alerte traduite avec un message formaté
     */
    public static void showTranslatedAlert(String titleKey, String messageKey, Alert.AlertType type, Object... args) {
        String title = languageManager.getTranslation(titleKey);
        String message = String.format(languageManager.getTranslation(messageKey), args);
        showAlert(title, message, type);
    }
    
    /**
     * Affiche une alerte avec titre traduit et message personnalisé
     */
    public static void showAlertWithTranslatedTitle(String titleKey, String message, Alert.AlertType type) {
        String title = languageManager.getTranslation(titleKey);
        showAlert(title, message, type);
    }
    
    /**
     * Affiche une alerte simple
     */
    private static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}




