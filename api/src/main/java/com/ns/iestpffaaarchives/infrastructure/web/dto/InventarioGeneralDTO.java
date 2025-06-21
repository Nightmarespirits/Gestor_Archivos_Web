// InventarioGeneralDTO.java
package com.ns.iestpffaaarchives.infrastructure.web.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral;

/**
 * DTO para Inventario General de Transferencia (Anexo N° 04)
 */
@Data
public class InventarioGeneralDTO {
    private Long id;
    private String titulo;
    
    // Información General
    private String unidadAdministrativa;
    private String numeroAnioRemision;
    private String seccion;
    private LocalDate fechaTransferencia;
    
    // Control y Totales
    private BigDecimal totalVolumen;
    private String lugarFechaEntrega;
    private String lugarFechaRecepcion;
    private String firmaSelloAutoridadEntrega;
    private String firmaSelloAutoridadRecibe;
    
    // Campos originales y complementarios
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private LocalDateTime fechaInicial;
    private LocalDateTime fechaFinal;
    private String estadoConservacion;
    private String observaciones;
    private Long creadorId;
    private String creadorNombre;
    private Long usuarioModificacionId;
    private String usuarioModificacionNombre;
    private InventarioGeneral.EstadoDocumento estado;
    private Integer version;
    
    // Relaciones
    private List<Long> documentoIds;
    private List<DetalleInventarioGeneralDTO> detalles;
}