// InventarioController.java
package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.InventarioService;
import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.domain.entity.inventory.*;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.infrastructure.web.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventarios")
public class InventarioController {
    
    private final InventarioService inventarioService;
    private final UserService userService;
    
    @Autowired
    public InventarioController(InventarioService inventarioService, UserService userService) {
        this.inventarioService = inventarioService;
        this.userService = userService;
    }
    
    // Inventario General
    @PostMapping("/general")
    @PreAuthorize("hasAuthority('DOCUMENT_CREATE')")
    public ResponseEntity<InventarioGeneralDTO> crearInventarioGeneral(@RequestBody InventarioGeneralDTO dto) {
        User creador = userService.getCurrentUser();
        
        InventarioGeneral inventario = new InventarioGeneral();
        // Información básica
        inventario.setTitulo(dto.getTitulo());
        inventario.setUnidadAdministrativa(dto.getUnidadAdministrativa());
        
        // Información según el Anexo N° 04
        inventario.setNumeroAnioRemision(dto.getNumeroAnioRemision());
        inventario.setSeccion(dto.getSeccion());
        inventario.setFechaTransferencia(dto.getFechaTransferencia());
        // Usamos la propiedad correcta del DTO
        inventario.setTotalVolumen(dto.getTotalVolumen());
        inventario.setLugarFechaEntrega(dto.getLugarFechaEntrega());
        inventario.setLugarFechaRecepcion(dto.getLugarFechaRecepcion());
        inventario.setFirmaSelloAutoridadEntrega(dto.getFirmaSelloAutoridadEntrega());
        inventario.setFirmaSelloAutoridadRecibe(dto.getFirmaSelloAutoridadRecibe());
        
        // Campos complementarios
        inventario.setFechaInicial(dto.getFechaInicial());
        inventario.setFechaFinal(dto.getFechaFinal());
        inventario.setEstadoConservacion(dto.getEstadoConservacion());
        inventario.setObservaciones(dto.getObservaciones());
        inventario.setCreador(creador);
        
        // Estado del documento
        if (dto.getEstado() != null) {
            inventario.setEstado(dto.getEstado());
        } else {
            inventario.setEstado(InventarioGeneral.EstadoDocumento.BORRADOR);
        }
        
        InventarioGeneral saved = inventarioService.crearInventarioGeneral(inventario, dto.getDocumentoIds());
        
        return ResponseEntity.ok(mapToDTO(saved));
    }
    
    /**
     * Mapea una entidad InventarioGeneral a su correspondiente DTO
     */
    private InventarioGeneralDTO mapToDTO(InventarioGeneral inventario) {
        InventarioGeneralDTO dto = new InventarioGeneralDTO();
        // Información básica
        dto.setId(inventario.getId());
        dto.setTitulo(inventario.getTitulo());
        dto.setUnidadAdministrativa(inventario.getUnidadAdministrativa());
        
        // Información según el Anexo N° 04
        dto.setNumeroAnioRemision(inventario.getNumeroAnioRemision());
        dto.setSeccion(inventario.getSeccion());
        dto.setFechaTransferencia(inventario.getFechaTransferencia());
        dto.setTotalVolumen(inventario.getTotalVolumen());
        dto.setLugarFechaEntrega(inventario.getLugarFechaEntrega());
        dto.setLugarFechaRecepcion(inventario.getLugarFechaRecepcion());
        dto.setFirmaSelloAutoridadEntrega(inventario.getFirmaSelloAutoridadEntrega());
        dto.setFirmaSelloAutoridadRecibe(inventario.getFirmaSelloAutoridadRecibe());
        
        // Campos de control y fechas
        dto.setFechaCreacion(inventario.getFechaCreacion());
        dto.setFechaModificacion(inventario.getFechaModificacion());
        dto.setFechaInicial(inventario.getFechaInicial());
        dto.setFechaFinal(inventario.getFechaFinal());
        dto.setEstadoConservacion(inventario.getEstadoConservacion());
        dto.setObservaciones(inventario.getObservaciones());
        dto.setEstado(inventario.getEstado());
        dto.setVersion(inventario.getVersion());
        
        // Información de usuarios
        if (inventario.getCreador() != null) {
            dto.setCreadorId(inventario.getCreador().getId());
            dto.setCreadorNombre(inventario.getCreador().getUsername());
        }
        
        if (inventario.getUsuarioModificacion() != null) {
            dto.setUsuarioModificacionId(inventario.getUsuarioModificacion().getId());
            dto.setUsuarioModificacionNombre(inventario.getUsuarioModificacion().getUsername());
        }
        
        // Detalles
        if (inventario.getDetalles() != null && !inventario.getDetalles().isEmpty()) {
            dto.setDetalles(inventario.getDetalles().stream()
                    .map(this::mapDetalleToDTO)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Mapea una entidad DetalleInventarioGeneral a su correspondiente DTO
     */
    private DetalleInventarioGeneralDTO mapDetalleToDTO(DetalleInventarioGeneral detalle) {
        DetalleInventarioGeneralDTO dto = new DetalleInventarioGeneralDTO();
        dto.setId(detalle.getId());
        dto.setInventarioGeneralId(detalle.getInventarioGeneral().getId());
        
        if (detalle.getDocumento() != null) {
            dto.setDocumentoId(detalle.getDocumento().getId());
            dto.setDocumentoNombre(detalle.getDocumento().getTitle());
            dto.setDocumentoTipo(detalle.getDocumento().getType() != null ? 
                    detalle.getDocumento().getType().getName() : "");
        }
        
        // Atributos del Ítem según Anexo N° 04
        dto.setNumeroItem(detalle.getNumeroItem());
        dto.setSerieDocumental(detalle.getSerieDocumental());
        dto.setFechaExtremaDel(detalle.getFechaExtremaDel());
        dto.setFechaExtremaAl(detalle.getFechaExtremaAl());
        dto.setTipoUnidadArchivamiento(detalle.getTipoUnidadArchivamiento());
        dto.setCantidadUnidadArchivamiento(detalle.getCantidadUnidadArchivamiento());
        dto.setVolumenMetrosLineales(detalle.getVolumenMetrosLineales());
        dto.setSoporte(detalle.getSoporte());
        dto.setObservaciones(detalle.getObservaciones());
        dto.setDescripcion(detalle.getDescripcion());
        
        return dto;
    }
    
    @GetMapping("/general")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<InventarioGeneralDTO>> getAllInventariosGenerales() {
        List<InventarioGeneral> inventarios = inventarioService.getAllInventariosGenerales();
        List<InventarioGeneralDTO> dtos = inventarios.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/general/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<InventarioGeneralDTO> getInventarioGeneralById(@PathVariable Long id) {
        InventarioGeneral inventario = inventarioService.getInventarioGeneralById(id);
        return ResponseEntity.ok(mapToDTO(inventario));
    }
    
    @GetMapping("/general/{id}/download")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<Resource> downloadInventarioGeneral(@PathVariable Long id) {
        InventarioGeneral inventario = inventarioService.getInventarioGeneralById(id);
        
        try {
            Path filePath = Paths.get(inventario.getRutaArchivo());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(resource);
            } else {
                throw new RuntimeException("No se puede leer el archivo");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al descargar el archivo", e);
        }
    }
    
    @GetMapping("/general/{id}/pdf")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<byte[]> downloadInventarioGeneralPDF(@PathVariable Long id) {
        byte[] pdfBytes = inventarioService.exportarInventarioGeneralPDF(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("InventarioGeneral_" + id + ".pdf").build());
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    @GetMapping("/general/{id}/excel")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<byte[]> downloadInventarioGeneralExcel(@PathVariable Long id) {
        byte[] excelBytes = inventarioService.exportarInventarioGeneralExcel(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("InventarioGeneral_" + id + ".xlsx").build());
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
    
    @PutMapping("/general/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<InventarioGeneralDTO> updateInventarioGeneral(
            @PathVariable Long id, 
            @RequestBody InventarioGeneralDTO dto) {
        // Verificar que el ID en la URL coincida con el ID en el cuerpo
        if (!id.equals(dto.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Obtener el inventario existente
        InventarioGeneral inventarioExistente = inventarioService.getInventarioGeneralById(id);
        User usuarioModificacion = userService.getCurrentUser();
        
        // Actualizar los campos modificables
        inventarioExistente.setTitulo(dto.getTitulo());
        inventarioExistente.setUnidadAdministrativa(dto.getUnidadAdministrativa());
        inventarioExistente.setNumeroAnioRemision(dto.getNumeroAnioRemision());
        inventarioExistente.setSeccion(dto.getSeccion());
        inventarioExistente.setFechaTransferencia(dto.getFechaTransferencia());
        inventarioExistente.setTotalVolumen(dto.getTotalVolumen());
        inventarioExistente.setLugarFechaEntrega(dto.getLugarFechaEntrega());
        inventarioExistente.setLugarFechaRecepcion(dto.getLugarFechaRecepcion());
        inventarioExistente.setFirmaSelloAutoridadEntrega(dto.getFirmaSelloAutoridadEntrega());
        inventarioExistente.setFirmaSelloAutoridadRecibe(dto.getFirmaSelloAutoridadRecibe());
        inventarioExistente.setFechaInicial(dto.getFechaInicial());
        inventarioExistente.setFechaFinal(dto.getFechaFinal());
        inventarioExistente.setEstadoConservacion(dto.getEstadoConservacion());
        inventarioExistente.setObservaciones(dto.getObservaciones());
        inventarioExistente.setEstado(dto.getEstado());
        inventarioExistente.setUsuarioModificacion(usuarioModificacion);
        
        // Actualizar el inventario
        InventarioGeneral updated = inventarioService.actualizarInventarioGeneral(inventarioExistente, dto.getDocumentoIds());
        
        return ResponseEntity.ok(mapToDTO(updated));
    }
    
    @DeleteMapping("/general/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    public ResponseEntity<Void> deleteInventarioGeneral(@PathVariable Long id) {
        inventarioService.eliminarInventarioGeneral(id);
        return ResponseEntity.noContent().build();
    }
    
    // Registro de Transferencia (Anexo N° 05)
    @PostMapping("/registro-transferencia")
    @PreAuthorize("hasAuthority('DOCUMENT_CREATE')")
    public ResponseEntity<RegistroTransferenciaDTO> crearRegistroTransferencia(@RequestBody RegistroTransferenciaDTO dto) {
        User creador = userService.getCurrentUser();
        
        RegistroTransferencia registro = new RegistroTransferencia();
        // Información básica
        registro.setNumero(dto.getNumero());
        registro.setCodigo(dto.getCodigo());
        registro.setFecha(dto.getFecha());
        registro.setDependenciaOrigen(dto.getDependenciaOrigen());
        registro.setDependenciaDestino(dto.getDependenciaDestino());
        registro.setTotalDocumentos(dto.getTotalDocumentos());
        registro.setResponsable(dto.getResponsable());
        registro.setObservaciones(dto.getObservaciones());
        
        // Estado del documento
        if (dto.getEstado() != null) {
            registro.setEstado(dto.getEstado());
        } else {
            registro.setEstado(InventarioGeneral.EstadoDocumento.BORRADOR);
        }
        
        registro.setCreador(creador);
        
        RegistroTransferencia saved = inventarioService.crearRegistroTransferencia(registro, dto.getDocumentoIds());
        
        return ResponseEntity.ok(mapRegistroToDTO(saved));
    }
    
    /**
     * Mapea una entidad RegistroTransferencia a su correspondiente DTO
     */
    private RegistroTransferenciaDTO mapRegistroToDTO(RegistroTransferencia registro) {
        RegistroTransferenciaDTO dto = new RegistroTransferenciaDTO();
        // Información básica
        dto.setId(registro.getId());
        dto.setNumero(registro.getNumero());
        dto.setCodigo(registro.getCodigo());
        dto.setFecha(registro.getFecha());
        dto.setDependenciaOrigen(registro.getDependenciaOrigen());
        dto.setDependenciaDestino(registro.getDependenciaDestino());
        dto.setTotalDocumentos(registro.getTotalDocumentos());
        dto.setResponsable(registro.getResponsable());
        dto.setObservaciones(registro.getObservaciones());
        dto.setEstado(registro.getEstado());
        
        // Campos de control y fechas
        dto.setFechaCreacion(registro.getFechaCreacion());
        dto.setFechaModificacion(registro.getFechaModificacion());
        
        // Información de usuarios
        if (registro.getCreador() != null) {
            dto.setCreadorId(registro.getCreador().getId());
            dto.setCreadorNombre(registro.getCreador().getUsername());
        }
        
        if (registro.getUsuarioModificacion() != null) {
            dto.setUsuarioModificacionId(registro.getUsuarioModificacion().getId());
            dto.setUsuarioModificacionNombre(registro.getUsuarioModificacion().getUsername());
        }
        
        // Detalles
        if (registro.getDetalles() != null && !registro.getDetalles().isEmpty()) {
            dto.setDetalles(registro.getDetalles().stream()
                    .map(this::mapDetalleRegistroToDTO)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Mapea una entidad DetalleRegistroTransferencia a su correspondiente DTO
     */
    private DetalleRegistroTransferenciaDTO mapDetalleRegistroToDTO(DetalleRegistroTransferencia detalle) {
        DetalleRegistroTransferenciaDTO dto = new DetalleRegistroTransferenciaDTO();
        dto.setId(detalle.getId());
        dto.setRegistroTransferenciaId(detalle.getRegistroTransferencia().getId());
        
        if (detalle.getDocumento() != null) {
            dto.setDocumentoId(detalle.getDocumento().getId());
            dto.setDocumentoNombre(detalle.getDocumento().getTitle());
            dto.setDocumentoTipo(detalle.getDocumento().getType() != null ? 
                    detalle.getDocumento().getType().getName() : "");
        }
        
        // Atributos del detalle según Anexo N° 05
        dto.setNumeroItem(detalle.getNumeroItem());
        dto.setObservaciones(detalle.getObservaciones());
        
        return dto;
    }
    
    @GetMapping("/registro-transferencia")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<RegistroTransferenciaDTO>> getAllRegistrosTransferencia() {
        List<RegistroTransferencia> registros = inventarioService.getAllRegistrosTransferencia();
        List<RegistroTransferenciaDTO> dtos = registros.stream()
                .map(this::mapRegistroToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/registro-transferencia/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<RegistroTransferenciaDTO> getRegistroTransferenciaById(@PathVariable Long id) {
        RegistroTransferencia registro = inventarioService.getRegistroTransferenciaById(id);
        return ResponseEntity.ok(mapRegistroToDTO(registro));
    }
    
    @GetMapping("/registro-transferencia/{id}/download")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<Resource> downloadRegistroTransferencia(@PathVariable Long id) {
        RegistroTransferencia registro = inventarioService.getRegistroTransferenciaById(id);
        
        try {
            Path filePath = Paths.get(registro.getRutaArchivo());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(resource);
            } else {
                throw new RuntimeException("No se puede leer el archivo");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al descargar el archivo", e);
        }
    }
    
    @GetMapping("/registro-transferencia/{id}/pdf")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<byte[]> downloadRegistroTransferenciaPDF(@PathVariable Long id) {
        byte[] pdfBytes = inventarioService.exportarRegistroTransferenciaPDF(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("RegistroTransferencia_" + id + ".pdf").build());
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    @GetMapping("/registro-transferencia/{id}/excel")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<byte[]> downloadRegistroTransferenciaExcel(@PathVariable Long id) {
        byte[] excelBytes = inventarioService.exportarRegistroTransferenciaExcel(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("RegistroTransferencia_" + id + ".xlsx").build());
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
    
    @PutMapping("/registro-transferencia/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<RegistroTransferenciaDTO> updateRegistroTransferencia(
            @PathVariable Long id, 
            @RequestBody RegistroTransferenciaDTO dto) {
        // Verificar que el ID en la URL coincida con el ID en el cuerpo
        if (!id.equals(dto.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Obtener el registro existente
        RegistroTransferencia registroExistente = inventarioService.getRegistroTransferenciaById(id);
        User usuarioModificacion = userService.getCurrentUser();
        
        // Actualizar los campos modificables
        registroExistente.setNumero(dto.getNumero());
        registroExistente.setCodigo(dto.getCodigo());
        registroExistente.setFecha(dto.getFecha());
        registroExistente.setDependenciaOrigen(dto.getDependenciaOrigen());
        registroExistente.setDependenciaDestino(dto.getDependenciaDestino());
        registroExistente.setTotalDocumentos(dto.getTotalDocumentos());
        registroExistente.setResponsable(dto.getResponsable());
        registroExistente.setObservaciones(dto.getObservaciones());
        registroExistente.setEstado(dto.getEstado());
        registroExistente.setUsuarioModificacion(usuarioModificacion);
        
        // Actualizar el registro
        RegistroTransferencia updated = inventarioService.actualizarRegistroTransferencia(registroExistente, dto.getDocumentoIds());
        
        return ResponseEntity.ok(mapRegistroToDTO(updated));
    }
    
    @DeleteMapping("/registro-transferencia/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    public ResponseEntity<Void> deleteRegistroTransferencia(@PathVariable Long id) {
        inventarioService.eliminarRegistroTransferencia(id);
        return ResponseEntity.noContent().build();
    }
    
    // Catálogo de Transferencia (Anexo N° 06)
    @PostMapping("/catalogo-transferencia")
    @PreAuthorize("hasAuthority('DOCUMENT_CREATE')")
    public ResponseEntity<CatalogoTransferenciaDTO> crearCatalogoTransferencia(@RequestBody CatalogoTransferenciaDTO dto) {
        User creador = userService.getCurrentUser();
        
        CatalogoTransferencia catalogo = new CatalogoTransferencia();
        // Información básica
        catalogo.setObservaciones(dto.getObservaciones());
        
        // Estado del documento
        if (dto.getEstado() != null) {
            catalogo.setEstado(dto.getEstado());
        } else {
            catalogo.setEstado(InventarioGeneral.EstadoDocumento.BORRADOR);
        }
        
        catalogo.setCreador(creador);
        
        CatalogoTransferencia saved = inventarioService.crearCatalogoTransferencia(catalogo, dto.getDocumentoIds());
        
        return ResponseEntity.ok(mapCatalogoToDTO(saved));
    }
    
    /**
     * Mapea una entidad CatalogoTransferencia a su correspondiente DTO
     */
    private CatalogoTransferenciaDTO mapCatalogoToDTO(CatalogoTransferencia catalogo) {
        CatalogoTransferenciaDTO dto = new CatalogoTransferenciaDTO();
        // Información básica
        dto.setId(catalogo.getId());
        dto.setObservaciones(catalogo.getObservaciones());
        dto.setEstado(catalogo.getEstado());
        
        // Campos de control y fechas
        dto.setFechaCreacion(catalogo.getFechaCreacion());
        dto.setFechaModificacion(catalogo.getFechaModificacion());
        
        // Información de usuarios
        if (catalogo.getCreador() != null) {
            dto.setCreadorId(catalogo.getCreador().getId());
            dto.setCreadorNombre(catalogo.getCreador().getUsername());
        }
        
        if (catalogo.getUsuarioModificacion() != null) {
            dto.setUsuarioModificacionId(catalogo.getUsuarioModificacion().getId());
            dto.setUsuarioModificacionNombre(catalogo.getUsuarioModificacion().getUsername());
        }
        
        // Detalles
        if (catalogo.getDetalles() != null && !catalogo.getDetalles().isEmpty()) {
            dto.setDetalles(catalogo.getDetalles().stream()
                    .map(this::mapDetalleCatalogoToDTO)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Mapea una entidad DetalleCatalogoTransferencia a su correspondiente DTO
     */
    private DetalleCatalogoTransferenciaDTO mapDetalleCatalogoToDTO(DetalleCatalogoTransferencia detalle) {
        DetalleCatalogoTransferenciaDTO dto = new DetalleCatalogoTransferenciaDTO();
        dto.setId(detalle.getId());
        dto.setCatalogoTransferenciaId(detalle.getCatalogoTransferencia().getId());
        
        if (detalle.getDocumento() != null) {
            dto.setDocumentoId(detalle.getDocumento().getId());
            dto.setDocumentoNombre(detalle.getDocumento().getTitle());
            dto.setDocumentoTipo(detalle.getDocumento().getType() != null ? 
                    detalle.getDocumento().getType().getName() : "");
        }
        
        // Atributos del detalle según Anexo N° 06
        dto.setNumeroItem(detalle.getNumeroItem());
        dto.setObservaciones(detalle.getObservaciones());
        
        return dto;
    }
    
    @GetMapping("/catalogo-transferencia")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<CatalogoTransferenciaDTO>> getAllCatalogosTransferencia() {
        List<CatalogoTransferencia> catalogos = inventarioService.getAllCatalogosTransferencia();
        List<CatalogoTransferenciaDTO> dtos = catalogos.stream()
                .map(this::mapCatalogoToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/catalogo-transferencia/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<CatalogoTransferenciaDTO> getCatalogoTransferenciaById(@PathVariable Long id) {
        CatalogoTransferencia catalogo = inventarioService.getCatalogoTransferenciaById(id);
        return ResponseEntity.ok(mapCatalogoToDTO(catalogo));
    }
    
    @GetMapping("/catalogo-transferencia/{id}/download")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<Resource> downloadCatalogoTransferencia(@PathVariable Long id) {
        CatalogoTransferencia catalogo = inventarioService.getCatalogoTransferenciaById(id);
        
        try {
            Path filePath = Paths.get(catalogo.getRutaArchivo());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(resource);
            } else {
                throw new RuntimeException("No se puede leer el archivo");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al descargar el archivo", e);
        }
    }
    
    @GetMapping("/catalogo-transferencia/{id}/pdf")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<byte[]> downloadCatalogoTransferenciaPDF(@PathVariable Long id) {
        byte[] pdfBytes = inventarioService.exportarCatalogoTransferenciaPDF(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("CatalogoTransferencia_" + id + ".pdf").build());
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    @GetMapping("/catalogo-transferencia/{id}/excel")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<byte[]> downloadCatalogoTransferenciaExcel(@PathVariable Long id) {
        byte[] excelBytes = inventarioService.exportarCatalogoTransferenciaExcel(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("CatalogoTransferencia_" + id + ".xlsx").build());
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
    
    @PutMapping("/catalogo-transferencia/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<CatalogoTransferenciaDTO> updateCatalogoTransferencia(
            @PathVariable Long id, 
            @RequestBody CatalogoTransferenciaDTO dto) {
        // Verificar que el ID en la URL coincida con el ID en el cuerpo
        if (!id.equals(dto.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Obtener el catálogo existente
        CatalogoTransferencia catalogoExistente = inventarioService.getCatalogoTransferenciaById(id);
        User usuarioModificacion = userService.getCurrentUser();
        
        // Actualizar los campos modificables
        catalogoExistente.setObservaciones(dto.getObservaciones());
        catalogoExistente.setEstado(dto.getEstado());
        catalogoExistente.setUsuarioModificacion(usuarioModificacion);
        
        // Actualizar el catálogo
        CatalogoTransferencia updated = inventarioService.actualizarCatalogoTransferencia(catalogoExistente, dto.getDocumentoIds());
        
        return ResponseEntity.ok(mapCatalogoToDTO(updated));
    }
    
    @DeleteMapping("/catalogo-transferencia/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    public ResponseEntity<Void> deleteCatalogoTransferencia(@PathVariable Long id) {
        inventarioService.eliminarCatalogoTransferencia(id);
        return ResponseEntity.noContent().build();
    }
}