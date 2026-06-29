package com.banknet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeTransaction type;
    
    @Column(length = 200)
    private String libelle;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "compte_source_id")
    private Compte compteSource;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "compte_dest_id")
    private Compte compteDest;
    
    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
    }
}



