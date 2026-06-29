package com.banknet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activites_suspectes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteSuspecte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id", nullable = true)
    private Transaction transaction;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TypeActiviteSuspecte type;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(nullable = false)
    private BigDecimal montant;
    
    @Column(nullable = false)
    private LocalDateTime dateDetection = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutActivite statut = StatutActivite.NON_TRAITEE;
    
    public enum TypeActiviteSuspecte {
        MONTANT_ELEVE,
        FREQUENCE_ELEVEE,
        TRANSACTION_NOCTURNE,
        PLAFOND_DEPASSE,
        COMPTE_BLOQUE,
        SOLDE_INSUFFISANT_MULTIPLE
    }
    
    public enum StatutActivite {
        NON_TRAITEE,
        EN_COURS,
        RESOLUE,
        FAUX_POSITIF
    }
}




