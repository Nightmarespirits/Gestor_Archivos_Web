package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_security")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSecurity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "security", cascade = CascadeType.ALL)
    private Document document;

    @Column(name = "encryption_status")
    private Boolean encryptionStatus = false;

    @Column(name = "encryption_method")
    private String encryptionMethod;

    @Column(name = "access_level", nullable = false)
    private String accessLevel = "Privado";
}
