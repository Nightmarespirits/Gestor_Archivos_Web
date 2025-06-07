// DetalleRegistroTransferenciaDTO.java
package com.ns.iestpffaaarchives.infrastructure.web.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para Detalle del Registro de Transferencia (Anexo N° 05)
 */
@Data
public class DetalleRegistroTransferenciaDTO {
    private Long id;
    private Long registroTransferenciaId;
    private Long documentoId;
    
    // Atributos de Items del Registro
    private Integer numeroItem;
    private String numeroCaja;
    private String numeroTomoPaquete;
    private String alcanceContenido;
    private String rangoExtremoDel;
    private String rangoExtremoAl;
    private LocalDate fechaExtremaDel;
    private LocalDate fechaExtremaAl;
    private Integer cantidadFolios;
    private String observaciones;
    
    // Información del documento asociado (opcional, para mostrar en frontend)
    private String documentoNombre;
    private String documentoTipo;
}
