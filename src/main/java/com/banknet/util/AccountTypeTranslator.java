package com.banknet.util;

import com.banknet.model.TypeCompte;

/**
 * Utilitaire pour traduire les types de comptes
 */
public class AccountTypeTranslator {
    
    /**
     * Traduit un TypeCompte en utilisant LanguageManager
     */
    public static String translate(TypeCompte type, LanguageManager languageManager) {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        switch (type) {
            case COURANT:
                return languageManager.getTranslation("account.type.current.short");
            case EPARGNE:
                return languageManager.getTranslation("account.type.savings.short");
            default:
                return type.toString();
        }
    }
    
    /**
     * Traduit un TypeCompte en utilisant le nom complet
     */
    public static String translateFull(TypeCompte type, LanguageManager languageManager) {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        switch (type) {
            case COURANT:
                return languageManager.getTranslation("account.type.current");
            case EPARGNE:
                return languageManager.getTranslation("account.type.savings");
            default:
                return type.toString();
        }
    }
}




