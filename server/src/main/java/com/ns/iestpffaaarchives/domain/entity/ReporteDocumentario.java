package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a generated report. The PDF and Excel files are
 * persisted as BLOBs in the database so they can be downloaded on demand.
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

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "pdf_content")
    private byte[] pdfContent;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "excel_content")
    private byte[] excelContent;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
