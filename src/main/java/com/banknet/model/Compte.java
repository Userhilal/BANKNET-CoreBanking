package com.banknet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comptes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String numeroCompte;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal solde = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeCompte type;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @OneToMany(mappedBy = "compteSource", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactionsSortantes = new ArrayList<>();
    
    @OneToMany(mappedBy = "compteDest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactionsEntrantes = new ArrayList<>();
}



