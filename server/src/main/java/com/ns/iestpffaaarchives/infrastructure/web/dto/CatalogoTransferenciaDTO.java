// CatalogoTransferenciaDTO.java
package com.ns.iestpffaaarchives.infrastructure.web.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral.EstadoDocumento;

/**
 * DTO para Catálogo de Transferencia (Anexo N° 06)
 */
@Data
public class CatalogoTransferenciaDTO {
    private Long id;
    private String titulo;
    
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
    
    // Campos originales y complementarios
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long creadorId;
    private String creadorNombre;
    private Long usuarioModificacionId;
    private String usuarioModificacionNombre;
    private String observaciones;
    private EstadoDocumento estado;
    private Integer version;
    
    // Relaciones
    private List<Long> documentoIds;
    private List<DetalleCatalogoTransferenciaDTO> detalles;
}
