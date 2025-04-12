package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.DocumentService;
import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

// Repositorio para Metadata
@Repository
interface MetadataRepository extends JpaRepository<Metadata, Long> {
    List<Metadata> findByDocument(Document document);
    void deleteByDocument(Document document);
}

// Servicio para Metadata
@Service
class MetadataService {
    
    private final MetadataRepository metadataRepository;
    
    @Autowired
    public MetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }
    
    public List<Metadata> getAllMetadata() {
        return metadataRepository.findAll();
    }
    
    public Optional<Metadata> getMetadataById(Long id) {
        return metadataRepository.findById(id);
    }
    
    public List<Metadata> getMetadataByDocument(Document document) {
        return metadataRepository.findByDocument(document);
    }
    
    public Metadata createMetadata(Metadata metadata) {
        return metadataRepository.save(metadata);
    }
    
    public Metadata updateMetadata(Metadata metadata) {
        return metadataRepository.save(metadata);
    }
    
    public void deleteMetadata(Long id) {
        metadataRepository.deleteById(id);
    }
    
    public void deleteMetadataByDocument(Document document) {
        metadataRepository.deleteByDocument(document);
    }
}

@RestController
@RequestMapping("/api/metadata")
// Eliminado @CrossOrigin(origins = "*") para usar la configuración global
public class MetadataController {

    private final MetadataService metadataService;
    private final DocumentService documentService;
    
    @Autowired
    public MetadataController(MetadataService metadataService, DocumentService documentService) {
        this.metadataService = metadataService;
        this.documentService = documentService;
    }
    
    @GetMapping
    public ResponseEntity<List<Metadata>> getAllMetadata() {
        List<Metadata> metadata = metadataService.getAllMetadata();
        return ResponseEntity.ok(metadata);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Metadata> getMetadataById(@PathVariable Long id) {
        Optional<Metadata> metadata = metadataService.getMetadataById(id);
        return metadata.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<Metadata>> getMetadataByDocument(@PathVariable Long documentId) {
        Optional<Document> documentOptional = documentService.getDocumentById(documentId);
        
        if (documentOptional.isPresent()) {
            List<Metadata> metadata = metadataService.getMetadataByDocument(documentOptional.get());
            return ResponseEntity.ok(metadata);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/document/{documentId}")
    public ResponseEntity<?> createMetadata(@PathVariable Long documentId, @RequestBody Metadata metadata) {
        Optional<Document> documentOptional = documentService.getDocumentById(documentId);
        
        if (documentOptional.isPresent()) {
            metadata.setDocument(documentOptional.get());
            Metadata newMetadata = metadataService.createMetadata(metadata);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMetadata);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMetadata(@PathVariable Long id, @RequestBody Metadata metadataDetails) {
        Optional<Metadata> metadataOptional = metadataService.getMetadataById(id);
        
        if (metadataOptional.isPresent()) {
            Metadata metadata = metadataOptional.get();
            
            metadata.setKeywords(metadataDetails.getKeywords());
            metadata.setDepartment(metadataDetails.getDepartment());
            metadata.setExpirationDate(metadataDetails.getExpirationDate());
            
            Metadata updatedMetadata = metadataService.updateMetadata(metadata);
            return ResponseEntity.ok(updatedMetadata);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetadata(@PathVariable Long id) {
        Optional<Metadata> metadataOptional = metadataService.getMetadataById(id);
        
        if (metadataOptional.isPresent()) {
            metadataService.deleteMetadata(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/document/{documentId}")
    public ResponseEntity<Void> deleteMetadataByDocument(@PathVariable Long documentId) {
        Optional<Document> documentOptional = documentService.getDocumentById(documentId);
        
        if (documentOptional.isPresent()) {
            metadataService.deleteMetadataByDocument(documentOptional.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
