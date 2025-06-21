// InventarioGeneral.java
package com.ns.iestpffaaarchives.domain.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ns.iestpffaaarchives.domain.entity.User;

/**
 * Entidad para Inventario General de Transferencia (Anexo N° 04)
 */
@Entity
@Table(name = "inventarios_generales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventarioGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    // Información General
    @Column(nullable = false, name = "unidad_organizacion")
    private String unidadAdministrativa;
    
    @Column(name = "numero_año_remision")
    private String numeroAnioRemision;
    
    @Column
    private String seccion;
    
    @Column(name = "fecha_transferencia")
    private LocalDate fechaTransferencia;
    
    // Control y Totales
    @Column(name = "total_volumen", precision = 10, scale = 2)
    private BigDecimal totalVolumen;
    
    @Column(name = "lugar_fecha_entrega")
    private String lugarFechaEntrega;
    
    @Column(name = "lugar_fecha_recepcion")
    private String lugarFechaRecepcion;
    
    @Column(name = "firma_sello_autoridad_entrega")
    private String firmaSelloAutoridadEntrega;
    
    @Column(name = "firma_sello_autoridad_recibe")
    private String firmaSelloAutoridadRecibe;
    
    // Campos originales y complementarios
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_inicial")
    private LocalDateTime fechaInicial;
    
    @Column(name = "fecha_final")
    private LocalDateTime fechaFinal;
    
    @Column(name = "estado_conservacion")
    private String estadoConservacion;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @ManyToOne
    @JoinColumn(name = "creador_id", nullable = false)
    private User creador;
    
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
    
    @ManyToOne
    @JoinColumn(name = "usuario_modificacion_id")
    private User usuarioModificacion;
    
    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoDocumento estado;
    
    @Column(name = "version")
    private Integer version;
    
    @OneToMany(mappedBy = "inventarioGeneral", cascade = CascadeType.ALL)
    private List<DetalleInventarioGeneral> detalles;
    
    @Column(name = "ruta_archivo")
    private String rutaArchivo;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        version = 1;
        if (estado == null) {
            estado = EstadoDocumento.BORRADOR;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
        version = version + 1;
    }
    
    public enum EstadoDocumento {
        BORRADOR, FINALIZADO, APROBADO
    }
}


