package com.banknet.dao;

import com.banknet.model.Transaction;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionDAO extends BaseDAO<Transaction> {
    
    @Override
    public Transaction findById(Long id) {
        Session session = getSession();
        try {
            return session.get(Transaction.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public List<Transaction> findAll() {
        Session session = getSession();
        try {
            Query<Transaction> query = session.createQuery("FROM Transaction ORDER BY date DESC", Transaction.class);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<Transaction> findByCompteId(Long compteId) {
        Session session = getSession();
        try {
            Query<Transaction> query = session.createQuery(
                "FROM Transaction WHERE compteSource.id = :compteId OR compteDest.id = :compteId ORDER BY date DESC", 
                Transaction.class);
            query.setParameter("compteId", compteId);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<Transaction> findByDateRange(LocalDateTime dateDebut, LocalDateTime dateFin) {
        Session session = getSession();
        try {
            Query<Transaction> query = session.createQuery(
                "FROM Transaction WHERE date BETWEEN :dateDebut AND :dateFin ORDER BY date DESC", 
                Transaction.class);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            return query.list();
        } finally {
            session.close();
        }
    }
}



