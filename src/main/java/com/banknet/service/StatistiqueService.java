package com.banknet.service;
import com.banknet.dao.CompteDAO;
import com.banknet.dao.TransactionDAO;
import com.banknet.model.Compte;
import com.banknet.model.Transaction;
import com.banknet.model.TypeTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatistiqueService {
    
    private final CompteDAO compteDAO;
    private final TransactionDAO transactionDAO;
    
    public StatistiqueService() {
        this.compteDAO = new CompteDAO();
        this.transactionDAO = new TransactionDAO();
    }
    
    /**
     * Calcule le solde total de la banque en utilisant 
     */
    public BigDecimal calculerSoldeTotalBanque() {
        return compteDAO.findAll().stream()
            //extraire les soldes de chaque compte.
            //additionner tous ces soldes pour obtenir le total.  
            .map(Compte::getSolde)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Filtre les transactions par date 
     */

    public List<Transaction> filtrerTransactionsParDate(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return transactionDAO.findAll().stream()
            .filter(t -> {
                LocalDateTime date = t.getDate();
                //seules les transactions entre les deux dates sont conservées
                return !date.isBefore(dateDebut) && !date.isAfter(dateFin);
            })
            .collect(Collectors.toList());
    }                                                                                                                                                                                                                                                                                            
    
    /**
     * Calcule le nombre de transactions par type 
     */
    public Map<TypeTransaction, Long> compterTransactionsParType() {
        return transactionDAO.findAll().stream()
            .collect(Collectors.groupingBy(
                Transaction::getType,
                Collectors.counting()
            ));
    }
    /**
     * Calcule le montant total des transactions par type
     */
    public Map<TypeTransaction, BigDecimal> calculerMontantTotalParType() {
        return transactionDAO.findAll().stream()
            .collect(Collectors.groupingBy(
                Transaction::getType,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    Transaction::getMontant,
                    BigDecimal::add
                )
            ));
    }
    
    /**
     * Obtient les transactions d'un compte spécifique
     */
    
    public List<Transaction> getTransactionsParCompte(Long compteId) {
        return transactionDAO.findByCompteId(compteId);
    }
    
    /**
     * Calcule le solde moyen des comptes
     */
    public BigDecimal calculerSoldeMoyen() {
        List<Compte> comptes = compteDAO.findAll();
        if (comptes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = comptes.stream()
            .map(Compte::getSolde)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.divide(BigDecimal.valueOf(comptes.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
}



