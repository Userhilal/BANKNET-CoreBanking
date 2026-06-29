package com.banknet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "objectifs_epargne")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectifEpargne {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;
    
    @Column(nullable = false, length = 100)
    private String libelle;
    
    @Column(nullable = false)
    private BigDecimal montantCible;
    
    @Column(nullable = false)
    private BigDecimal montantActuel = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private LocalDate dateCible;
    
    @Column(nullable = false)
    private LocalDate dateCreation = LocalDate.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutObjectif statut = StatutObjectif.EN_COURS;
    
    public enum StatutObjectif {
        EN_COURS,
        ATTEINT,
        DEPASSE
    }
    
    public BigDecimal getPourcentageProgression() {
        if (montantCible.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return montantActuel.divide(montantCible, 4, java.math.RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }
}




