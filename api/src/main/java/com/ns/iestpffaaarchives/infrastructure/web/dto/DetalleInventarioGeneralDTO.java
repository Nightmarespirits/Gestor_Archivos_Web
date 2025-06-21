// DetalleInventarioGeneralDTO.java
package com.ns.iestpffaaarchives.infrastructure.web.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para Detalle del Inventario General de Transferencia
 */
@Data
public class DetalleInventarioGeneralDTO {
    private Long id;
    private Long inventarioGeneralId;
    private Long documentoId;
    
    // Atributos del Ítem según Anexo N° 04
    private Integer numeroItem;
    private String serieDocumental;
    private LocalDate fechaExtremaDel;
    private LocalDate fechaExtremaAl;
    private String tipoUnidadArchivamiento;
    private Integer cantidadUnidadArchivamiento;
    private BigDecimal volumenMetrosLineales;
    private String soporte;
    private String observaciones;
    
    // Campo original
    private String descripcion;
    
    // Información del documento asociado (opcional, para mostrar en frontend)
    private String documentoNombre;
    private String documentoTipo;
}
