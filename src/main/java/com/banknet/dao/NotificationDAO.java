package com.banknet.dao;

import com.banknet.model.Notification;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class NotificationDAO extends BaseDAO<Notification> {
    
    @Override
    public Notification findById(Long id) {
        Session session = getSession();
        try {
            return session.get(Notification.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public List<Notification> findAll() {
        Session session = getSession();
        try {
            Query<Notification> query = session.createQuery("FROM Notification ORDER BY dateCreation DESC", Notification.class);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<Notification> findByClientId(Long clientId) {
        Session session = getSession();
        try {
            Query<Notification> query = session.createQuery("FROM Notification WHERE client.id = :clientId ORDER BY dateCreation DESC", Notification.class);
            query.setParameter("clientId", clientId);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<Notification> findNonLuesByClientId(Long clientId) {
        Session session = getSession();
        try {
            Query<Notification> query = session.createQuery("FROM Notification WHERE client.id = :clientId AND lu = false ORDER BY dateCreation DESC", Notification.class);
            query.setParameter("clientId", clientId);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public long countNonLuesByClientId(Long clientId) {
        Session session = getSession();
        try {
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM Notification WHERE client.id = :clientId AND lu = false", Long.class);
            query.setParameter("clientId", clientId);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
    
    public void marquerCommeLu(Long notificationId) {
        executeInTransaction(session -> {
            Notification notification = session.get(Notification.class, notificationId);
            if (notification != null) {
                notification.setLu(true);
                session.merge(notification);
            }
            return null;
        });
    }
}




