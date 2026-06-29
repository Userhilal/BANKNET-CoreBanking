package com.banknet.dao;

import com.banknet.model.UserAccount;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class UserAccountDAO extends BaseDAO<UserAccount> {
    
    @Override
    public UserAccount findById(Long id) {
        Session session = getSession();
        try {
            return session.get(UserAccount.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public List<UserAccount> findAll() {
        Session session = getSession();
        try {
            Query<UserAccount> query = session.createQuery("FROM UserAccount", UserAccount.class);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public Optional<UserAccount> findByLogin(String login) {
        Session session = getSession();
        try {
            Query<UserAccount> query = session.createQuery("FROM UserAccount WHERE login = :login", UserAccount.class);
            query.setParameter("login", login);
            return query.uniqueResultOptional();
        } finally {
            session.close();
        }
    }
    
    public boolean existsByLogin(String login) {
        Session session = getSession();
        try {
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM UserAccount WHERE login = :login", Long.class);
            query.setParameter("login", login);
            return query.uniqueResult() > 0;
        } finally {
            session.close();
        }
    }
    
    public List<UserAccount> findByStatus(com.banknet.model.AccountStatus status) {
        Session session = getSession();
        try {
            Query<UserAccount> query = session.createQuery("FROM UserAccount WHERE status = :status", UserAccount.class);
            query.setParameter("status", status);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<UserAccount> findBlockedAccounts() {
        return findByStatus(com.banknet.model.AccountStatus.BLOQUE);
    }
}
