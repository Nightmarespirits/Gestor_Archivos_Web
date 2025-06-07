// DetalleCatalogoTransferencia.java
package com.ns.iestpffaaarchives.domain.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

import com.ns.iestpffaaarchives.domain.entity.Document;

/**
 * Detalle del Catálogo de Transferencia (Anexo N° 06)
 */
@Entity
@Table(name = "detalles_catalogo_transferencia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DetalleCatalogoTransferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "catalogo_id", nullable = false)
    private CatalogoTransferencia catalogoTransferencia;
    
    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Document documento;
    
    // Atributos de Items del Catálogo
    @Column(name = "numero_item")
    private Integer numeroItem;
    
    @Column(name = "numero_caja", length = 50)
    private String numeroCaja;
    
    @Column(name = "numero_tomo_paquete", length = 50)
    private String numeroTomoPaquete;
    
    @Column(name = "numero_unidad_documental", length = 100)
    private String numeroUnidadDocumental;
    
    @Column(name = "fecha_unidad_documental")
    private LocalDate fechaUnidadDocumental;
    
    @Column(name = "alcance_contenido", columnDefinition = "TEXT")
    private String alcanceContenido;
    
    @Column(name = "informacion_adicional", columnDefinition = "TEXT")
    private String informacionAdicional;
    
    @Column(name = "cantidad_folios")
    private Integer cantidadFolios;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    // Campos originales
    @Column(name = "codigo_clasificacion")
    private String codigoClasificacion;
    
    @Column(name = "nombre_expediente")
    private String nombreExpediente;
    
    @Column(name = "fecha_inicial")
    private LocalDate fechaInicial;
    
    @Column(name = "fecha_final")
    private LocalDate fechaFinal;
    
    private String descriptoresTematicos;
    
    @Column(name = "ubicacion_fisica")
    private String ubicacionFisica;
}