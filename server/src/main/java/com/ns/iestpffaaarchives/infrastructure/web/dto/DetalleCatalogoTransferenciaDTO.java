// DetalleCatalogoTransferenciaDTO.java
package com.ns.iestpffaaarchives.infrastructure.web.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para Detalle del Catálogo de Transferencia (Anexo N° 06)
 */
@Data
public class DetalleCatalogoTransferenciaDTO {
    private Long id;
    private Long catalogoTransferenciaId;
    private Long documentoId;
    
    // Atributos de Items del Catálogo
    private Integer numeroItem;
    private String numeroCaja;
    private String numeroTomoPaquete;
    private String numeroUnidadDocumental;
    private LocalDate fechaUnidadDocumental;
    private String alcanceContenido;
    private String informacionAdicional;
    private Integer cantidadFolios;
    private String observaciones;
    
    // Campos originales
    private String codigoClasificacion;
    private String nombreExpediente;
    private LocalDate fechaInicial;
    private LocalDate fechaFinal;
    private String descriptoresTematicos;
    private String ubicacionFisica;
    
    // Información del documento asociado (opcional, para mostrar en frontend)
    private String documentoNombre;
    private String documentoTipo;
}
