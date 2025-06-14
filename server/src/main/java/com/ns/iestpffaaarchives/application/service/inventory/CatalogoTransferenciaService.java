package com.ns.iestpffaaarchives.application.service.inventory;

import com.ns.iestpffaaarchives.domain.entity.inventory.CatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleCatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral.EstadoDocumento;
import com.ns.iestpffaaarchives.domain.repository.inventory.CatalogoTransferenciaRepository;
import com.ns.iestpffaaarchives.domain.repository.inventory.DetalleCatalogoTransferenciaRepository;
import com.ns.iestpffaaarchives.domain.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para la gestión de Catálogos de Transferencia y sus detalles
 */
@Service
public class CatalogoTransferenciaService {
    
    private static final Logger logger = LoggerFactory.getLogger(CatalogoTransferenciaService.class);
    
    private final CatalogoTransferenciaRepository catalogoTransferenciaRepository;
    private final DetalleCatalogoTransferenciaRepository detalleCatalogoTransferenciaRepository;
    
    @Autowired
    public CatalogoTransferenciaService(
            CatalogoTransferenciaRepository catalogoTransferenciaRepository,
            DetalleCatalogoTransferenciaRepository detalleCatalogoTransferenciaRepository) {
        this.catalogoTransferenciaRepository = catalogoTransferenciaRepository;
        this.detalleCatalogoTransferenciaRepository = detalleCatalogoTransferenciaRepository;
    }
    
    // Métodos de consulta
    
    /**
     * Obtiene todos los catálogos de transferencia
     * 
     * @return Lista de catálogos de transferencia
     */
    @Transactional(readOnly = true)
    public List<CatalogoTransferencia> getAllCatalogosTransferencia() {
        logger.info("Obteniendo todos los catálogos de transferencia");
        return catalogoTransferenciaRepository.findAll();
    }
    
    /**
     * Obtiene un catálogo de transferencia por su ID
     * 
     * @param id ID del catálogo de transferencia
     * @return Optional con el catálogo de transferencia si existe
     */
    @Transactional(readOnly = true)
    public Optional<CatalogoTransferencia> getCatalogoTransferenciaById(Long id) {
        logger.info("Buscando catálogo de transferencia con ID: {}", id);
        return catalogoTransferenciaRepository.findById(id);
    }
    
    /**
     * Búsqueda paginada de catálogos de transferencia
     * 
     * @param pageable Información de paginación
     * @return Página de catálogos de transferencia
     */
    @Transactional(readOnly = true)
    public Page<CatalogoTransferencia> getPaginatedCatalogosTransferencia(Pageable pageable) {
        logger.info("Obteniendo catálogos de transferencia paginados");
        return catalogoTransferenciaRepository.findAll(pageable);
    }
    
    /**
     * Busca catálogos de transferencia por estado
     * 
     * @param estado Estado del documento
     * @return Lista de catálogos con el estado especificado
     */
    @Transactional(readOnly = true)
    public List<CatalogoTransferencia> getCatalogosByEstado(EstadoDocumento estado) {
        logger.info("Buscando catálogos de transferencia con estado: {}", estado);
        return catalogoTransferenciaRepository.findByEstado(estado);
    }
    
    /**
     * Busca catálogos de transferencia por unidad de organización
     * 
     * @param unidadOrganizacion Unidad de organización
     * @return Lista de catálogos de la unidad especificada
     */
    @Transactional(readOnly = true)
    public List<CatalogoTransferencia> getCatalogosByUnidadOrganizacion(String unidadOrganizacion) {
        logger.info("Buscando catálogos de transferencia de la unidad: {}", unidadOrganizacion);
        return catalogoTransferenciaRepository.findByUnidadOrganizacion(unidadOrganizacion);
    }
    
    /**
     * Busca catálogos de transferencia por serie documental y soporte
     * 
     * @param serieDocumental Serie documental
     * @param soporte Soporte del documento
     * @return Lista de catálogos que coinciden con los criterios
     */
    @Transactional(readOnly = true)
    public List<CatalogoTransferencia> getCatalogosBySerieDocumentalAndSoporte(String serieDocumental, String soporte) {
        logger.info("Buscando catálogos por serie documental: {} y soporte: {}", serieDocumental, soporte);
        return catalogoTransferenciaRepository.findBySerieDocumentalAndSoporte(serieDocumental, soporte);
    }
    
    // Métodos de creación y actualización
    
    /**
     * Crea un nuevo catálogo de transferencia
     * 
     * @param catalogoTransferencia Catálogo a crear
     * @param currentUser Usuario que realiza la acción
     * @return Catálogo creado
     */
    @Transactional
    public CatalogoTransferencia createCatalogoTransferencia(CatalogoTransferencia catalogoTransferencia, User currentUser) {
        logger.info("Creando nuevo catálogo de transferencia para usuario: {}", currentUser.getUsername());
        
        // Establecer datos de auditoria
        catalogoTransferencia.setCreador(currentUser);
        
        // El método onCreate() en la entidad establecerá fecha de creación, versión y estado
        
        // Si la relación bidireccional ya está configurada correctamente, no es necesario
        // manipular la colección de detalles. Hibernate se encargará de persistir toda la
        // jerarquía gracias a la configuración cascade = CascadeType.ALL
        if (catalogoTransferencia.getDetalles() != null) {
            // Verificamos que cada detalle tenga la referencia al catálogo correctamente establecida
            for (DetalleCatalogoTransferencia detalle : catalogoTransferencia.getDetalles()) {
                if (detalle.getCatalogoTransferencia() == null) {
                    detalle.setCatalogoTransferencia(catalogoTransferencia);
                }
            }
        }
        
        // Guardar el catálogo con sus detalles en una sola operación
        CatalogoTransferencia savedCatalogo = catalogoTransferenciaRepository.save(catalogoTransferencia);
        
        return savedCatalogo;
    }
    
    /**
     * Actualiza un catálogo de transferencia existente
     * 
     * @param catalogoTransferencia Catálogo con los datos actualizados
     * @param currentUser Usuario que realiza la actualización
     * @return Catálogo actualizado
     */
    @Transactional
    public CatalogoTransferencia updateCatalogoTransferencia(CatalogoTransferencia catalogoTransferencia, User currentUser) {
        logger.info("Actualizando catálogo de transferencia ID: {} por usuario: {}", 
                 catalogoTransferencia.getId(), currentUser.getUsername());
        
        CatalogoTransferencia existingCatalogo = catalogoTransferenciaRepository.findById(catalogoTransferencia.getId())
            .orElseThrow(() -> new RuntimeException("Catálogo de transferencia no encontrado con ID: " + catalogoTransferencia.getId()));
        
        // Actualizar campos del catálogo
        if (catalogoTransferencia.getNombreEntidad() != null) {
            existingCatalogo.setNombreEntidad(catalogoTransferencia.getNombreEntidad());
        }
        if (catalogoTransferencia.getUnidadOrganizacion() != null) {
            existingCatalogo.setUnidadOrganizacion(catalogoTransferencia.getUnidadOrganizacion());
        }
        if (catalogoTransferencia.getSeccion() != null) {
            existingCatalogo.setSeccion(catalogoTransferencia.getSeccion());
        }
        if (catalogoTransferencia.getNivelDescripcion() != null) {
            existingCatalogo.setNivelDescripcion(catalogoTransferencia.getNivelDescripcion());
        }
        if (catalogoTransferencia.getSerieDocumental() != null) {
            existingCatalogo.setSerieDocumental(catalogoTransferencia.getSerieDocumental());
        }
        if (catalogoTransferencia.getCodigoReferencia() != null) {
            existingCatalogo.setCodigoReferencia(catalogoTransferencia.getCodigoReferencia());
        }
        if (catalogoTransferencia.getSoporte() != null) {
            existingCatalogo.setSoporte(catalogoTransferencia.getSoporte());
        }
        if (catalogoTransferencia.getVolumenMetrosLineales() != null) {
            existingCatalogo.setVolumenMetrosLineales(catalogoTransferencia.getVolumenMetrosLineales());
        }
        if (catalogoTransferencia.getResponsableSeccion() != null) {
            existingCatalogo.setResponsableSeccion(catalogoTransferencia.getResponsableSeccion());
        }
        if (catalogoTransferencia.getInventarioElaboradoPor() != null) {
            existingCatalogo.setInventarioElaboradoPor(catalogoTransferencia.getInventarioElaboradoPor());
        }
        if (catalogoTransferencia.getNumeroAnioRemision() != null) {
            existingCatalogo.setNumeroAnioRemision(catalogoTransferencia.getNumeroAnioRemision());
        }
        if (catalogoTransferencia.getLugarFechaElaboracion() != null) {
            existingCatalogo.setLugarFechaElaboracion(catalogoTransferencia.getLugarFechaElaboracion());
        }
        if (catalogoTransferencia.getVistoBuenoResponsable() != null) {
            existingCatalogo.setVistoBuenoResponsable(catalogoTransferencia.getVistoBuenoResponsable());
        }
        if (catalogoTransferencia.getObservaciones() != null) {
            existingCatalogo.setObservaciones(catalogoTransferencia.getObservaciones());
        }
        if (catalogoTransferencia.getEstado() != null) {
            existingCatalogo.setEstado(catalogoTransferencia.getEstado());
        }
        if (catalogoTransferencia.getRutaArchivo() != null) {
            existingCatalogo.setRutaArchivo(catalogoTransferencia.getRutaArchivo());
        }
        
        // Actualizar datos de auditoría
        existingCatalogo.setUsuarioModificacion(currentUser);
        // El método onUpdate() en la entidad actualizará la fecha de modificación y versión
        
        return catalogoTransferenciaRepository.save(existingCatalogo);
    }
    
    /**
     * Actualiza el estado de un catálogo de transferencia
     * 
     * @param id ID del catálogo
     * @param nuevoEstado Nuevo estado
     * @param currentUser Usuario que realiza el cambio
     * @return Catálogo actualizado
     */
    @Transactional
    public CatalogoTransferencia actualizarEstado(Long id, EstadoDocumento nuevoEstado, User currentUser) {
        logger.info("Actualizando estado del catálogo ID: {} a {}", id, nuevoEstado);
        
        CatalogoTransferencia catalogo = catalogoTransferenciaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catálogo de transferencia no encontrado con ID: " + id));
        
        catalogo.setEstado(nuevoEstado);
        catalogo.setUsuarioModificacion(currentUser);
        // El método onUpdate() en la entidad actualizará la fecha de modificación y versión
        
        return catalogoTransferenciaRepository.save(catalogo);
    }
    
    /**
     * Elimina un catálogo de transferencia (y sus detalles asociados)
     * 
     * @param id ID del catálogo a eliminar
     */
    @Transactional
    public void deleteCatalogoTransferencia(Long id) {
        logger.info("Eliminando catálogo de transferencia ID: {}", id);
        
        CatalogoTransferencia catalogo = catalogoTransferenciaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catálogo de transferencia no encontrado con ID: " + id));
        
        // Primero eliminar los detalles asociados
        detalleCatalogoTransferenciaRepository.deleteByCatalogoTransferencia(catalogo);
        
        // Luego eliminar el catálogo
        catalogoTransferenciaRepository.delete(catalogo);
    }
    
    // Métodos para gestionar los detalles del catálogo
    
    /**
     * Obtiene todos los detalles asociados a un catálogo de transferencia
     * 
     * @param catalogoId ID del catálogo
     * @return Lista de detalles asociados
     */
    @Transactional(readOnly = true)
    public List<DetalleCatalogoTransferencia> getDetallesByCatalogoId(Long catalogoId) {
        logger.info("Obteniendo detalles para el catálogo ID: {}", catalogoId);
        
        CatalogoTransferencia catalogo = catalogoTransferenciaRepository.findById(catalogoId)
            .orElseThrow(() -> new RuntimeException("Catálogo de transferencia no encontrado con ID: " + catalogoId));
            
        return detalleCatalogoTransferenciaRepository.findByCatalogoTransferencia(catalogo);
    }
    
    /**
     * Agrega un nuevo detalle a un catálogo de transferencia
     * 
     * @param catalogoId ID del catálogo
     * @param detalle Detalle a agregar
     * @return Detalle guardado
     */
    @Transactional
    public DetalleCatalogoTransferencia addDetalleToCatalogo(Long catalogoId, DetalleCatalogoTransferencia detalle) {
        logger.info("Agregando nuevo detalle al catálogo ID: {}", catalogoId);
        
        CatalogoTransferencia catalogo = catalogoTransferenciaRepository.findById(catalogoId)
            .orElseThrow(() -> new RuntimeException("Catálogo de transferencia no encontrado con ID: " + catalogoId));
        
        // Establecer la relación con el catálogo
        detalle.setCatalogoTransferencia(catalogo);
        
        // Calcular número de item si no está definido
        if (detalle.getNumeroItem() == null) {
            // Obtener el número de item más alto y agregar 1
            List<DetalleCatalogoTransferencia> detalles = detalleCatalogoTransferenciaRepository.findByCatalogoTransferencia(catalogo);
            int maxItem = 0;
            
            for (DetalleCatalogoTransferencia det : detalles) {
                if (det.getNumeroItem() != null && det.getNumeroItem() > maxItem) {
                    maxItem = det.getNumeroItem();
                }
            }
            
            detalle.setNumeroItem(maxItem + 1);
        }
        
        return detalleCatalogoTransferenciaRepository.save(detalle);
    }
    
    /**
     * Actualiza un detalle existente de un catálogo de transferencia
     * 
     * @param detalleId ID del detalle a actualizar
     * @param detalle Nuevos datos del detalle
     * @return Detalle actualizado
     */
    @Transactional
    public DetalleCatalogoTransferencia updateDetalle(Long detalleId, DetalleCatalogoTransferencia detalle) {
        logger.info("Actualizando detalle ID: {}", detalleId);
        
        DetalleCatalogoTransferencia existingDetalle = detalleCatalogoTransferenciaRepository.findById(detalleId)
            .orElseThrow(() -> new RuntimeException("Detalle de catálogo no encontrado con ID: " + detalleId));
        
        // Actualizar campos del detalle
        if (detalle.getNumeroCaja() != null) {
            existingDetalle.setNumeroCaja(detalle.getNumeroCaja());
        }
        if (detalle.getNumeroTomoPaquete() != null) {
            existingDetalle.setNumeroTomoPaquete(detalle.getNumeroTomoPaquete());
        }
        if (detalle.getNumeroUnidadDocumental() != null) {
            existingDetalle.setNumeroUnidadDocumental(detalle.getNumeroUnidadDocumental());
        }
        if (detalle.getFechaUnidadDocumental() != null) {
            existingDetalle.setFechaUnidadDocumental(detalle.getFechaUnidadDocumental());
        }
        if (detalle.getAlcanceContenido() != null) {
            existingDetalle.setAlcanceContenido(detalle.getAlcanceContenido());
        }
        if (detalle.getInformacionAdicional() != null) {
            existingDetalle.setInformacionAdicional(detalle.getInformacionAdicional());
        }
        if (detalle.getCantidadFolios() != null) {
            existingDetalle.setCantidadFolios(detalle.getCantidadFolios());
        }
        if (detalle.getObservaciones() != null) {
            existingDetalle.setObservaciones(detalle.getObservaciones());
        }
        if (detalle.getNumeroItem() != null) {
            existingDetalle.setNumeroItem(detalle.getNumeroItem());
        }
        
        return detalleCatalogoTransferenciaRepository.save(existingDetalle);
    }
    
    /**
     * Elimina un detalle específico de un catálogo
     * 
     * @param detalleId ID del detalle a eliminar
     */
    @Transactional
    public void deleteDetalle(Long detalleId) {
        logger.info("Eliminando detalle ID: {}", detalleId);
        
        // Verificar si existe el detalle antes de intentar eliminarlo
        if (!detalleCatalogoTransferenciaRepository.existsById(detalleId)) {
            throw new RuntimeException("Detalle de catálogo no encontrado con ID: " + detalleId);
        }
        
        detalleCatalogoTransferenciaRepository.deleteById(detalleId);
    }
    
    /**
     * Agrega múltiples detalles a un catálogo de transferencia
     * 
     * @param catalogoId ID del catálogo
     * @param detalles Lista de detalles a agregar
     * @return Lista de detalles guardados
     */
    @Transactional
    public List<DetalleCatalogoTransferencia> addMultiplesDetalles(Long catalogoId, List<DetalleCatalogoTransferencia> detalles) {
        logger.info("Agregando {} detalles al catálogo ID: {}", detalles.size(), catalogoId);
        
        CatalogoTransferencia catalogo = catalogoTransferenciaRepository.findById(catalogoId)
            .orElseThrow(() -> new RuntimeException("Catálogo de transferencia no encontrado con ID: " + catalogoId));
        
        // Obtener el número de item más alto para asignar nuevos items
        List<DetalleCatalogoTransferencia> existingDetalles = detalleCatalogoTransferenciaRepository.findByCatalogoTransferencia(catalogo);
        int maxItem = 0;
        
        for (DetalleCatalogoTransferencia det : existingDetalles) {
            if (det.getNumeroItem() != null && det.getNumeroItem() > maxItem) {
                maxItem = det.getNumeroItem();
            }
        }
        
        // Procesar y guardar cada detalle
        for (int i = 0; i < detalles.size(); i++) {
            DetalleCatalogoTransferencia detalle = detalles.get(i);
            detalle.setCatalogoTransferencia(catalogo);
            
            // Asignar número de item si no está definido
            if (detalle.getNumeroItem() == null) {
                detalle.setNumeroItem(maxItem + i + 1);
            }
        }
        
        return detalleCatalogoTransferenciaRepository.saveAll(detalles);
    }
}
