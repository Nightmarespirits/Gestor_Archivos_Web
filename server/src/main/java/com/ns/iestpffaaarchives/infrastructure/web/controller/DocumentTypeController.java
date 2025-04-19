package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.domain.entity.DocumentType;
import com.ns.iestpffaaarchives.application.service.DocumentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/document-types")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;
    
    @Autowired
    public DocumentTypeController(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }
    
    @GetMapping
    public ResponseEntity<List<DocumentType>> getAllDocumentTypes() {
        List<DocumentType> documentTypes = documentTypeService.getAllDocumentTypes();
        return ResponseEntity.ok(documentTypes);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentType> getDocumentTypeById(@PathVariable Long id) {
        Optional<DocumentType> documentType = documentTypeService.getDocumentTypeById(id);
        return documentType.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createDocumentType(@RequestBody DocumentType documentType) {
        if (documentTypeService.existsByName(documentType.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Ya existe un tipo de documento con ese nombre");
        }
        
        DocumentType newDocumentType = documentTypeService.createDocumentType(documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDocumentType);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocumentType(@PathVariable Long id, @RequestBody DocumentType documentTypeDetails) {
        Optional<DocumentType> documentTypeOptional = documentTypeService.getDocumentTypeById(id);
        
        if (documentTypeOptional.isPresent()) {
            DocumentType documentType = documentTypeOptional.get();
            
            if (!documentType.getName().equals(documentTypeDetails.getName()) && 
                    documentTypeService.existsByName(documentTypeDetails.getName())) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: Ya existe un tipo de documento con ese nombre");
            }
            
            documentType.setName(documentTypeDetails.getName());
            documentType.setDescription(documentTypeDetails.getDescription());
            
            DocumentType updatedDocumentType = documentTypeService.updateDocumentType(documentType);
            return ResponseEntity.ok(updatedDocumentType);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentType(@PathVariable Long id) {
        Optional<DocumentType> documentTypeOptional = documentTypeService.getDocumentTypeById(id);
        
        if (documentTypeOptional.isPresent()) {
            documentTypeService.deleteDocumentType(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
