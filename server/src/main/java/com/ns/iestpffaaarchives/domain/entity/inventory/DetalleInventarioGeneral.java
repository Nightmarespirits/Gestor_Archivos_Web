// DetalleInventarioGeneral.java
package com.ns.iestpffaaarchives.domain.entity.inventory;

import com.ns.iestpffaaarchives.domain.entity.Document;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Detalle del Inventario General de Transferencia (Anexo N° 04)
 */
@Entity
@Table(name = "detalles_inventario_general")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DetalleInventarioGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "inventario_id", nullable = false)
    private InventarioGeneral inventarioGeneral;
    
    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Document documento;
    
    // Atributos del Ítem según Anexo N° 04
    @Column(name = "numero_item")
    private Integer numeroItem;
    
    @Column(name = "nombre_serie_documental", length = 500)
    private String serieDocumental;
    
    @Column(name = "fecha_extrema_del")
    private LocalDate fechaExtremaDel;
    
    @Column(name = "fecha_extrema_al")
    private LocalDate fechaExtremaAl;
    
    @Column(name = "tipo_unidad_archivamiento", length = 200)
    private String tipoUnidadArchivamiento;
    
    @Column(name = "cantidad_unidad_archivamiento")
    private Integer cantidadUnidadArchivamiento;
    
    @Column(name = "volumen_metros_lineales", precision = 10, scale = 2)
    private BigDecimal volumenMetrosLineales;
    
    @Column(name = "soporte", length = 100)
    private String soporte;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    // Campo original
    private String descripcion;
}
