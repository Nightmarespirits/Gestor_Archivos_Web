package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.DocumentType;
import com.ns.iestpffaaarchives.domain.repository.DocumentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentTypeService {
    
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
    
    public Optional<DocumentType> findByName(String name) {
        return documentTypeRepository.findByName(name);
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