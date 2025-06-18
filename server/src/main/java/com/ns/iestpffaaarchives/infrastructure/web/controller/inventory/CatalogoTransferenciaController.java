package com.ns.iestpffaaarchives.infrastructure.web.controller.inventory;

import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.application.service.inventory.CatalogoTransferenciaService;
import com.ns.iestpffaaarchives.application.service.export.CatalogoTransferenciaExportService;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.repository.inventory.DetalleCatalogoTransferenciaRepository;
import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral.EstadoDocumento;
import com.ns.iestpffaaarchives.domain.entity.inventory.CatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleCatalogoTransferencia;
import com.ns.iestpffaaarchives.infrastructure.web.dto.inventory.CatalogoTransferenciaDTO;
import com.ns.iestpffaaarchives.infrastructure.web.dto.inventory.DetalleCatalogoTransferenciaDTO;
import com.ns.iestpffaaarchives.infrastructure.web.dto.mapper.inventory.CatalogoTransferenciaMapper;
import com.ns.iestpffaaarchives.infrastructure.web.dto.mapper.inventory.DetalleCatalogoTransferenciaMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Controlador REST para gestionar Catálogos de Transferencia
 */
@RestController
@RequestMapping("/api/catalogo-transferencia")
public class CatalogoTransferenciaController {

    private static final Logger logger = LoggerFactory.getLogger(CatalogoTransferenciaController.class);
    
    private final CatalogoTransferenciaService catalogoTransferenciaService;
    private final UserService userService;
    private final DetalleCatalogoTransferenciaRepository detalleCatalogoTransferenciaRepository;
    private final CatalogoTransferenciaExportService excelExportService;
    
    @Autowired
    public CatalogoTransferenciaController(CatalogoTransferenciaService catalogoTransferenciaService, 
                                          UserService userService, 
                                          DetalleCatalogoTransferenciaRepository detalleCatalogoTransferenciaRepository,
                                          CatalogoTransferenciaExportService excelExportService) {
        this.catalogoTransferenciaService = catalogoTransferenciaService;
        this.userService = userService;
        this.detalleCatalogoTransferenciaRepository = detalleCatalogoTransferenciaRepository;
        this.excelExportService = excelExportService;
    }
    
    /**
     * Obtiene todos los catálogos de transferencia
     * @return Lista de catálogos
     */
    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<CatalogoTransferenciaDTO>> getAllCatalogos() {
        try {
            List<CatalogoTransferencia> catalogos = catalogoTransferenciaService.getAllCatalogosTransferencia();
            if (catalogos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(CatalogoTransferenciaMapper.toDTOList(catalogos));
        } catch (Exception e) {
            logger.error("Error al obtener todos los catálogos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /**
     * Obtiene un catálogo por su ID
     * @param id ID del catálogo
     * @return El catálogo si existe
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<CatalogoTransferenciaDTO> getCatalogoById(@PathVariable Long id) {
        try {
            Optional<CatalogoTransferencia> catalogo = catalogoTransferenciaService.getCatalogoTransferenciaById(id);
            return catalogo.map(c -> ResponseEntity.ok(CatalogoTransferenciaMapper.toDTO(c)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error al obtener catálogo con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Busca catálogos por varios criterios
     * @param unidad Unidad/organización
     * @param titulo Título o término de búsqueda
     * @param estado Estado del documento
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortBy Campo para ordenar
     * @param direction Dirección de ordenamiento
     * @return Página de catálogos que coinciden con los criterios
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<Page<CatalogoTransferenciaDTO>> searchCatalogos(
            @RequestParam(required = false) String unidad,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            page = Math.max(0, page);
            size = Math.max(1, Math.min(100, size));
            
            Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
                
            Pageable pageable = PageRequest.of(page, size, sort);
            
            List<CatalogoTransferencia> resultsList = catalogoTransferenciaService.getCatalogosByUnidadOrganizacion(unidad);
            Page<CatalogoTransferencia> results = new PageImpl<>(resultsList, pageable, resultsList.size());
                
            Page<CatalogoTransferenciaDTO> dtoPage = results.map(CatalogoTransferenciaMapper::toDTO);
            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            logger.error("Error en búsqueda de catálogos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene todos los detalles de un catálogo
     * @param catalogoId ID del catálogo
     * @return Lista de detalles
     */
    @GetMapping("/{catalogoId}/detalles")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<DetalleCatalogoTransferenciaDTO>> getDetallesByCatalogo(
            @PathVariable Long catalogoId) {
        try {
            List<DetalleCatalogoTransferencia> detalles = catalogoTransferenciaService.getDetallesByCatalogoId(catalogoId);
            if (detalles.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(DetalleCatalogoTransferenciaMapper.toDTOList(detalles));
        } catch (Exception e) {
            logger.error("Error al obtener detalles del catálogo ID {}: {}", catalogoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene un detalle específico de un catálogo
     * @param catalogoId ID del catálogo
     * @param detalleId ID del detalle
     * @return El detalle si existe
     */
    @GetMapping("/{catalogoId}/detalles/{detalleId}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<DetalleCatalogoTransferenciaDTO> getDetalleById(
            @PathVariable Long catalogoId, 
            @PathVariable Long detalleId) {
        try {
            Optional<CatalogoTransferencia> catalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(catalogoId);
            if (catalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Obtener detalle por ID desde el repositorio
            Optional<DetalleCatalogoTransferencia> detalleOpt = detalleCatalogoTransferenciaRepository.findById(detalleId);
            if (detalleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            DetalleCatalogoTransferencia detalle = detalleOpt.get();
            // Verificar que el detalle pertenece al catálogo especificado
            if (!detalle.getCatalogoTransferencia().getId().equals(catalogoId)) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(DetalleCatalogoTransferenciaMapper.toDTO(detalle));
        } catch (Exception e) {
            logger.error("Error al obtener detalle ID {} del catálogo ID {}: {}", 
                         detalleId, catalogoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Crea un nuevo catálogo de transferencia
     * @param catalogoDTO DTO con la información del catálogo
     * @return El catálogo creado
     */
    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENT_CREATE')")
    public ResponseEntity<?> createCatalogo(@RequestBody CatalogoTransferenciaDTO catalogoDTO) {
        try {
            logger.info("Creando nuevo catálogo de transferencia");
            
            // Validaciones básicas
            if (catalogoDTO.getNombreEntidad() == null || catalogoDTO.getNombreEntidad().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre de entidad es obligatorio");
            }
            
            // Obtener usuario actual para auditoria
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }
            
            CatalogoTransferencia catalogo = CatalogoTransferenciaMapper.toEntity(catalogoDTO);
            
            // Establecer usuario creador
            catalogo.setCreador(currentUser);
            
            CatalogoTransferencia savedCatalogo = catalogoTransferenciaService.createCatalogoTransferencia(catalogo, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(CatalogoTransferenciaMapper.toDTO(savedCatalogo));
        } catch (Exception e) {
            logger.error("Error al crear catálogo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear catálogo: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza un catálogo existente
     * @param id ID del catálogo a actualizar
     * @param catalogoDTO Datos actualizados
     * @return El catálogo actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<?> updateCatalogo(@PathVariable Long id, @RequestBody CatalogoTransferenciaDTO catalogoDTO) {
        try {
            logger.info("Actualizando catálogo ID: {}", id);
            
            Optional<CatalogoTransferencia> existingCatalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(id);
            if (existingCatalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Obtener usuario actual para auditoria
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }
            
            CatalogoTransferencia existingCatalogo = existingCatalogoOpt.get();
            CatalogoTransferencia catalogoToUpdate = CatalogoTransferenciaMapper.toEntity(catalogoDTO);
            catalogoToUpdate.setId(id);
            
            // Mantener información que no debe cambiar
            catalogoToUpdate.setFechaCreacion(existingCatalogo.getFechaCreacion());
            catalogoToUpdate.setCreador(existingCatalogo.getCreador());
            catalogoToUpdate.setVersion(existingCatalogo.getVersion());
            
            // Actualizar usuario de modificación
            catalogoToUpdate.setUsuarioModificacion(currentUser);
            
            CatalogoTransferencia updatedCatalogo = catalogoTransferenciaService.updateCatalogoTransferencia(catalogoToUpdate, currentUser);
            return ResponseEntity.ok(CatalogoTransferenciaMapper.toDTO(updatedCatalogo));
        } catch (Exception e) {
            logger.error("Error al actualizar catálogo ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar catálogo: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza parcialmente un catálogo
     * @param id ID del catálogo
     * @param catalogoDTO Datos parciales del catálogo
     * @return El catálogo actualizado
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<?> patchCatalogo(@PathVariable Long id, @RequestBody CatalogoTransferenciaDTO catalogoDTO) {
        try {
            logger.info("Actualizando parcialmente catálogo ID: {}", id);
            
            Optional<CatalogoTransferencia> existingCatalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(id);
            if (existingCatalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Obtener usuario actual para auditoria
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }
            
            CatalogoTransferencia existingCatalogo = existingCatalogoOpt.get();
            
            // Actualizar solo los campos recibidos que no sean nulos
            if (catalogoDTO.getNombreEntidad() != null) {
                existingCatalogo.setNombreEntidad(catalogoDTO.getNombreEntidad());
            }
            if (catalogoDTO.getUnidadOrganizacion() != null) {
                existingCatalogo.setUnidadOrganizacion(catalogoDTO.getUnidadOrganizacion());
            }
            if (catalogoDTO.getSeccion() != null) {
                existingCatalogo.setSeccion(catalogoDTO.getSeccion());
            }
            if (catalogoDTO.getNivelDescripcion() != null) {
                existingCatalogo.setNivelDescripcion(catalogoDTO.getNivelDescripcion());
            }
            if (catalogoDTO.getSerieDocumental() != null) {
                existingCatalogo.setSerieDocumental(catalogoDTO.getSerieDocumental());
            }
            if (catalogoDTO.getCodigoReferencia() != null) {
                existingCatalogo.setCodigoReferencia(catalogoDTO.getCodigoReferencia());
            }
            if (catalogoDTO.getSoporte() != null) {
                existingCatalogo.setSoporte(catalogoDTO.getSoporte());
            }
            if (catalogoDTO.getVolumenMetrosLineales() != null) {
                existingCatalogo.setVolumenMetrosLineales(catalogoDTO.getVolumenMetrosLineales());
            }
            if (catalogoDTO.getResponsableSeccion() != null) {
                existingCatalogo.setResponsableSeccion(catalogoDTO.getResponsableSeccion());
            }
            if (catalogoDTO.getInventarioElaboradoPor() != null) {
                existingCatalogo.setInventarioElaboradoPor(catalogoDTO.getInventarioElaboradoPor());
            }
            if (catalogoDTO.getNumeroAnioRemision() != null) {
                existingCatalogo.setNumeroAnioRemision(catalogoDTO.getNumeroAnioRemision());
            }
            if (catalogoDTO.getLugarFechaElaboracion() != null) {
                existingCatalogo.setLugarFechaElaboracion(catalogoDTO.getLugarFechaElaboracion());
            }
            if (catalogoDTO.getVistoBuenoResponsable() != null) {
                existingCatalogo.setVistoBuenoResponsable(catalogoDTO.getVistoBuenoResponsable());
            }
            if (catalogoDTO.getObservaciones() != null) {
                existingCatalogo.setObservaciones(catalogoDTO.getObservaciones());
            }
            if (catalogoDTO.getRutaArchivo() != null) {
                existingCatalogo.setRutaArchivo(catalogoDTO.getRutaArchivo());
            }
            
            // Actualizar usuario de modificación
            existingCatalogo.setUsuarioModificacion(currentUser);
            
            CatalogoTransferencia updatedCatalogo = catalogoTransferenciaService.updateCatalogoTransferencia(existingCatalogo, currentUser);
            return ResponseEntity.ok(CatalogoTransferenciaMapper.toDTO(updatedCatalogo));
        } catch (Exception e) {
            logger.error("Error al actualizar parcialmente catálogo ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar catálogo: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza el estado de un catálogo
     * @param id ID del catálogo
     * @param estado Nuevo estado
     * @return El catálogo actualizado
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<?> updateEstadoCatalogo(
            @PathVariable Long id, 
            @RequestParam String estado) {
        try {
            logger.info("Actualizando estado del catálogo ID: {} a: {}", id, estado);
            
            Optional<CatalogoTransferencia> catalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(id);
            if (catalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Obtener usuario actual para auditoria
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }
            
            EstadoDocumento estadoDocumento = EstadoDocumento.valueOf(estado);
            CatalogoTransferencia updatedCatalogo = catalogoTransferenciaService.actualizarEstado(id, estadoDocumento, currentUser);
            return ResponseEntity.ok(CatalogoTransferenciaMapper.toDTO(updatedCatalogo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al actualizar estado del catálogo ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar estado del catálogo: " + e.getMessage());
        }
    }
    
    /**
     * Elimina un catálogo de transferencia
     * @param id ID del catálogo a eliminar
     * @return Respuesta sin contenido si se eliminó correctamente
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    public ResponseEntity<?> deleteCatalogo(@PathVariable Long id) {
        try {
            logger.info("Eliminando catálogo ID: {}", id);
            
            Optional<CatalogoTransferencia> catalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(id);
            if (catalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            catalogoTransferenciaService.deleteCatalogoTransferencia(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error al eliminar catálogo ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar catálogo: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el usuario actual autenticado
     * @return Usuario actual o null si no está autenticado
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return userService.getUserByUsername(authentication.getName()).orElse(null);
        }
        return null;
    }
    
    /**
     * Añade un nuevo detalle a un catálogo existente
     * @param catalogoId ID del catálogo
     * @param detalleDTO Datos del detalle
     * @return El detalle creado
     */
    @PostMapping("/{catalogoId}/detalles")
    @PreAuthorize("hasAuthority('DOCUMENT_CREATE')")
    public ResponseEntity<?> addDetalle(@PathVariable Long catalogoId, 
                                      @RequestBody DetalleCatalogoTransferenciaDTO detalleDTO) {
        try {
            logger.info("Añadiendo nuevo detalle al catálogo ID: {}", catalogoId);
            
            Optional<CatalogoTransferencia> catalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(catalogoId);
            if (catalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            DetalleCatalogoTransferencia detalle = DetalleCatalogoTransferenciaMapper.toEntity(detalleDTO);
            detalle.setCatalogoTransferencia(catalogoOpt.get());
            
            // Si no se especificó el número de item, se asignará automáticamente
            if (detalle.getNumeroItem() == null) {
                detalle.setNumeroItem(0); // El servicio lo asignará automáticamente
            }
            
            DetalleCatalogoTransferencia savedDetalle = catalogoTransferenciaService.addDetalleToCatalogo(catalogoId, detalle);
            return ResponseEntity.status(HttpStatus.CREATED).body(DetalleCatalogoTransferenciaMapper.toDTO(savedDetalle));
        } catch (Exception e) {
            logger.error("Error al añadir detalle al catálogo ID {}: {}", catalogoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al añadir detalle: " + e.getMessage());
        }
    }
    
    /**
     * Añade múltiples detalles a un catálogo existente
     * @param catalogoId ID del catálogo
     * @param detallesDTO Lista de detalles a añadir
     * @return Lista de detalles creados
     */
    @PostMapping("/{catalogoId}/detalles/batch")
    @PreAuthorize("hasAuthority('DOCUMENT_CREATE')")
    public ResponseEntity<?> addDetallesBatch(@PathVariable Long catalogoId, 
                                           @RequestBody List<DetalleCatalogoTransferenciaDTO> detallesDTO) {
        try {
            logger.info("Añadiendo {} detalles al catálogo ID: {}", detallesDTO.size(), catalogoId);
            
            Optional<CatalogoTransferencia> catalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(catalogoId);
            if (catalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<DetalleCatalogoTransferencia> detalles = new ArrayList<>();
            for(DetalleCatalogoTransferenciaDTO dto : detallesDTO) {
                DetalleCatalogoTransferencia detalle = DetalleCatalogoTransferenciaMapper.toEntity(dto);
                detalle.setCatalogoTransferencia(catalogoOpt.get());
                detalles.add(detalle);
            }
            
            List<DetalleCatalogoTransferencia> savedDetalles = catalogoTransferenciaService.addMultiplesDetalles(
                catalogoId, detalles);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DetalleCatalogoTransferenciaMapper.toDTOList(savedDetalles));
        } catch (Exception e) {
            logger.error("Error al añadir detalles en lote al catálogo ID {}: {}", catalogoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al añadir detalles en lote: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza un detalle existente
     * @param catalogoId ID del catálogo
     * @param detalleId ID del detalle
     * @param detalleDTO Datos actualizados del detalle
     * @return El detalle actualizado
     */
    @PutMapping("/{catalogoId}/detalles/{detalleId}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<?> updateDetalle(@PathVariable Long catalogoId, 
                                        @PathVariable Long detalleId,
                                        @RequestBody DetalleCatalogoTransferenciaDTO detalleDTO) {
        try {
            logger.info("Actualizando detalle ID: {} del catálogo ID: {}", detalleId, catalogoId);
            
            // Verificar que el catálogo existe
            Optional<CatalogoTransferencia> catalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(catalogoId);
            if (catalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el detalle existe
            Optional<DetalleCatalogoTransferencia> detalleOpt = detalleCatalogoTransferenciaRepository.findById(detalleId);
            if (detalleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el detalle pertenece al catálogo
            DetalleCatalogoTransferencia existingDetalle = detalleOpt.get();
            if (!existingDetalle.getCatalogoTransferencia().getId().equals(catalogoId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El detalle no pertenece al catálogo especificado");
            }
            
            // Convertir DTO a entidad y establecer el ID y la relación con el catálogo
            DetalleCatalogoTransferencia detalle = DetalleCatalogoTransferenciaMapper.toEntity(detalleDTO);
            detalle.setId(detalleId);
            detalle.setCatalogoTransferencia(catalogoOpt.get());
            
            DetalleCatalogoTransferencia updatedDetalle = catalogoTransferenciaService.updateDetalle(detalleId, detalle);
            return ResponseEntity.ok(DetalleCatalogoTransferenciaMapper.toDTO(updatedDetalle));
        } catch (Exception e) {
            logger.error("Error al actualizar detalle ID {} del catálogo ID {}: {}", 
                         detalleId, catalogoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar detalle: " + e.getMessage());
        }
    }
    
    /**
     * Elimina un detalle de un catálogo
     * @param catalogoId ID del catálogo
     * @param detalleId ID del detalle a eliminar
     * @return Respuesta sin contenido si se eliminó correctamente
     */
    @DeleteMapping("/{catalogoId}/detalles/{detalleId}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    public ResponseEntity<?> deleteDetalle(@PathVariable Long catalogoId, @PathVariable Long detalleId) {
        try {
            logger.info("Eliminando detalle ID: {} del catálogo ID: {}", detalleId, catalogoId);
            
            // Verificar que el catálogo existe
            Optional<CatalogoTransferencia> catalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(catalogoId);
            if (catalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el detalle existe
            Optional<DetalleCatalogoTransferencia> detalleOpt = detalleCatalogoTransferenciaRepository.findById(detalleId);
            if (detalleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el detalle pertenece al catálogo
            DetalleCatalogoTransferencia detalle = detalleOpt.get();
            if (!detalle.getCatalogoTransferencia().getId().equals(catalogoId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El detalle no pertenece al catálogo especificado");
            }
            
            catalogoTransferenciaService.deleteDetalle(detalleId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error al eliminar detalle ID {} del catálogo ID {}: {}", 
                         detalleId, catalogoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar detalle: " + e.getMessage());
        }
    }
    
    /**
     * Exporta un catálogo de transferencia y sus detalles a un archivo Excel
     * @param id ID del catálogo a exportar
     * @return Archivo Excel como arreglo de bytes
     */
    @GetMapping("/{id}/export-excel")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<?> exportCatalogoToExcel(@PathVariable Long id) {
        try {
            logger.info("Exportando a Excel el catálogo ID: {}", id);
            
            // Verificar que el catálogo existe
            Optional<CatalogoTransferencia> catalogoOpt = catalogoTransferenciaService.getCatalogoTransferenciaById(id);
            if (catalogoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CatalogoTransferencia catalogo = catalogoOpt.get();
            
            // Generar el archivo Excel
            byte[] excelBytes = excelExportService.exportar(catalogo);
            
            // Construir el nombre del archivo
            String filename = "catalogo_transferencia_" + id + "_" + System.currentTimeMillis() + ".xlsx";
            
            // Retornar el archivo como respuesta para descarga
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(excelBytes);
        } catch (IOException e) {
            logger.error("Error al generar el archivo Excel para el catálogo ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el archivo Excel: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al exportar catálogo ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al exportar catálogo: " + e.getMessage());
        }
    }
}
