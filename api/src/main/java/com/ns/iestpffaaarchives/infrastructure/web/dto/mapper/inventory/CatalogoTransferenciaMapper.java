package com.ns.iestpffaaarchives.infrastructure.web.dto.mapper.inventory;

import com.ns.iestpffaaarchives.domain.entity.inventory.CatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleCatalogoTransferencia;
import com.ns.iestpffaaarchives.infrastructure.web.dto.inventory.CatalogoTransferenciaDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre CatalogoTransferencia y CatalogoTransferenciaDTO
 */
public class CatalogoTransferenciaMapper {

    /**
     * Convierte una entidad CatalogoTransferencia a DTO
     */
    public static CatalogoTransferenciaDTO toDTO(CatalogoTransferencia catalogo) {
        if (catalogo == null) {
            return null;
        }
        
        CatalogoTransferenciaDTO dto = new CatalogoTransferenciaDTO();
        dto.setId(catalogo.getId());
        
        // Información General
        dto.setNombreEntidad(catalogo.getNombreEntidad());
        dto.setUnidadOrganizacion(catalogo.getUnidadOrganizacion());
        dto.setSeccion(catalogo.getSeccion());
        dto.setNivelDescripcion(catalogo.getNivelDescripcion());
        dto.setSerieDocumental(catalogo.getSerieDocumental());
        dto.setCodigoReferencia(catalogo.getCodigoReferencia());
        dto.setSoporte(catalogo.getSoporte());
        dto.setVolumenMetrosLineales(catalogo.getVolumenMetrosLineales());
        dto.setResponsableSeccion(catalogo.getResponsableSeccion());
        dto.setInventarioElaboradoPor(catalogo.getInventarioElaboradoPor());
        dto.setNumeroAnioRemision(catalogo.getNumeroAnioRemision());
        dto.setLugarFechaElaboracion(catalogo.getLugarFechaElaboracion());
        
        // Atributos de Control
        dto.setVistoBuenoResponsable(catalogo.getVistoBuenoResponsable());
        dto.setFechaCreacion(catalogo.getFechaCreacion());
        dto.setFechaModificacion(catalogo.getFechaModificacion());
        
        if (catalogo.getCreador() != null) {
            dto.setCreadorId(catalogo.getCreador().getId());
            dto.setCreadorNombre(catalogo.getCreador().getFullName());
        }
        
        if (catalogo.getUsuarioModificacion() != null) {
            dto.setUsuarioModificacionId(catalogo.getUsuarioModificacion().getId());
            dto.setUsuarioModificacionNombre(catalogo.getUsuarioModificacion().getFullName());
        }
        
        dto.setObservaciones(catalogo.getObservaciones());
        
        if (catalogo.getEstado() != null) {
            dto.setEstado(catalogo.getEstado().toString());
        }
        
        dto.setVersion(catalogo.getVersion());
        dto.setRutaArchivo(catalogo.getRutaArchivo());
        
        // Incluir detalles si existen
        if (catalogo.getDetalles() != null && !catalogo.getDetalles().isEmpty()) {
            dto.setDetalles(catalogo.getDetalles().stream()
                            .map(DetalleCatalogoTransferenciaMapper::toDTO)
                            .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Convierte un DTO a entidad CatalogoTransferencia
     * No incluye los campos que se establecen en el servidor (fechas, versión, etc.)
     */
    public static CatalogoTransferencia toEntity(CatalogoTransferenciaDTO dto) {
        if (dto == null) {
            return null;
        }
        
        CatalogoTransferencia catalogo = new CatalogoTransferencia();
        catalogo.setId(dto.getId());
        
        // Información General
        catalogo.setNombreEntidad(dto.getNombreEntidad());
        catalogo.setUnidadOrganizacion(dto.getUnidadOrganizacion());
        catalogo.setSeccion(dto.getSeccion());
        catalogo.setNivelDescripcion(dto.getNivelDescripcion());
        catalogo.setSerieDocumental(dto.getSerieDocumental());
        catalogo.setCodigoReferencia(dto.getCodigoReferencia());
        catalogo.setSoporte(dto.getSoporte());
        catalogo.setVolumenMetrosLineales(dto.getVolumenMetrosLineales());
        catalogo.setResponsableSeccion(dto.getResponsableSeccion());
        catalogo.setInventarioElaboradoPor(dto.getInventarioElaboradoPor());
        catalogo.setNumeroAnioRemision(dto.getNumeroAnioRemision());
        catalogo.setLugarFechaElaboracion(dto.getLugarFechaElaboracion());
        catalogo.setVistoBuenoResponsable(dto.getVistoBuenoResponsable());
        catalogo.setObservaciones(dto.getObservaciones());
        catalogo.setRutaArchivo(dto.getRutaArchivo());
        
        // Mapear detalles si existen
        if (dto.getDetalles() != null && !dto.getDetalles().isEmpty()) {
            dto.getDetalles().forEach(detalleDTO -> {
                DetalleCatalogoTransferencia detalle = DetalleCatalogoTransferenciaMapper.toEntity(detalleDTO);
                detalle.setCatalogoTransferencia(catalogo);
                catalogo.addDetalle(detalle);
            });
        }
        
        // Los campos autogenerados (fechas, versión) y relaciones (creador, usuario modificación)
        // se establecen en el servicio
        
        return catalogo;
    }
    
    /**
     * Convierte una lista de entidades a lista de DTOs
     */
    public static List<CatalogoTransferenciaDTO> toDTOList(List<CatalogoTransferencia> catalogos) {
        if (catalogos == null) {
            return List.of();
        }
        
        return catalogos.stream()
                .map(CatalogoTransferenciaMapper::toDTO)
                .collect(Collectors.toList());
    }
}
