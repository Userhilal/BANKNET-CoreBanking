package com.banknet.dao;

import com.banknet.model.PlafondTransaction;
import com.banknet.model.PlafondTransaction.TypePlafond;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PlafondTransactionDAO extends BaseDAO<PlafondTransaction> {
    
    @Override
    public PlafondTransaction findById(Long id) {
        Session session = getSession();
        try {
            return session.get(PlafondTransaction.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public List<PlafondTransaction> findAll() {
        Session session = getSession();
        try {
            Query<PlafondTransaction> query = session.createQuery("FROM PlafondTransaction", PlafondTransaction.class);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public Optional<PlafondTransaction> findByCompteAndTypeAndPeriode(Long compteId, TypePlafond type, LocalDate date) {
        Session session = getSession();
        try {
            Query<PlafondTransaction> query = session.createQuery(
                "FROM PlafondTransaction WHERE compte.id = :compteId AND type = :type " +
                "AND :date BETWEEN periodeDebut AND periodeFin",
                PlafondTransaction.class);
            query.setParameter("compteId", compteId);
            query.setParameter("type", type);
            query.setParameter("date", date);
            return query.uniqueResultOptional();
        } finally {
            session.close();
        }
    }
    
    public List<PlafondTransaction> findByCompteId(Long compteId) {
        Session session = getSession();
        try {
            Query<PlafondTransaction> query = session.createQuery("FROM PlafondTransaction WHERE compte.id = :compteId", PlafondTransaction.class);
            query.setParameter("compteId", compteId);
            return query.list();
        } finally {
            session.close();
        }
    }
}

