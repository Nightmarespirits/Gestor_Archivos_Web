package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.ns.iestpffaaarchives.domain.enums.TipoReporte;
import com.ns.iestpffaaarchives.domain.entity.TransferenciaDocumental;

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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_documental_id")
    private TransferenciaDocumental transferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private TipoReporte tipoReporte;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "tamanio_pdf")
    private Long tamanioPdf;

    @Column(name = "tamanio_excel")
    private Long tamanioExcel;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "archivo_pdf")
    private byte[] archivoPdf;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "archivo_excel")
    private byte[] archivoExcel;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "generado_por")
    private String generadoPor;

    @PrePersist
    protected void onCreate() {
        fechaGeneracion = LocalDateTime.now();
    }
}
