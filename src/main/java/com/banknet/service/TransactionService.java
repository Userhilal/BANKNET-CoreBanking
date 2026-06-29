package com.banknet.service;
import com.banknet.dao.CompteDAO;
import com.banknet.dao.TransactionDAO;
import com.banknet.exception.SoldeInsuffisantException;
import com.banknet.model.Compte;
import com.banknet.model.PlafondTransaction;
import com.banknet.model.Transaction;
import com.banknet.model.TypeCompte;
import com.banknet.model.TypeTransaction;
import com.banknet.model.PlafondTransaction.TypePlafond;
import com.banknet.service.ObjectifEpargneService;
import java.math.BigDecimal;
import java.util.Optional;
public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final CompteDAO compteDAO;
    private final AsyncAuditLogger auditLogger;
    private final PlafondService plafondService;
    private final ActiviteSuspecteService activiteSuspecteService;
    private final NotificationService notificationService;
    private final ObjectifEpargneService objectifEpargneService;
    //Pour éviter que deux threads 
    //modifient les mêmes données en même temps
    private final Object lock = new Object();
    
    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.compteDAO = new CompteDAO();
        this.auditLogger = AsyncAuditLogger.getInstance();
        this.plafondService = new PlafondService();
        this.activiteSuspecteService = new ActiviteSuspecteService();
        this.notificationService = new NotificationService();
        this.objectifEpargneService = new ObjectifEpargneService();
    }
    
    public Transaction virement(Long compteSourceId, Long compteDestId, BigDecimal montant, String libelle) 
            throws SoldeInsuffisantException {
        
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        
        if (compteSourceId.equals(compteDestId)) {
            throw new IllegalArgumentException("Le compte source et le compte destination ne peuvent pas être identiques");
        }
        
        synchronized (lock) {
            // Charger les comptes avec verrouillage
            Optional<Compte> compteSourceOpt = Optional.ofNullable(compteDAO.findById(compteSourceId));
            Optional<Compte> compteDestOpt = Optional.ofNullable(compteDAO.findById(compteDestId));
            
            if (compteSourceOpt.isEmpty()) {
                throw new IllegalArgumentException("Compte source introuvable");
            }
            
            if (compteDestOpt.isEmpty()) {
                throw new IllegalArgumentException("Compte destination introuvable");
            }
            
            Compte compteSource = compteSourceOpt.get();
            Compte compteDest = compteDestOpt.get();
            
            // Vérifier le solde
            if (compteSource.getSolde().compareTo(montant) < 0) {
                String message = String.format("Solde insuffisant. Solde actuel : %s, Montant demandé : %s", 
                    compteSource.getSolde(), montant);
                auditLogger.log("VIREMENT_ECHOUE", 
                    String.format("Compte %s -> %s, Montant: %s, Raison: Solde insuffisant", 
                        compteSource.getNumeroCompte(), compteDest.getNumeroCompte(), montant));
                throw new SoldeInsuffisantException(message);
            }
            
            // Effectuer le virement dans une transaction Hibernate
            Transaction transaction = transactionDAO.save(new Transaction(
                null,
                null,
                montant,
                TypeTransaction.DEBIT,
                libelle != null ? libelle : "Virement vers " + compteDest.getNumeroCompte(),
                compteSource,
                compteDest
            ));
            
            // Mettre à jour les soldes
            compteSource.setSolde(compteSource.getSolde().subtract(montant));
            compteDest.setSolde(compteDest.getSolde().add(montant));
            
            compteDAO.update(compteSource);
            compteDAO.update(compteDest);
            
           
            // Analyser l'activité suspecte
            activiteSuspecteService.analyserTransaction(transaction, compteSource);
            
            return transaction;
        }
    }
    
    public Transaction depot(Long compteId, BigDecimal montant, String libelle) {
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        //Seul un thread à la fois peut modifier les comptes et créer une transaction
        synchronized (lock) {
            Compte compte = compteDAO.findById(compteId);
            if (compte == null) {
                throw new IllegalArgumentException("Compte introuvable");
            }
            
            Transaction transaction = transactionDAO.save(new Transaction(
                null,
                null,
                montant,
                TypeTransaction.CREDIT,
                libelle != null ? libelle : "Dépôt",
                null,
                compte
            ));
            
            compte.setSolde(compte.getSolde().add(montant));
            compteDAO.update(compte);
            
            auditLogger.log("DEPOT", 
                String.format("Compte %s, Montant: %s", compte.getNumeroCompte(), montant));
            
            return transaction;
        }
    }
    
    public Transaction retrait(Long compteId, BigDecimal montant, String libelle) throws SoldeInsuffisantException {
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        
        synchronized (lock) {
            Compte compte = compteDAO.findById(compteId);
            if (compte == null) {
                throw new IllegalArgumentException("Compte introuvable");
            }
            
            if (compte.getSolde().compareTo(montant) < 0) {
                String message = String.format("Solde insuffisant. Solde actuel : %s, Montant demandé : %s", 
                    compte.getSolde(), montant);
                auditLogger.log("RETRAIT_ECHOUE", 
                    String.format("Compte %s, Montant: %s, Raison: Solde insuffisant", 
                        compte.getNumeroCompte(), montant));
                throw new SoldeInsuffisantException(message);
            }
            
            Transaction transaction = transactionDAO.save(new Transaction(
                null,
                null,
                montant,
                TypeTransaction.DEBIT,
                libelle != null ? libelle : "Retrait",
                compte,
                null
            ));
            
            compte.setSolde(compte.getSolde().subtract(montant));
            compteDAO.update(compte);
            
            auditLogger.log("RETRAIT", 
                String.format("Compte %s, Montant: %s", compte.getNumeroCompte(), montant));
            
            return transaction;
        }
    }
}
