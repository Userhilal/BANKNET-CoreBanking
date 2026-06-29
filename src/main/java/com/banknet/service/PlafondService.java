package com.banknet.service;

import com.banknet.dao.PlafondTransactionDAO;
import com.banknet.model.Compte;
import com.banknet.model.PlafondTransaction;
import com.banknet.model.PlafondTransaction.TypePlafond;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class PlafondService {
    
    private final PlafondTransactionDAO plafondDAO;
    
    public PlafondService() {
        this.plafondDAO = new PlafondTransactionDAO();
    }
    
    public PlafondTransaction creerPlafond(Compte compte, TypePlafond type, BigDecimal montantMax, LocalDate periodeDebut, LocalDate periodeFin) {
        PlafondTransaction plafond = new PlafondTransaction();
        plafond.setCompte(compte);
        plafond.setType(type);
        plafond.setMontantMax(montantMax);
        plafond.setMontantUtilise(BigDecimal.ZERO);
        plafond.setPeriodeDebut(periodeDebut);
        plafond.setPeriodeFin(periodeFin);
        return plafondDAO.save(plafond);
    }
    
    public boolean verifierPlafond(Long compteId, BigDecimal montant, TypePlafond type) {
        LocalDate aujourdhui = LocalDate.now();
        Optional<PlafondTransaction> plafondOpt = plafondDAO.findByCompteAndTypeAndPeriode(compteId, type, aujourdhui);
        
        if (plafondOpt.isPresent()) {
            PlafondTransaction plafond = plafondOpt.get();
            return !plafond.estDepasse(montant);
        }
        
        // Si pas de plafond défini, autoriser la transaction
        return true;
    }
    
    public void incrementerUtilisation(Long compteId, BigDecimal montant, TypePlafond type) {
        LocalDate aujourdhui = LocalDate.now();
        Optional<PlafondTransaction> plafondOpt = plafondDAO.findByCompteAndTypeAndPeriode(compteId, type, aujourdhui);
        
        if (plafondOpt.isPresent()) {
            PlafondTransaction plafond = plafondOpt.get();
            plafond.setMontantUtilise(plafond.getMontantUtilise().add(montant));
            plafondDAO.update(plafond);
        }
    }
    
    public Optional<PlafondTransaction> getPlafond(Long compteId, TypePlafond type) {
        LocalDate aujourdhui = LocalDate.now();
        return plafondDAO.findByCompteAndTypeAndPeriode(compteId, type, aujourdhui);
    }
}




