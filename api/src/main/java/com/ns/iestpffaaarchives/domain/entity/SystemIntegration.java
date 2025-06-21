package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_integrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "system_name", nullable = false)
    private String systemName;

    @Column(name = "api_endpoint")
    private String apiEndpoint;

    @Column(name = "auth_token")
    private String authToken;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    @Column(name = "sync_status")
    private String syncStatus;
}
