// DetalleRegistroTransferencia.java
package com.ns.iestpffaaarchives.domain.entity.inventory;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import com.ns.iestpffaaarchives.domain.entity.Document;

/**
 * Detalle del Registro de Transferencia (Anexo N° 05)
 */
@Entity
@Table(name = "detalles_registro_transferencia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DetalleRegistroTransferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "registro_transferencia_id", nullable = false)
    private RegistroTransferencia registroTransferencia;
    
    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Document documento;
    
    // Atributos de Items del Registro
    @Column(name = "numero_item")
    private Integer numeroItem;
    
    @Column(name = "numero_caja", length = 50)
    private String numeroCaja;
    
    @Column(name = "numero_tomo_paquete", length = 50)
    private String numeroTomoPaquete;
    
    @Column(name = "alcance_contenido", columnDefinition = "TEXT")
    private String alcanceContenido;
    
    @Column(name = "rango_extremo_del", length = 100)
    private String rangoExtremoDel;
    
    @Column(name = "rango_extremo_al", length = 100)
    private String rangoExtremoAl;
    
    @Column(name = "fecha_extrema_del")
    private LocalDate fechaExtremaDel;
    
    @Column(name = "fecha_extrema_al")
    private LocalDate fechaExtremaAl;
    
    @Column(name = "cantidad_folios")
    private Integer cantidadFolios;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
