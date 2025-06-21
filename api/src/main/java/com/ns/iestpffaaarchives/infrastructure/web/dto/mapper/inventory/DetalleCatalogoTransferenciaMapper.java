package com.ns.iestpffaaarchives.infrastructure.web.dto.mapper.inventory;

import com.ns.iestpffaaarchives.domain.entity.inventory.CatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleCatalogoTransferencia;
import com.ns.iestpffaaarchives.infrastructure.web.dto.inventory.DetalleCatalogoTransferenciaDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre DetalleCatalogoTransferencia y DetalleCatalogoTransferenciaDTO
 */
public class DetalleCatalogoTransferenciaMapper {

    /**
     * Convierte una entidad DetalleCatalogoTransferencia a DTO
     */
    public static DetalleCatalogoTransferenciaDTO toDTO(DetalleCatalogoTransferencia detalle) {
        if (detalle == null) {
            return null;
        }
        
        DetalleCatalogoTransferenciaDTO dto = new DetalleCatalogoTransferenciaDTO();
        dto.setId(detalle.getId());
        
        if (detalle.getCatalogoTransferencia() != null) {
            dto.setCatalogoId(detalle.getCatalogoTransferencia().getId());
        }
        
        dto.setNumeroItem(detalle.getNumeroItem());
        dto.setNumeroCaja(detalle.getNumeroCaja());
        dto.setNumeroTomoPaquete(detalle.getNumeroTomoPaquete());
        dto.setNumeroUnidadDocumental(detalle.getNumeroUnidadDocumental());
        dto.setFechaUnidadDocumental(detalle.getFechaUnidadDocumental());
        dto.setAlcanceContenido(detalle.getAlcanceContenido());
        dto.setInformacionAdicional(detalle.getInformacionAdicional());
        dto.setCantidadFolios(detalle.getCantidadFolios());
        dto.setObservaciones(detalle.getObservaciones());
        
        return dto;
    }
    
    /**
     * Convierte un DTO a entidad DetalleCatalogoTransferencia
     * Este método requiere que se establezca la relación con CatalogoTransferencia posteriormente
     */
    public static DetalleCatalogoTransferencia toEntity(DetalleCatalogoTransferenciaDTO dto) {
        if (dto == null) {
            return null;
        }
        
        DetalleCatalogoTransferencia detalle = new DetalleCatalogoTransferencia();
        detalle.setId(dto.getId());
        detalle.setNumeroItem(dto.getNumeroItem());
        detalle.setNumeroCaja(dto.getNumeroCaja());
        detalle.setNumeroTomoPaquete(dto.getNumeroTomoPaquete());
        detalle.setNumeroUnidadDocumental(dto.getNumeroUnidadDocumental());
        detalle.setFechaUnidadDocumental(dto.getFechaUnidadDocumental());
        detalle.setAlcanceContenido(dto.getAlcanceContenido());
        detalle.setInformacionAdicional(dto.getInformacionAdicional());
        detalle.setCantidadFolios(dto.getCantidadFolios());
        detalle.setObservaciones(dto.getObservaciones());
        
        // La relación con el catálogo debe establecerse por separado
        
        return detalle;
    }
    
    /**
     * Convierte una entidad DetalleCatalogoTransferencia a DTO con el catálogo especificado
     */
    public static DetalleCatalogoTransferencia toEntity(DetalleCatalogoTransferenciaDTO dto, CatalogoTransferencia catalogo) {
        DetalleCatalogoTransferencia detalle = toEntity(dto);
        if (detalle != null && catalogo != null) {
            detalle.setCatalogoTransferencia(catalogo);
        }
        return detalle;
    }
    
    /**
     * Convierte una lista de entidades a lista de DTOs
     */
    public static List<DetalleCatalogoTransferenciaDTO> toDTOList(List<DetalleCatalogoTransferencia> detalles) {
        if (detalles == null) {
            return List.of();
        }
        
        return detalles.stream()
                .map(DetalleCatalogoTransferenciaMapper::toDTO)
                .collect(Collectors.toList());
    }
}
