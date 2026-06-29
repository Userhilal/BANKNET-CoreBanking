package com.banknet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "plafonds_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlafondTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypePlafond type;
    
    @Column(nullable = false)
    private BigDecimal montantMax;
    
    @Column(nullable = false)
    private BigDecimal montantUtilise = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private java.time.LocalDate periodeDebut;
    
    @Column(nullable = false)
    private java.time.LocalDate periodeFin;
    
    public enum TypePlafond {
        QUOTIDIEN,
        MENSUEL,
        ANNUEL
    }
    
    public boolean estDepasse(BigDecimal montant) {
        return montantUtilise.add(montant).compareTo(montantMax) > 0;
    }
    
    public BigDecimal getMontantRestant() {
        return montantMax.subtract(montantUtilise);
    }
}




