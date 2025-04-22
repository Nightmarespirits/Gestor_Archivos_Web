package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.entity.DocumentVersion;
import com.ns.iestpffaaarchives.domain.repository.DocumentRepository;
import com.ns.iestpffaaarchives.domain.repository.DocumentVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import org.springframework.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import com.ns.iestpffaaarchives.application.service.DocumentSpecification;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentVersionRepository documentVersionRepository) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    @Transactional(readOnly = true)
    public List<Document> getAllDocuments() {
        return documentRepository.findByIsDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByAuthor(User author) {
        return documentRepository.findByAuthor(author);
    }

    @Transactional(readOnly = true)
    public List<Document> searchDocumentsByTitle(String title) {
        return documentRepository.findByTitleContainingIgnoreCase(title);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByTag(String tagName) {
        return documentRepository.findByTagName(tagName);
    }

    @Transactional
    public Document createDocument(Document document) {
        document.setUploadDate(LocalDateTime.now());
        document.setIsDeleted(false);
        document.setVersionNumber(1);
        return documentRepository.save(document);
    }

    @Transactional
    public Document updateDocument(Document document) {
        Document existingDocument = documentRepository.findById(document.getId())
            .orElseThrow(() -> new RuntimeException("Document not found"));

        if (document.getTitle() != null) {
            existingDocument.setTitle(document.getTitle());
        }
        if (document.getDescription() != null) {
            existingDocument.setDescription(document.getDescription());
        }
        if (document.getType() != null) {
            existingDocument.setType(document.getType());
        }
        if (document.getTags() != null) {
            existingDocument.setTags(document.getTags());
        }
        if (document.getFilePath() != null) {
            existingDocument.setFilePath(document.getFilePath());
        }
        if (document.getFormat() != null) {
            existingDocument.setFormat(document.getFormat());
        }
        if (document.getVersionNumber() > 0) {
            existingDocument.setVersionNumber(document.getVersionNumber());
        }

        return documentRepository.save(existingDocument);
    }

    @Transactional
    public void softDeleteDocument(Long id) {
        documentRepository.findById(id).ifPresent(document -> {
            document.setIsDeleted(true);
            documentRepository.save(document);
        });
    }

    @Transactional
    public void hardDeleteDocument(Long id) {
        documentRepository.deleteById(id);
    }

    @Transactional
    public DocumentVersion saveDocumentVersion(DocumentVersion version) {
        return documentVersionRepository.save(version);
    }

    /**
     * Actualiza un documento con versionado. Si hay archivo nuevo, crea una versión antigua antes de actualizar.
     * Devuelve el documento actualizado o lanza excepción si algo falla.
     */
    @Transactional
    public Document updateDocumentWithVersioning(Document updatedData, MultipartFile file, String uploadDir) {
        logger.info("[updateDocumentWithVersioning] Iniciando actualización para documento id={}", updatedData.getId());
        Document existingDocument = documentRepository.findById(updatedData.getId())
            .orElseThrow(() -> new RuntimeException("Documento no encontrado: id=" + updatedData.getId()));

        // Guardar versión antigua si hay archivo nuevo
        if (file != null && !file.isEmpty()) {
            logger.info("[updateDocumentWithVersioning] Archivo nuevo detectado para documento id={}", existingDocument.getId());
            DocumentVersion version = new DocumentVersion();
            version.setDocument(existingDocument);
            version.setVersionNumber(existingDocument.getVersionNumber());
            version.setChanges("Actualización de archivo");
            version.setAuthor(existingDocument.getAuthor());
            version.setFilePath(existingDocument.getFilePath());
            saveDocumentVersion(version);
            logger.info("[updateDocumentWithVersioning] Versión antigua guardada para documento id={}", existingDocument.getId());
        }

        // Actualizar campos básicos
        if (updatedData.getTitle() != null) {
            existingDocument.setTitle(updatedData.getTitle());
        }
        if (updatedData.getDescription() != null) {
            existingDocument.setDescription(updatedData.getDescription());
        }
        if (updatedData.getType() != null) {
            existingDocument.setType(updatedData.getType());
        }
        if (updatedData.getTags() != null) {
            existingDocument.setTags(updatedData.getTags());
        }

        // Si hay archivo nuevo, actualizar filePath, formato y versionNumber
        if (file != null && !file.isEmpty()) {
            try {
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();
                java.nio.file.Files.createDirectories(uploadPath);
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = java.util.UUID.randomUUID().toString() + fileExtension;
                java.nio.file.Path filePath = uploadPath.resolve(uniqueFilename);
                java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                existingDocument.setFilePath(uniqueFilename);
                existingDocument.setFormat(file.getContentType());
                existingDocument.setVersionNumber(existingDocument.getVersionNumber() + 1);
                logger.info("[updateDocumentWithVersioning] Archivo actualizado y campos filePath, format, versionNumber actualizados para documento id={}", existingDocument.getId());
            } catch (Exception e) {
                logger.error("[updateDocumentWithVersioning] Error al guardar archivo actualizado para documento id={}", existingDocument.getId(), e);
                throw new RuntimeException("Error al guardar archivo actualizado", e);
            }
        }

        Document saved = documentRepository.save(existingDocument);
        logger.info("[updateDocumentWithVersioning] Documento actualizado correctamente id={}", saved.getId());
        return saved;
    }

    // --- MÉTODO ADVANCED SEARCH CON SPECIFICATION ---
    @Transactional(readOnly = true)
    public List<Document> advancedSearch(String title, String author, String fromDate, String toDate, Long documentTypeId, List<String> tagNames) {
        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        if (fromDate != null && !fromDate.isEmpty()) {
            from = java.time.LocalDate.parse(fromDate).atStartOfDay();
        }
        if (toDate != null && !toDate.isEmpty()) {
            to = java.time.LocalDate.parse(toDate).atTime(23, 59, 59);
        }
        Specification<com.ns.iestpffaaarchives.domain.entity.Document> spec = DocumentSpecification.advancedSearch(
            (title != null && !title.isEmpty()) ? title : null,
            (author != null && !author.isEmpty()) ? author : null,
            from,
            to,
            documentTypeId,
            (tagNames != null && !tagNames.isEmpty()) ? tagNames : null
        );
        return documentRepository.findAll(spec);
    }
}
