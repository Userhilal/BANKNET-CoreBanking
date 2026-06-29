package com.banknet.service;

import com.banknet.dao.CompteDAO;
import com.banknet.dao.ObjectifEpargneDAO;
import com.banknet.model.Client;
import com.banknet.model.Compte;
import com.banknet.model.ObjectifEpargne;
import com.banknet.model.ObjectifEpargne.StatutObjectif;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ObjectifEpargneService {
    
    private final ObjectifEpargneDAO objectifDAO;
    private final CompteDAO compteDAO;
    private final NotificationService notificationService;
    
    public ObjectifEpargneService() {
        this.objectifDAO = new ObjectifEpargneDAO();
        this.compteDAO = new CompteDAO();
        this.notificationService = new NotificationService();
    }
    
    public ObjectifEpargne creerObjectif(Client client, Long compteId, String libelle, BigDecimal montantCible, LocalDate dateCible) {
        Compte compte = compteDAO.findById(compteId);
        if (compte == null) {
            throw new IllegalArgumentException("Compte introuvable");
        }
        
        if (!compte.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Le compte n'appartient pas au client");
        }
        
        ObjectifEpargne objectif = new ObjectifEpargne();
        objectif.setClient(client);
        objectif.setCompte(compte);
        objectif.setLibelle(libelle);
        objectif.setMontantCible(montantCible);
        objectif.setMontantActuel(BigDecimal.ZERO);
        objectif.setDateCible(dateCible);
        objectif.setDateCreation(LocalDate.now());
        objectif.setStatut(StatutObjectif.EN_COURS);
        
        return objectifDAO.save(objectif);
    }
    
    public void mettreAJourObjectifs(Long compteId) {
        Compte compte = compteDAO.findById(compteId);
        if (compte == null) return;
        
        List<ObjectifEpargne> objectifs = objectifDAO.findByCompteId(compteId);
        BigDecimal soldeActuel = compte.getSolde();
        
        for (ObjectifEpargne objectif : objectifs) {
            if (objectif.getStatut() == StatutObjectif.EN_COURS) {
                objectif.setMontantActuel(soldeActuel);
                
                if (soldeActuel.compareTo(objectif.getMontantCible()) >= 0) {
                    objectif.setStatut(StatutObjectif.ATTEINT);
                    notificationService.notifierObjectifAtteint(objectif.getClient(), objectif.getLibelle());
                } else if (soldeActuel.compareTo(objectif.getMontantCible().multiply(new BigDecimal("1.2"))) >= 0) {
                    objectif.setStatut(StatutObjectif.DEPASSE);
                }
                
                objectifDAO.update(objectif);
            }
        }
    }
    
    public List<ObjectifEpargne> getObjectifsClient(Long clientId) {
        return objectifDAO.findByClientId(clientId);
    }
    
    public ObjectifEpargne getObjectif(Long objectifId) {
        return objectifDAO.findById(objectifId);
    }
    
    public void supprimerObjectif(Long objectifId) {
        ObjectifEpargne objectif = objectifDAO.findById(objectifId);
        if (objectif != null) {
            objectifDAO.delete(objectif);
        }
    }
}




