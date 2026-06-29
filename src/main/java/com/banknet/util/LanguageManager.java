package com.banknet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Gestionnaire de langues pour l'internationalisation (i18n)
 */
public class LanguageManager {
    
    private static LanguageManager instance;
    private Locale currentLocale;
    private Properties translations;
    
    public enum Language {
        FRENCH("fr", "Français"),
        ARABIC("ar", "العربية"),
        ENGLISH("en", "English");
        
        private final String code;
        private final String displayName;
        
        Language(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static Language fromCode(String code) {
            for (Language lang : values()) {
                if (lang.code.equals(code)) {
                    return lang;
                }
            }
            return FRENCH; // Default
        }
    }
    
    private LanguageManager() {
        // Par défaut, on utilise le français
        currentLocale = Locale.FRENCH;
        loadTranslations();
    }
    
    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }
    
    public void setLanguage(Language language) {
        switch (language) {
            case FRENCH:
                currentLocale = Locale.FRENCH;
                break;
            case ARABIC:
                currentLocale = new Locale("ar", "MA"); // Arabe Maroc
                break;
            case ENGLISH:
                currentLocale = Locale.ENGLISH;
                break;
        }
        loadTranslations();
        
        // Définir la locale par défaut pour JavaFX (DatePicker, etc.)
        java.util.Locale.setDefault(currentLocale);
        
        // Définir la locale pour le thread actuel également
        try {
            // JavaFX utilise automatiquement la locale par défaut
            // Mais on s'assure qu'elle est bien définie pour ce thread
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        } catch (Exception e) {
            System.err.println("Note: Locale setting - " + e.getMessage());
        }
    }
    
    public Language getCurrentLanguage() {
        String langCode = currentLocale.getLanguage();
        return Language.fromCode(langCode);
    }
    
    private void loadTranslations() {
        translations = new Properties();
        String fileName = "/com/banknet/i18n/messages_" + currentLocale.getLanguage() + ".properties";
        
        try (InputStream inputStream = getClass().getResourceAsStream(fileName);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            if (inputStream != null) {
                translations.load(reader);
            } else {
                System.err.println("Fichier de traduction introuvable: " + fileName);
                // Charger le français par défaut
                if (!currentLocale.getLanguage().equals("fr")) {
                    currentLocale = Locale.FRENCH;
                    loadTranslations();
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des traductions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String getTranslation(String key) {
        String translation = translations.getProperty(key);
        if (translation == null) {
            System.err.println("Clé de traduction introuvable: " + key);
            return "[" + key + "]";
        }
        return translation;
    }
    
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    public boolean isRTL() {
        return "ar".equals(currentLocale.getLanguage());
    }
}

