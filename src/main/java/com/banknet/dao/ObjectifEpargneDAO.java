package com.banknet.dao;

import com.banknet.model.ObjectifEpargne;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ObjectifEpargneDAO extends BaseDAO<ObjectifEpargne> {
    
    @Override
    public ObjectifEpargne findById(Long id) {
        Session session = getSession();
        try {
            return session.get(ObjectifEpargne.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public List<ObjectifEpargne> findAll() {
        Session session = getSession();
        try {
            Query<ObjectifEpargne> query = session.createQuery("FROM ObjectifEpargne ORDER BY dateCreation DESC", ObjectifEpargne.class);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<ObjectifEpargne> findByClientId(Long clientId) {
        Session session = getSession();
        try {
            Query<ObjectifEpargne> query = session.createQuery("FROM ObjectifEpargne WHERE client.id = :clientId ORDER BY dateCible ASC", ObjectifEpargne.class);
            query.setParameter("clientId", clientId);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<ObjectifEpargne> findByCompteId(Long compteId) {
        Session session = getSession();
        try {
            Query<ObjectifEpargne> query = session.createQuery("FROM ObjectifEpargne WHERE compte.id = :compteId ORDER BY dateCible ASC", ObjectifEpargne.class);
            query.setParameter("compteId", compteId);
            return query.list();
        } finally {
            session.close();
        }
    }
}




