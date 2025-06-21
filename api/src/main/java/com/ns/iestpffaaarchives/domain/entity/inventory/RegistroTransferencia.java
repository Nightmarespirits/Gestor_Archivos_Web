// RegistroTransferencia.java
package com.ns.iestpffaaarchives.domain.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ns.iestpffaaarchives.domain.entity.User;

/**
 * Entidad para Registro de Transferencia (Anexo N° 05)
 */
@Entity
@Table(name = "registros_transferencia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegistroTransferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Información General
    @Column(name = "nombre_entidad", length = 255)
    private String nombreEntidad;
    
    @Column(name = "unidad_organizacion", length = 255, nullable = false)
    private String unidadOrganizacion;
    
    @Column(length = 255)
    private String seccion;
    
    @Column(name = "nivel_descripcion", length = 100)
    private String nivelDescripcion;
    
    @Column(name = "serie_documental", length = 300)
    private String serieDocumental;
    
    @Column(name = "codigo_referencia", length = 100)
    private String codigoReferencia;
    
    @Column(length = 100)
    private String soporte;
    
    @Column(name = "volumen_metros_lineales", precision = 10, scale = 2)
    private BigDecimal volumenMetrosLineales;
    
    @Column(name = "responsable_seccion", length = 255)
    private String responsableSeccion;
    
    @Column(name = "inventario_elaborado_por", length = 255)
    private String inventarioElaboradoPor;
    
    @Column(name = "numero_año_remision", length = 100)
    private String numeroAnioRemision;
    
    @Column(name = "lugar_fecha_elaboracion", length = 300)
    private String lugarFechaElaboracion;
    
    // Atributos de Control
    @Column(name = "visto_bueno_responsable", length = 255)
    private String vistoBuenoResponsable;

    @Column(name = "fecha_transferencia", nullable = false)
    private LocalDate fechaTransferencia;
    
    @Column(name = "ruta_archivo")
    private String rutaArchivo;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
    
    @ManyToOne
    @JoinColumn(name = "creador_id")
    private User creador;
    
    @ManyToOne
    @JoinColumn(name = "usuario_modificacion_id")
    private User usuarioModificacion;
    
    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private InventarioGeneral.EstadoDocumento estado;
    
    @Column(name = "version")
    private Integer version;
    
    @OneToMany(mappedBy = "registroTransferencia", cascade = CascadeType.ALL)
    private List<DetalleRegistroTransferencia> detalles;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        version = 1;
        if (estado == null) {
            estado = InventarioGeneral.EstadoDocumento.BORRADOR;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
        version = version + 1;
    }
}
