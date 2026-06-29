package com.banknet.dao;

import com.banknet.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.function.Function;

public abstract class BaseDAO<T> {
    //Ouvre une session Hibernate 
    //Accessible uniquement aux sous-classes
    protected Session getSession() {
        if (!HibernateUtil.isInitialized()) {
            throw new IllegalStateException("La base de données n'est pas disponible. Vérifiez votre connexion MySQL.");
        }
        return HibernateUtil.getSessionFactory().openSession();
    }
    
    protected <R> R executeInTransaction(Function<Session, R> function) {
        Session session = getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            R result = function.apply(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur DAO : " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }
    
    public T save(T entity) {
        return executeInTransaction(session -> {
            session.persist(entity);
            return entity;
        });
    }
    
    public T update(T entity) {
        return executeInTransaction(session -> {
            session.merge(entity);
            return entity;
        });
    }
    
    public void delete(T entity) {
        executeInTransaction(session -> {
            session.remove(session.contains(entity) ? entity : session.merge(entity));
            return null;
        });
    }
    
    public abstract T findById(Long id);
    public abstract List<T> findAll();
}
