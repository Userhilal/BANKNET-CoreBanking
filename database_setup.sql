-- Script de création de la base de données BANKNET
-- À exécuter dans MySQL avant de lancer l'application

CREATE DATABASE IF NOT EXISTS banknet_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE banknet_db;

-- Note: Les tables seront créées automatiquement par Hibernate
-- avec la configuration hbm2ddl.auto = update dans hibernate.cfg.xml

-- Vous pouvez également créer manuellement les tables si nécessaire,
-- mais Hibernate le fera automatiquement au premier lancement.



