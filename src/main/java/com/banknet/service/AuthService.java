package com.banknet.service;

import com.banknet.dao.UserAccountDAO;
import com.banknet.model.AccountStatus;
import com.banknet.model.Role;
import com.banknet.model.UserAccount;
//BCrypt → pour hasher et vérifier les mots de passe de manière sécurisée.
import org.mindrot.jbcrypt.BCrypt;
//Optional → éviter les NullPointerException lors de recherches de l’utilisateur.
import java.util.Optional;

public class AuthService {
    
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private final UserAccountDAO userAccountDAO;
    //auditLogger → logger asynchrone pour garder trace des actions
    //(login, logout, changement de mot de passe…).
    private final AsyncAuditLogger auditLogger;
    private UserAccount currentUser;
    
    public AuthService() {
        this.userAccountDAO = new UserAccountDAO();
        this.auditLogger = AsyncAuditLogger.getInstance();
    }
    //Cherche l’utilisateur par son login.
    //Retourne un Optional<UserAccount> pour gérer le cas où l’utilisateur n’existe pas.
    public boolean login(String login, String password) {
        Optional<UserAccount> userOpt = userAccountDAO.findByLogin(login);
        
        if (userOpt.isPresent()) {
            UserAccount user = userOpt.get();
            
            // Vérifier si le compte est bloqué
            if (user.getStatus() == AccountStatus.BLOQUE) {
                auditLogger.log("LOGIN_ECHOUE", String.format("Utilisateur: %s, Raison: Compte bloqué", login));
                return false;
            }
            
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                // Réinitialiser les tentatives échouées en cas de succès
                if (user.getFailedLoginAttempts() > 0) {
                    user.setFailedLoginAttempts(0);
                    userAccountDAO.update(user);
                }
                currentUser = user;
                auditLogger.log("LOGIN_REUSSI", String.format("Utilisateur: %s, Role: %s", login, user.getRole()));
                return true;
            } else {
                // Incrémenter les tentatives échouées
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                // Bloquer le compte si le nombre maximum de tentatives est atteint
                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    user.setStatus(AccountStatus.BLOQUE);
                    auditLogger.log("COMPTE_BLOQUE", String.format("Utilisateur: %s bloqué après %d tentatives échouées", login, attempts));
                } else {
                    auditLogger.log("LOGIN_ECHOUE", String.format("Utilisateur: %s, Raison: Mot de passe incorrect (Tentative %d/%d)", login, attempts, MAX_FAILED_ATTEMPTS));
                }
                
                userAccountDAO.update(user);
            }
        } else {
            auditLogger.log("LOGIN_ECHOUE", String.format("Utilisateur: %s, Raison: Utilisateur introuvable", login));
        }
        
        return false;
    }
    
    public void logout() {
        if (currentUser != null) {
            auditLogger.log("LOGOUT", String.format("Utilisateur: %s", currentUser.getLogin()));
            currentUser = null;
        }
    }£
    public UserAccount getCurrentUser() {
        return currentUser;
    }
    
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    public boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }
    
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * Vérifier si un compte est bloqué
     */
    public boolean isAccountBlocked(String login) {
        Optional<UserAccount> userOpt = userAccountDAO.findByLogin(login);
        return userOpt.isPresent() && userOpt.get().getStatus() == AccountStatus.BLOQUE;
    }
    
    /**
     * Débloquer un compte utilisateur
     */
    public boolean unblockAccount(Long userId) {
        UserAccount user = userAccountDAO.findById(userId);
        if (user != null && user.getStatus() == AccountStatus.BLOQUE) {
            user.setStatus(AccountStatus.ACTIF);
            user.setFailedLoginAttempts(0);
            userAccountDAO.update(user);
            auditLogger.log("COMPTE_DEBLOQUE", String.format("Compte débloqué: %s (ID: %d)", user.getLogin(), userId));
            return true;
        }
        return false;
    }
    
    /**
     * Changer le mot de passe d'un utilisateur
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        UserAccount user = userAccountDAO.findById(userId);
        if (user == null) {
            return false;
        }
        
        // Vérifier l'ancien mot de passe
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            auditLogger.log("CHANGE_PASSWORD_ECHOUE", String.format("Utilisateur: %s, Raison: Ancien mot de passe incorrect", user.getLogin()));
            return false;
        }
        
        // Mettre à jour le mot de passe
        user.setPasswordHash(hashPassword(newPassword));
        userAccountDAO.update(user);
        auditLogger.log("PASSWORD_CHANGE", String.format("Mot de passe changé pour: %s", user.getLogin()));
        return true;
    }
    
    /**
     * Réinitialiser le mot de passe (pour admin)
     */
    public boolean resetPassword(Long userId, String newPassword) {
        UserAccount user = userAccountDAO.findById(userId);
        if (user == null) {
            return false;
        }
        
        user.setPasswordHash(hashPassword(newPassword));
        user.setFailedLoginAttempts(0); // Réinitialiser aussi les tentatives
        userAccountDAO.update(user);
        auditLogger.log("PASSWORD_RESET", String.format("Mot de passe réinitialisé pour: %s (ID: %d)", user.getLogin(), userId));
        return true;
    }
}
