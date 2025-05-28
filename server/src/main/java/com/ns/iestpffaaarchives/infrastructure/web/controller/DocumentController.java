package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.DocumentService;
import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.application.service.TagService;
import com.ns.iestpffaaarchives.application.service.DocumentTypeService;
import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.entity.Tag;
import com.ns.iestpffaaarchives.domain.entity.DocumentType;
import com.ns.iestpffaaarchives.infrastructure.web.dto.DocumentDTO;
import com.ns.iestpffaaarchives.infrastructure.web.dto.mapper.DocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import com.ns.iestpffaaarchives.domain.entity.DocumentSecurity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/documents")
// Eliminado @CrossOrigin(origins = "*") para usar la configuración global
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final UserService userService;
    private final TagService tagService;
    private final DocumentTypeService documentTypeService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public DocumentController(DocumentService documentService, UserService userService, 
            TagService tagService, DocumentTypeService documentTypeService) {
        this.documentService = documentService;
        this.userService = userService;
        this.tagService = tagService;
        this.documentTypeService = documentTypeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        try {
            List<Document> documents = documentService.getAllDocuments();
            if (documents.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(DocumentMapper.toDTOList(documents));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<Page<DocumentDTO>> getPaginatedDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            // Validar que la página no sea negativa
            page = Math.max(0, page);
            
            // Validar tamaño de página (mínimo 1, máximo 100)
            size = Math.max(1, Math.min(100, size));
            
            Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
                
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Document> documents = documentService.getPaginatedDocuments(pageable);
            
            Page<DocumentDTO> dtoPage = documents.map(DocumentMapper::toDTO);
            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            logger.error("Error al obtener documentos paginados: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
            .map(document -> ResponseEntity.ok(DocumentMapper.toDTO(document)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tag/{tagName}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByTag(@PathVariable String tagName) {
        List<Document> documents = documentService.getDocumentsByTag(tagName);
        return ResponseEntity.ok(DocumentMapper.toDTOList(documents));
    }

    @GetMapping("/author/{authorId}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByAuthor(@PathVariable Long authorId) {
        Optional<User> authorOptional = userService.getUserById(authorId);
        
        if (authorOptional.isPresent()) {
            List<Document> documents = documentService.getDocumentsByAuthor(authorOptional.get());
            return ResponseEntity.ok(DocumentMapper.toDTOList(documents));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<Page<DocumentDTO>> searchDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long documentTypeId,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        try {
            // Validar que la página no sea negativa
            page = Math.max(0, page);
            
            // Validar tamaño de página (mínimo 1, máximo 100)
            size = Math.max(1, Math.min(100, size));
            
            Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
                
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Document> results = documentService.advancedSearchPaginated(
                title, description, author, fromDate, toDate, documentTypeId, tags, pageable);
                
            Page<DocumentDTO> dtoPage = results.map(DocumentMapper::toDTO);
            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            logger.error("Error en búsqueda avanzada: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/search/legacy")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<DocumentDTO>> searchDocumentsLegacy(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long documentTypeId,
            @RequestParam(required = false) List<String> tags
    ) {
        List<Document> results = documentService.advancedSearch(title, author, fromDate, toDate, documentTypeId, tags);
        List<DocumentDTO> dtos = results.stream()
                .map(DocumentMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @SuppressWarnings("unchecked")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT_CREATE')")
    public ResponseEntity<?> createDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("authorId") Long authorId,
            @RequestParam("type") String type,
            @RequestParam(value = "tags", required = false) List<String> tagNames,
            @RequestParam(value = "security", required = false) String securityJson) {
        
        // Validar archivo
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }
        
        // Validar título
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El título es obligatorio");
        }
        
        // Validar autor
        Optional<User> authorOptional = userService.getUserById(authorId);
        if (authorOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("El autor especificado no existe (ID: " + authorId + ")");
        }
        
        // Validar tipo de documento
        Optional<DocumentType> documentType = documentTypeService.findByName(type);
        if (documentType.isEmpty()) {
            return ResponseEntity.badRequest().body("El tipo de documento especificado no existe (Tipo: " + type + ")");
        }
        
        // Variable para el nombre del archivo
        String uniqueFilename = null;
        
        try {
            // Crear directorio de carga si no existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            logger.info("Ruta de directorio de carga: {}", uploadPath.toAbsolutePath());
            
            // Generar nombre de archivo único
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Guardar archivo
            Path filePath = uploadPath.resolve(uniqueFilename);
            logger.info("Guardando archivo en: {}", filePath.toAbsolutePath());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Archivo guardado correctamente: {}", filePath.getFileName());
            
            // Procesar tags
            Set<Tag> tags = new HashSet<>();
            if (tagNames != null && !tagNames.isEmpty()) {
                tags = tagNames.stream()
                    .map(tagName -> {
                        List<Tag> existingTags = tagService.searchTagsByName(tagName);
                        if (!existingTags.isEmpty()) {
                            return existingTags.get(0);
                        } else {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagService.createTag(newTag);
                        }
                    })
                    .collect(Collectors.toSet());
            }
            
            // Crear documento
            Document document = new Document();
            document.setTitle(title);
            document.setDescription(description);
            document.setFilePath(uniqueFilename);  // Solo guardamos el nombre del archivo
            document.setFormat(file.getContentType());
            document.setAuthor(authorOptional.get());
            document.setType(documentType.get());
            document.setTags(tags);

            // Procesar la información de seguridad
            if (securityJson != null && !securityJson.isEmpty()) {
                try {
                    // Usar Jackson para parsear el JSON
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> securityMap = objectMapper.readValue(securityJson, Map.class);
                    
                    // Crear o actualizar el objeto de seguridad
                    DocumentSecurity security = new DocumentSecurity();
                    
                    // Verificar si hay un nivel de acceso especificado
                    if (securityMap.containsKey("accessLevel")) {
                        String accessLevel = (String) securityMap.get("accessLevel");
                        security.setAccessLevel(accessLevel);
                    } else {
                        security.setAccessLevel("Privado"); // Valor por defecto
                        logger.info("Nivel de acceso establecido por defecto: {}", security.getAccessLevel());
                    }
                    
                    security.setDocument(document);
                    document.setSecurity(security);
                    
                    logger.info("Nivel de acceso establecido: {}", security.getAccessLevel());

                } catch (Exception e) {
                    logger.error("Error al procesar JSON de seguridad: {}", e.getMessage(), e);
                    // En caso de error, el servicio se encargará de asignar el nivel predeterminado
                }
            }
            
            Document savedDocument = documentService.createDocument(document);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(DocumentMapper.toDTO(savedDocument));
            
        } catch (IOException e) {
            // Eliminar el archivo si se subió pero hubo un error al crear el documento
            try {
                logger.error("Error al procesar el archivo: {}", e.getMessage(), e);
                Path filePath = Paths.get(uploadDir).resolve(uniqueFilename);
                Files.deleteIfExists(filePath);
                logger.info("Se eliminó el archivo temporal: {}", filePath.getFileName());
            } catch (IOException deleteError) {
                // Log error de limpieza
                logger.error("Error al eliminar el archivo temporal: {}", deleteError.getMessage(), deleteError);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al procesar el archivo: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al crear el documento: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error inesperado al crear el documento: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasAuthority('FILE_DOWNLOAD')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Optional<Document> documentOptional = documentService.getDocumentById(id);
        
        if (documentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Document document = documentOptional.get();
        Path filePath = Paths.get(uploadDir).resolve(document.getFilePath());
        
        try {
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getTitle() + "\"")
                        .contentType(MediaType.parseMediaType(document.getFormat()))
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<DocumentDTO> updateDocument(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "documentTypeId", required = false) Long documentTypeId,
            @RequestParam(value = "tags", required = false) List<String> tagNames,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "security", required = false) String securityJson) {
        
        logger.info("[updateDocument] Solicitud de actualización para documento id={}", id);
        Optional<Document> documentOptional = documentService.getDocumentById(id);
        if (documentOptional.isEmpty()) {
            logger.warn("[updateDocument] Documento no encontrado id={}", id);
            return ResponseEntity.notFound().build();
        }
        Document document = documentOptional.get();
        // Actualizar campos recibidos
        if (title != null) document.setTitle(title);
        if (description != null) document.setDescription(description);
        if (documentTypeId != null) {
            DocumentType documentType = documentTypeService.getDocumentTypeById(documentTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de documento no encontrado"));
            document.setType(documentType);
        }
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = tagNames.stream()
                .map(tagName -> {
                    List<Tag> existingTags = tagService.searchTagsByName(tagName);
                    if (!existingTags.isEmpty()) {
                        return existingTags.get(0);
                    } else {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagService.createTag(newTag);
                    }
                })
                .collect(Collectors.toSet());
            document.setTags(tags);
        }

        // Procesar información de seguridad si se proporciona
        if (securityJson != null && !securityJson.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> securityMap = objectMapper.readValue(securityJson, Map.class);
                
                // Actualizar o crear el objeto de seguridad
                DocumentSecurity security = document.getSecurity();
                if (security == null) {
                    security = new DocumentSecurity();
                    security.setDocument(document);
                    document.setSecurity(security);
                }
                
                if (securityMap.containsKey("accessLevel")) {
                    String accessLevel = (String) securityMap.get("accessLevel");
                    security.setAccessLevel(accessLevel);
                    logger.info("[updateDocument] Nivel de acceso actualizado a: {}", accessLevel);
                }
            } catch (Exception e) {
                logger.error("[updateDocument] Error al procesar JSON de seguridad: {}", e.getMessage(), e);
            }
        }
        try {
            Document updatedDocument = documentService.updateDocumentWithVersioning(document, file, uploadDir);
            logger.info("[updateDocument] Documento actualizado correctamente id={}", updatedDocument.getId());
            return ResponseEntity.ok(DocumentMapper.toDTO(updatedDocument));
        } catch (Exception e) {
            logger.error("[updateDocument] Error al actualizar documento id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    public ResponseEntity<Void> softDeleteDocument(@PathVariable Long id) {
        Optional<Document> documentOptional = documentService.getDocumentById(id);
        
        if (documentOptional.isPresent()) {
            documentService.softDeleteDocument(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/permanent/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    public ResponseEntity<Void> hardDeleteDocument(@PathVariable Long id) {
        Optional<Document> documentOptional = documentService.getDocumentById(id);
        
        if (documentOptional.isPresent()) {
            // Eliminar archivo físico
            Document document = documentOptional.get();
            try {
                Path filePath = Paths.get(uploadDir).resolve(document.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Continuar con la eliminación del registro incluso si el archivo no se puede eliminar
            }
            
            documentService.hardDeleteDocument(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
