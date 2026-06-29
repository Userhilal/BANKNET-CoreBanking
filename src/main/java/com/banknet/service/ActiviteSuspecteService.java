package com.banknet.service;

import com.banknet.dao.ActiviteSuspecteDAO;
import com.banknet.dao.TransactionDAO;
import com.banknet.model.ActiviteSuspecte;
import com.banknet.model.Client;
import com.banknet.model.Compte;
import com.banknet.model.Transaction;
import com.banknet.model.ActiviteSuspecte.TypeActiviteSuspecte;
import com.banknet.model.ActiviteSuspecte.StatutActivite;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

//analyse les transactions et détecte les activités suspectes
public class ActiviteSuspecteService {
    //Définition de seuils constants pour détecter les transactions suspectes :
    private static final BigDecimal SEUIL_MONTANT_ELEVE = new BigDecimal("50000"); //Montant élevé
    private static final int SEUIL_FREQUENCE = 5; // 5 transactions en 1 heure Fréquence élevée

    private final ActiviteSuspecteDAO activiteDAO;
    private final TransactionDAO transactionDAO;
    private final NotificationService notificationService;
    
    public ActiviteSuspecteService() {
        this.activiteDAO = new ActiviteSuspecteDAO();
        this.transactionDAO = new TransactionDAO();
        this.notificationService = new NotificationService();
    }
    
    public void analyserTransaction(Transaction transaction, Compte compte) {
        Client client = compte.getClient();
        if (client == null) return;
        // 1. Vérifier montant élevé
        if (transaction.getMontant().compareTo(SEUIL_MONTANT_ELEVE) > 0) {
            creerActiviteSuspecte(client, transaction, TypeActiviteSuspecte.MONTANT_ELEVE,
                String.format("Transaction élevée détectée : %s MAD", transaction.getMontant()));
        }
        
        // 2. Vérifier fréquence élevée (plus de 5 transactions en 1 heure)
        LocalDateTime heureLimite = transaction.getDate().minusHours(1);
        List<Transaction> toutesTransactions = transactionDAO.findAll();
        List<Transaction> recentes = toutesTransactions.stream()
            .filter(t -> t.getCompteSource() != null && t.getCompteSource().getId().equals(compte.getId()))
            .filter(t -> t.getDate().isAfter(heureLimite) && t.getDate().isBefore(transaction.getDate()))
            .toList();
        
        if (recentes.size() >= SEUIL_FREQUENCE) {
            creerActiviteSuspecte(client, transaction, TypeActiviteSuspecte.FREQUENCE_ELEVEE,
                String.format("%d transactions effectuées en moins d'une heure", recentes.size() + 1));
        }
        
    }
    
    private void creerActiviteSuspecte(Client client, Transaction transaction, TypeActiviteSuspecte type, String description) {
        ActiviteSuspecte activite = new ActiviteSuspecte();
        activite.setClient(client);
        activite.setTransaction(transaction);
        activite.setType(type);
        activite.setDescription(description);
        activite.setMontant(transaction.getMontant());
        activite.setDateDetection(LocalDateTime.now());
        activite.setStatut(StatutActivite.NON_TRAITEE);
        activiteDAO.save(activite);
        
        // Envoyer une notification
        notificationService.notifierActiviteSuspecte(client, description);
    }
    
    public List<ActiviteSuspecte> getActivitesSuspectes(Long clientId) {
        return activiteDAO.findByClientId(clientId);
    }
    
    public List<ActiviteSuspecte> getActivitesNonTraitees() {
        return activiteDAO.findNonTraitees();
    }
    
}

