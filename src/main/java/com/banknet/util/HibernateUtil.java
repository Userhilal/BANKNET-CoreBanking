package com.banknet.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    
    private static SessionFactory sessionFactory;
    
    static {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            sessionFactory = configuration.buildSessionFactory();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de Hibernate : " + e.getMessage());
            // Ne pas imprimer la stack trace complète pour une erreur de connexion normale
            if (e.getMessage() == null || !e.getMessage().contains("Access denied")) {
                e.printStackTrace();
            }
            sessionFactory = null;
        }
    }
    
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("SessionFactory n'est pas initialisé. Vérifiez la connexion à la base de données MySQL.");
        }
        return sessionFactory;
    }
    
    public static boolean isInitialized() {
        return sessionFactory != null;
    }
    
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
