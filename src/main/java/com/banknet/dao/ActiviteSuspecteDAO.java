package com.banknet.dao;

import com.banknet.model.ActiviteSuspecte;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ActiviteSuspecteDAO extends BaseDAO<ActiviteSuspecte> {
    
    @Override
    public ActiviteSuspecte findById(Long id) {
        Session session = getSession();
        try {
            return session.get(ActiviteSuspecte.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public List<ActiviteSuspecte> findAll() {
        Session session = getSession();
        try {
            Query<ActiviteSuspecte> query = session.createQuery("FROM ActiviteSuspecte ORDER BY dateDetection DESC", ActiviteSuspecte.class);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<ActiviteSuspecte> findByClientId(Long clientId) {
        Session session = getSession();
        try {
            Query<ActiviteSuspecte> query = session.createQuery("FROM ActiviteSuspecte WHERE client.id = :clientId ORDER BY dateDetection DESC", ActiviteSuspecte.class);
            query.setParameter("clientId", clientId);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<ActiviteSuspecte> findNonTraitees() {
        Session session = getSession();
        try {
            Query<ActiviteSuspecte> query = session.createQuery("FROM ActiviteSuspecte WHERE statut = 'NON_TRAITEE' ORDER BY dateDetection DESC", ActiviteSuspecte.class);
            return query.list();
        } finally {
            session.close();
        }
    }
}




