package com.ns.iestpffaaarchives.infrastructure.web.dto.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para Catálogo de Transferencia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoTransferenciaDTO {
    private Long id;
    
    // Información General
    private String nombreEntidad;
    private String unidadOrganizacion;
    private String seccion;
    private String nivelDescripcion;
    private String serieDocumental;
    private String codigoReferencia;
    private String soporte;
    private BigDecimal volumenMetrosLineales;
    private String responsableSeccion;
    private String inventarioElaboradoPor;
    private String numeroAnioRemision;
    private String lugarFechaElaboracion;
    
    // Atributos de Control
    private String vistoBuenoResponsable;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaModificacion;
    
    private Long creadorId;
    private String creadorNombre;
    
    private Long usuarioModificacionId;
    private String usuarioModificacionNombre;
    
    private String observaciones;
    private String estado;
    private Integer version;
    
    private String rutaArchivo;
    
    private List<DetalleCatalogoTransferenciaDTO> detalles;
}
