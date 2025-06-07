// RegistroTransferenciaDTO.java
package com.ns.iestpffaaarchives.infrastructure.web.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral.EstadoDocumento;

/**
 * DTO para Registro de Transferencia (Anexo N° 05)
 */
@Data
public class RegistroTransferenciaDTO {
    private Long id;
    //
    private String numero;
    private String codigo;
    private LocalDate fecha;
    private String dependenciaOrigen;
    private String dependenciaDestino;
    private Integer totalDocumentos;
    private String responsable;
    
    // Información General
    private String nombreEntidad;
    private String unidadAdministrativa;
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
    private String numeroConsecutivo;
    private LocalDate fechaTransferencia;
    private Long responsableEntregaId;
    private String responsableEntregaNombre;
    private Long responsableRecepcionId;
    private String responsableRecepcionNombre;
    private Integer unidadesConservacion;
    private String observaciones;
    private String estadoDocumentacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long creadorId;
    private String creadorNombre;
    private Long usuarioModificacionId;
    private String usuarioModificacionNombre;
    private EstadoDocumento estado;
    private Integer version;
    
    // Relaciones
    private List<Long> documentoIds;
    private List<DetalleRegistroTransferenciaDTO> detalles;
}
