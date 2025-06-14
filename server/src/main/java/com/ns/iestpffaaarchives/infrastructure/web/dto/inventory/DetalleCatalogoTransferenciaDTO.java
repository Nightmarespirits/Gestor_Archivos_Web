package com.ns.iestpffaaarchives.infrastructure.web.dto.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO para Detalle de Catálogo de Transferencia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCatalogoTransferenciaDTO {
    private Long id;
    private Long catalogoId;
    
    // Atributos de Items del Catálogo
    private Integer numeroItem;
    private String numeroCaja;
    private String numeroTomoPaquete;
    private String numeroUnidadDocumental;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaUnidadDocumental;
    
    private String alcanceContenido;
    private String informacionAdicional;
    private Integer cantidadFolios;
    private String observaciones;
}
