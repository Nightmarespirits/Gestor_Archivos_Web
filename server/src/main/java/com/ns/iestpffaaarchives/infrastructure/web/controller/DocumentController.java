package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.DocumentService;
import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.User;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
// Eliminado @CrossOrigin(origins = "*") para usar la configuración global
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public DocumentController(DocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Optional<Document> document = documentService.getDocumentById(id);
        return document.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocumentsByTitle(@RequestParam String title) {
        List<Document> documents = documentService.searchDocumentsByTitle(title);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/tag/{tagName}")
    public ResponseEntity<List<Document>> getDocumentsByTag(@PathVariable String tagName) {
        List<Document> documents = documentService.getDocumentsByTag(tagName);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Document>> getDocumentsByAuthor(@PathVariable Long authorId) {
        Optional<User> authorOptional = userService.getUserById(authorId);
        
        if (authorOptional.isPresent()) {
            List<Document> documents = documentService.getDocumentsByAuthor(authorOptional.get());
            return ResponseEntity.ok(documents);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> createDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("authorId") Long authorId) {
        
        Optional<User> authorOptional = userService.getUserById(authorId);
        
        if (authorOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
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
            
            // Crear documento
            Document document = new Document();
            document.setTitle(title);
            document.setDescription(description);
            document.setFilePath(uniqueFilename);
            document.setFormat(file.getContentType());
            document.setAuthor(authorOptional.get());
            
            Document savedDocument = documentService.createDocument(document);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, @RequestBody Document documentDetails) {
        Optional<Document> documentOptional = documentService.getDocumentById(id);
        
        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();
            
            document.setTitle(documentDetails.getTitle());
            document.setDescription(documentDetails.getDescription());
            
            // Si se proporcionan etiquetas, actualizarlas
            if (documentDetails.getTags() != null) {
                document.setTags(documentDetails.getTags());
            }
            
            Document updatedDocument = documentService.updateDocument(document);
            return ResponseEntity.ok(updatedDocument);
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
