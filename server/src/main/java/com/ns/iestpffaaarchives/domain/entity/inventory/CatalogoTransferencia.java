
// CatalogoTransferencia.java
package com.ns.iestpffaaarchives.domain.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ns.iestpffaaarchives.domain.entity.User;

/**
 * Entidad para Catálogo de Transferencia (Anexo N° 06)
 */
@Entity
@Table(name = "catalogos_transferencia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CatalogoTransferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    // Información General
    @Column(name = "nombre_entidad", length = 255)
    private String nombreEntidad;
    
    @Column(name = "unidad_organizacion", length = 255)
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
    
    // Campos originales y complementarios
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
    
    @ManyToOne
    @JoinColumn(name = "creador_id", nullable = false)
    private User creador;
    
    @ManyToOne
    @JoinColumn(name = "usuario_modificacion_id")
    private User usuarioModificacion;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private InventarioGeneral.EstadoDocumento estado;
    
    @Column(name = "version")
    private Integer version;
    
    @OneToMany(mappedBy = "catalogoTransferencia", cascade = CascadeType.ALL)
    private List<DetalleCatalogoTransferencia> detalles;
    
    @Column(name = "ruta_archivo")
    private String rutaArchivo;
    
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