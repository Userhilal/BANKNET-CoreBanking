package com.banknet.dao;

import com.banknet.model.Message;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class MessageDAO extends BaseDAO<Message> {
    
    @Override
    public Message findById(Long id) {
        Session session = getSession();
        try {
            return session.get(Message.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public List<Message> findAll() {
        Session session = getSession();
        try {
            Query<Message> query = session.createQuery("FROM Message ORDER BY dateEnvoi DESC", Message.class);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<Message> findByClientId(Long clientId) {
        Session session = getSession();
        try {
            Query<Message> query = session.createQuery("FROM Message WHERE client.id = :clientId ORDER BY dateEnvoi DESC", Message.class);
            query.setParameter("clientId", clientId);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<Message> findNonLusByClientId(Long clientId) {
        Session session = getSession();
        try {
            Query<Message> query = session.createQuery("FROM Message WHERE client.id = :clientId AND lu = false ORDER BY dateEnvoi DESC", Message.class);
            query.setParameter("clientId", clientId);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<Message> findByMessageParent(Long messageParentId) {
        Session session = getSession();
        try {
            Query<Message> query = session.createQuery("FROM Message WHERE messageParent.id = :parentId ORDER BY dateEnvoi ASC", Message.class);
            query.setParameter("parentId", messageParentId);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<Message> findByExpediteur(com.banknet.model.Role expediteur) {
        Session session = getSession();
        try {
            Query<Message> query = session.createQuery("FROM Message WHERE expediteur = :expediteur ORDER BY dateEnvoi DESC", Message.class);
            query.setParameter("expediteur", expediteur);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public void marquerCommeLu(Long messageId) {
        executeInTransaction(session -> {
            Message message = session.get(Message.class, messageId);
            if (message != null) {
                message.setLu(true);
                session.merge(message);
            }
            return null;
        });
    }
}

