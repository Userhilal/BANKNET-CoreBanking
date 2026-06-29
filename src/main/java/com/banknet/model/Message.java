package com.banknet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role expediteur; // CLIENT ou ADMIN/AGENT
    
    @Column(nullable = false, length = 255)
    private String sujet;
    
    @Column(nullable = false, length = 2000)
    private String contenu;
    
    @Column(nullable = false)
    private Boolean lu = false;
    
    @Column(nullable = false)
    private LocalDateTime dateEnvoi = LocalDateTime.now();
    
    @Column(nullable = false)
    private Boolean estReponse = false;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "message_parent_id", nullable = true)
    private Message messageParent; // Pour les réponses
}

