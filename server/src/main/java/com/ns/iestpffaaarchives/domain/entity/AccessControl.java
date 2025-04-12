package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_control")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "access_date")
    private LocalDateTime accessDate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "access_type", length = 50)
    private String accessType;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;
}
