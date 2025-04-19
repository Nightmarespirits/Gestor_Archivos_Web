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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

@RestController
@RequestMapping("/api/documents")
// Eliminado @CrossOrigin(origins = "*") para usar la configuración global
public class DocumentController {

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
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(DocumentMapper.toDTOList(documents));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
            .map(document -> ResponseEntity.ok(DocumentMapper.toDTO(document)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentDTO>> searchDocumentsByTitle(@RequestParam String title) {
        List<Document> documents = documentService.searchDocumentsByTitle(title);
        return ResponseEntity.ok(DocumentMapper.toDTOList(documents));
    }

    @GetMapping("/tag/{tagName}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByTag(@PathVariable String tagName) {
        List<Document> documents = documentService.getDocumentsByTag(tagName);
        return ResponseEntity.ok(DocumentMapper.toDTOList(documents));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByAuthor(@PathVariable Long authorId) {
        Optional<User> authorOptional = userService.getUserById(authorId);
        
        if (authorOptional.isPresent()) {
            List<Document> documents = documentService.getDocumentsByAuthor(authorOptional.get());
            return ResponseEntity.ok(DocumentMapper.toDTOList(documents));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("authorId") Long authorId,
            @RequestParam("type") String type,
            @RequestParam(value = "tags", required = false) List<String> tagNames) {
        
        Optional<User> authorOptional = userService.getUserById(authorId);
        
        if (authorOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("El autor especificado no existe");
        }
        
        // Buscar el DocumentType por nombre
        Optional<DocumentType> documentType = documentTypeService.findByName(type);
        if (documentType.isEmpty()) {
            return ResponseEntity.badRequest().body("El tipo de documento especificado no existe");
        }
        
        try {
            // Crear directorio de carga si no existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generar nombre de archivo único
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Guardar archivo
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
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
            document.setFilePath(uniqueFilename);
            document.setFormat(file.getContentType());
            document.setAuthor(authorOptional.get());
            document.setType(documentType.get());
            document.setTags(tags);
            
            Document savedDocument = documentService.createDocument(document);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(DocumentMapper.toDTO(savedDocument));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
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

    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable Long id, @RequestBody DocumentDTO documentDetails) {
        Optional<Document> documentOptional = documentService.getDocumentById(id);
        
        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();
            
            if (documentDetails.getTitle() != null) {
                document.setTitle(documentDetails.getTitle());
            }
            if (documentDetails.getDescription() != null) {
                document.setDescription(documentDetails.getDescription());
            }
            
            // Convertir nombres de etiquetas a entidades Tag
            if (documentDetails.getTags() != null) {
                Set<Tag> tags = documentDetails.getTags().stream()
                    .map(tagName -> {
                        // Buscar la etiqueta por nombre o crear una nueva
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
            
            Document updatedDocument = documentService.updateDocument(document);
            return ResponseEntity.ok(DocumentMapper.toDTO(updatedDocument));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
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
