package com.banknet.util;

import com.banknet.model.TypeTransaction;

/**
 * Utility class to translate transaction types
 */
public class TransactionTypeTranslator {
    
    /**
     * Translates a TypeTransaction enum value using LanguageManager
     * @param type The transaction type to translate
     * @param languageManager The language manager instance
     * @return The translated string
     */
    public static String translate(TypeTransaction type, LanguageManager languageManager) {
        if (type == null || languageManager == null) {
            return type != null ? type.toString() : "";
        }
        
        String key;
        switch (type) {
            case DEBIT:
                key = "dashboard.table.debit";
                break;
            case CREDIT:
                key = "dashboard.table.credit";
                break;
            default:
                return type.toString();
        }
        
        return languageManager.getTranslation(key);
    }
}


