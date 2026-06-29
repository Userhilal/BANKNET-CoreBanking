package com.banknet.service;

import com.banknet.dao.MessageDAO;
import com.banknet.model.Client;
import com.banknet.model.Message;
import com.banknet.model.Notification;
import com.banknet.model.Role;

import java.time.LocalDateTime;
import java.util.List;

public class MessageService {
    
    private final MessageDAO messageDAO;
    private final NotificationService notificationService;
    
    public MessageService() {
        this.messageDAO = new MessageDAO();
        this.notificationService = new NotificationService();
    }
    
    public Message envoyerMessage(Client client, Role expediteur, String sujet, String contenu) {
        Message message = new Message();
        message.setClient(client);
        message.setExpediteur(expediteur);
        message.setSujet(sujet);
        message.setContenu(contenu);
        message.setLu(false);
        message.setDateEnvoi(LocalDateTime.now());
        message.setEstReponse(false);
        Message savedMessage = messageDAO.save(message);
        
        // Si c'est un client qui envoie un message, notifier les admins
        // Les notifications admin sont gérées via les messages non lus (badge visible dans l'interface)
        // L'interface MessagesAdminController affiche déjà les messages non lus avec un indicateur 🔴
        
        return savedMessage;
    }
    
    public Message repondreMessage(Message messageParent, Role expediteur, String contenu) {
        Message reponse = new Message();
        reponse.setClient(messageParent.getClient());
        reponse.setExpediteur(expediteur);
        reponse.setSujet("Re: " + messageParent.getSujet());
        reponse.setContenu(contenu);
        reponse.setLu(false);
        reponse.setDateEnvoi(LocalDateTime.now());
        reponse.setEstReponse(true);
        reponse.setMessageParent(messageParent);
        Message savedReponse = messageDAO.save(reponse);
        
        // Si c'est l'admin qui répond, notifier le client
        if (expediteur == Role.ADMIN && messageParent.getClient() != null) {
            notificationService.creerNotification(
                messageParent.getClient(),
                "Réponse à votre message",
                String.format("Vous avez reçu une réponse concernant : %s", messageParent.getSujet()),
                Notification.NotificationType.INFO
            );
        }
        // Si c'est un client qui répond, les notifications admin sont gérées via les messages non lus
        
        return savedReponse;
    }
    
    public List<Message> getMessagesClient(Long clientId) {
        return messageDAO.findByClientId(clientId);
    }
    
    public List<Message> getMessagesNonLus(Long clientId) {
        return messageDAO.findNonLusByClientId(clientId);
    }
    
    public List<Message> getReponses(Long messageId) {
        return messageDAO.findByMessageParent(messageId);
    }
    
    public void marquerCommeLu(Long messageId) {
        messageDAO.marquerCommeLu(messageId);
    }
    
    public Message getMessage(Long messageId) {
        return messageDAO.findById(messageId);
    }
    
    public List<Message> findAllMessagesFromClients() {
        return messageDAO.findByExpediteur(Role.CLIENT);
    }
}

