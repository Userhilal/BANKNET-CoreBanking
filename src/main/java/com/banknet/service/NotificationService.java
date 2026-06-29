package com.banknet.service;

import com.banknet.dao.NotificationDAO;
import com.banknet.model.Client;
import com.banknet.model.Notification;
import com.banknet.model.Notification.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationService {
    
    private final NotificationDAO notificationDAO;
    
    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }
    
    public void creerNotification(Client client, String titre, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setClient(client);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setType(type);
        notification.setLu(false);
        notification.setDateCreation(LocalDateTime.now());
        notificationDAO.save(notification);
    }
    
    public List<Notification> getNotificationsClient(Long clientId) {
        return notificationDAO.findByClientId(clientId);
    }
    
    public List<Notification> getNotificationsNonLues(Long clientId) {
        return notificationDAO.findNonLuesByClientId(clientId);
    }
    
    public long countNotificationsNonLues(Long clientId) {
        return notificationDAO.countNonLuesByClientId(clientId);
    }
    
    public void marquerCommeLu(Long notificationId) {
        notificationDAO.marquerCommeLu(notificationId);
    }
    
    public void notifierPlafondDepasse(Client client, String compteNumero, String montant) {
        creerNotification(client, "Plafond dépassé", 
            String.format("Attention : Le plafond de transaction a été dépassé pour le compte %s. Montant : %s MAD", 
                compteNumero, montant), 
            NotificationType.AVERTISSEMENT);
    }
    
    public void notifierObjectifAtteint(Client client, String objectifLibelle) {
        creerNotification(client, "Objectif atteint ! 🎉", 
            String.format("Félicitations ! Vous avez atteint votre objectif d'épargne : %s", objectifLibelle), 
            NotificationType.SUCCES);
    }
    
    public void notifierActiviteSuspecte(Client client, String description) {
        creerNotification(client, "Activité suspecte détectée", 
            String.format("Une activité suspecte a été détectée sur votre compte : %s. Veuillez contacter la banque si ce n'est pas vous.", description), 
            NotificationType.ALERTE);
    }
    
    /**
     * Compte le nombre de messages non lus des clients (pour les admins)
     * Les notifications admin sont gérées via les messages non lus
     */
    
    public int compterMessagesNonLusClients() {
        try {
            com.banknet.dao.MessageDAO messageDAO = new com.banknet.dao.MessageDAO();
            com.banknet.model.Role clientRole = com.banknet.model.Role.CLIENT;
            List<com.banknet.model.Message> messages = messageDAO.findByExpediteur(clientRole);
            return (int) messages.stream()
                .filter(m -> !m.getLu() && !m.getEstReponse())
                .count();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}

