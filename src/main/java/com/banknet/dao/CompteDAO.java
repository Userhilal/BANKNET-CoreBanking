package com.banknet.dao;

import com.banknet.model.Compte;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class CompteDAO extends BaseDAO<Compte> {
    
    @Override
    public Compte findById(Long id) {
        Session session = getSession();
        try {
            return session.get(Compte.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public List<Compte> findAll() {
        Session session = getSession();
        try {
            Query<Compte> query = session.createQuery("FROM Compte", Compte.class);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public Optional<Compte> findByNumeroCompte(String numeroCompte) {
        Session session = getSession();
        try {
            Query<Compte> query = session.createQuery("FROM Compte WHERE numeroCompte = :numero", Compte.class);
            query.setParameter("numero", numeroCompte);
            return query.uniqueResultOptional();
        } finally {
            session.close();
        }
    }
    
    public List<Compte> findByClientId(Long clientId) {
        Session session = getSession();
        try {
            Query<Compte> query = session.createQuery("FROM Compte WHERE client.id = :clientId", Compte.class);
            query.setParameter("clientId", clientId);
            return query.list();
        } finally {
            session.close();
        }
    }
}



