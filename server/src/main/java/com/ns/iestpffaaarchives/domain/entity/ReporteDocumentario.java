package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a generated report stored on disk.
 */
@Entity
@Table(name = "reporte_documentario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDocumentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "transfer_id")
    private Long transferId;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
