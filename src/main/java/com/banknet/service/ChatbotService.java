package com.banknet.service;

import com.banknet.util.LanguageManager;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service du chatbot bancaire avec support multilingue
 */
public class ChatbotService {
    
    private LanguageManager languageManager;
    // On garde les patterns en français car ils sont insensibles à la casse et fonctionnent avec toutes les langues
    private final Map<Pattern, String> reponses;
    
    public ChatbotService() {
        this.languageManager = LanguageManager.getInstance();
        this.reponses = new HashMap<>();
        initialiserReponses();
    }
    
    private void initialiserReponses() {
        // Tous les patterns sont insensibles à la casse et acceptent différentes langues
        // Les réponses seront traduites selon la langue actuelle
        
        // ========== SALUTATIONS ==========
        Pattern salutationPattern = Pattern.compile("(bonjour|salut|hello|bonsoir|bonne.*journee|bonne.*soiree|hey|hi|مرحبا|أهلا|صباح|مساء|الخير)", Pattern.CASE_INSENSITIVE);
        reponses.put(salutationPattern, "chatbot.welcome");
        
        // ========== SOLDE ET COMPTES ==========
        Pattern soldeTotalPattern = Pattern.compile("(solde.*total|montant.*total|combien.*j.*ai.*total|الرصيد.*الإجمالي|total.*balance)", Pattern.CASE_INSENSITIVE);
        reponses.put(soldeTotalPattern, "chatbot.response.solde.total");
        
        Pattern soldeCourantPattern = Pattern.compile("(solde.*courant|compte.*courant|montant.*courant|الحساب.*الجاري|current.*account)", Pattern.CASE_INSENSITIVE);
        reponses.put(soldeCourantPattern, "chatbot.response.compte.courant");
        
        Pattern soldeEpargnePattern = Pattern.compile("(solde.*epargne|compte.*epargne|montant.*epargne|حساب.*التوفير|savings.*account)", Pattern.CASE_INSENSITIVE);
        reponses.put(soldeEpargnePattern, "chatbot.response.compte.epargne");
        
        Pattern soldePattern = Pattern.compile("(solde|montant|combien.*argent|mon.*solde|solde.*actuel|avoir|الرصيد|balance|amount)", Pattern.CASE_INSENSITIVE);
        reponses.put(soldePattern, "chatbot.response.solde");
        
        // ========== VIREMENTS ==========
        Pattern virementInternePattern = Pattern.compile("(virement.*interne|transfert.*interne|virer.*entre.*compte|transférer.*mes.*comptes|تحويل.*داخلي|internal.*transfer)", Pattern.CASE_INSENSITIVE);
        reponses.put(virementInternePattern, "chatbot.response.virement.interne");
        
        Pattern virementTiersPattern = Pattern.compile("(virement.*tiers|virement.*externe|envoyer.*autre.*personne|transférer.*client|virer.*tiers|تحويل.*لطرف.*ثالث|transfer.*to.*third)", Pattern.CASE_INSENSITIVE);
        reponses.put(virementTiersPattern, "chatbot.response.virement.tiers");
        
        Pattern virementPattern = Pattern.compile("(virement|transfert|envoyer.*argent|transférer|virer|envoyer|تحويل|transfer|send.*money)", Pattern.CASE_INSENSITIVE);
        reponses.put(virementPattern, "chatbot.response.virement");
        
        Pattern commentVirerPattern = Pattern.compile("(comment.*virer|comment.*transférer|faire.*virement|effectuer.*virement|كيف.*تحويل|how.*transfer)", Pattern.CASE_INSENSITIVE);
        reponses.put(commentVirerPattern, "chatbot.response.comment.virer");
        
        // ========== GRAPHIQUE ==========
        Pattern graphiquePattern = Pattern.compile("(graphique|evolution|evolution.*finance|graphique.*finance|graph|chart|رسم.*بياني|graph)", Pattern.CASE_INSENSITIVE);
        reponses.put(graphiquePattern, "chatbot.response.graphique");
        
        // ========== HISTORIQUE ==========
        Pattern historiquePattern = Pattern.compile("(historique|transaction|operation|releve|liste.*transaction|mes.*transactions|السجل|history|transactions)", Pattern.CASE_INSENSITIVE);
        reponses.put(historiquePattern, "chatbot.response.historique");
        
        Pattern filtrerPattern = Pattern.compile("(filtrer.*transaction|filtrer.*historique|filtrer.*par.*date|filtrer.*par.*montant|تصفية|filter)", Pattern.CASE_INSENSITIVE);
        reponses.put(filtrerPattern, "chatbot.response.filtrer");
        
        Pattern exporterPattern = Pattern.compile("(exporter.*csv|telecharger.*releve|exporter.*transaction|csv|تصدير|export)", Pattern.CASE_INSENSITIVE);
        reponses.put(exporterPattern, "chatbot.response.exporter");
        
        // ========== OBJECTIFS ==========
        Pattern objectifPattern = Pattern.compile("(objectif.*epargne|creer.*objectif|nouvel.*objectif|definir.*objectif|ajouter.*objectif|هدف.*التوفير|savings.*goal)", Pattern.CASE_INSENSITIVE);
        reponses.put(objectifPattern, "chatbot.response.objectif");
        
        // ========== MESSAGERIE ==========
        Pattern messageriePattern = Pattern.compile("(messagerie|message.*banque|contacter.*banque|ecrire.*banque|envoyer.*message|رسائل|messaging)", Pattern.CASE_INSENSITIVE);
        reponses.put(messageriePattern, "chatbot.response.messagerie");
        
        // ========== NOTIFICATIONS ==========
        Pattern notificationPattern = Pattern.compile("(notification|notifications|alerte|centre.*notification|إشعار|notification)", Pattern.CASE_INSENSITIVE);
        reponses.put(notificationPattern, "chatbot.response.notification");
        
        // ========== MOT DE PASSE ==========
        Pattern passwordPattern = Pattern.compile("(changer.*mot.*passe|modifier.*mot.*passe|mettre.*jour.*mot.*passe|تغيير.*كلمة.*المرور|change.*password)", Pattern.CASE_INSENSITIVE);
        reponses.put(passwordPattern, "chatbot.response.password");
        
        // ========== CONTACT ==========
        Pattern contactPattern = Pattern.compile("(contact|aide|support|assistance|telephone|email|contacter|appeler|aide.*client|اتصال|contact)", Pattern.CASE_INSENSITIVE);
        reponses.put(contactPattern, "chatbot.response.contact");
        
        // ========== MERCI ==========
        Pattern merciPattern = Pattern.compile("(merci|remerciement|thank.*you|thanks|شكرا|شكراً)", Pattern.CASE_INSENSITIVE);
        reponses.put(merciPattern, "chatbot.response.merci");
        
        Pattern aurevoirPattern = Pattern.compile("(aurevoir|au.*revoir|bye|a.*bientot|a.*plus|goodbye|see.*you|مع.*السلامة|bye)", Pattern.CASE_INSENSITIVE);
        reponses.put(aurevoirPattern, "chatbot.response.aurevoir");
    }
    
    public String obtenirReponse(String question) {
        if (question == null || question.trim().isEmpty()) {
            return languageManager.getTranslation("chatbot.welcome");
        }
        
        question = question.trim();
        
        // Chercher un pattern qui correspond
        for (Map.Entry<Pattern, String> entry : reponses.entrySet()) {
            if (entry.getKey().matcher(question).find()) {
                String translationKey = entry.getValue();
                // Essayer de charger la traduction
                String translation = languageManager.getTranslation(translationKey);
                // Si la clé n'existe pas, retourner un message par défaut
                if (translation.startsWith("[") && translation.endsWith("]")) {
                    return languageManager.getTranslation("chatbot.default.response");
                }
                return translation;
            }
        }
        
        // Aucune correspondance trouvée
        return languageManager.getTranslation("chatbot.default.response");
    }
}
