package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.domain.entity.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

// Repositorio para DocumentType
@Repository
interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
    boolean existsByName(String name);
}

// Servicio para DocumentType
@Service
class DocumentTypeService {
    
    private final DocumentTypeRepository documentTypeRepository;
    
    @Autowired
    public DocumentTypeService(DocumentTypeRepository documentTypeRepository) {
        this.documentTypeRepository = documentTypeRepository;
    }
    
    public List<DocumentType> getAllDocumentTypes() {
        return documentTypeRepository.findAll();
    }
    
    public Optional<DocumentType> getDocumentTypeById(Long id) {
        return documentTypeRepository.findById(id);
    }
    
    public DocumentType createDocumentType(DocumentType documentType) {
        return documentTypeRepository.save(documentType);
    }
    
    public DocumentType updateDocumentType(DocumentType documentType) {
        return documentTypeRepository.save(documentType);
    }
    
    public void deleteDocumentType(Long id) {
        documentTypeRepository.deleteById(id);
    }
    
    public boolean existsByName(String name) {
        return documentTypeRepository.existsByName(name);
    }
}

@RestController
@RequestMapping("/api/document-types")
@CrossOrigin(origins = "*")
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
        // Verificar si ya existe un tipo de documento con el mismo nombre
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
            
            // Verificar si el nuevo nombre ya existe y no es el mismo documento
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
