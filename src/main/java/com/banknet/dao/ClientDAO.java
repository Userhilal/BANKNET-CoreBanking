package com.banknet.dao;

import com.banknet.model.Client;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class ClientDAO extends BaseDAO<Client> {
    
    @Override
    public Client findById(Long id) {
        Session session = getSession();
        try {
            return session.get(Client.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public List<Client> findAll() {
        Session session = getSession();
        try {
            Query<Client> query = session.createQuery("FROM Client", Client.class);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public Optional<Client> findByCin(String cin) {
        Session session = getSession();
        try {
            // Recherche insensible à la casse et en ignorant les espaces
            Query<Client> query = session.createQuery("FROM Client WHERE UPPER(TRIM(cin)) = UPPER(TRIM(:cin))", Client.class);
            query.setParameter("cin", cin);
            return query.uniqueResultOptional();
        } finally {
            session.close();
        }
    }
    
    public Optional<Client> findByIdAndCin(Long id, String cin) {
        Session session = getSession();
        try {
            // Recherche insensible à la casse et en ignorant les espaces
            Query<Client> query = session.createQuery("FROM Client WHERE id = :id AND UPPER(TRIM(cin)) = UPPER(TRIM(:cin))", Client.class);
            query.setParameter("id", id);
            query.setParameter("cin", cin);
            return query.uniqueResultOptional();
        } finally {
            session.close();
        }
    }
}
