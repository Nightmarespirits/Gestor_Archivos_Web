package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findByIsDeletedFalse();
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> getDocumentsByAuthor(User author) {
        return documentRepository.findByAuthor(author);
    }

    public List<Document> searchDocumentsByTitle(String title) {
        return documentRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Document> getDocumentsByTag(String tagName) {
        return documentRepository.findByTagName(tagName);
    }

    public Document createDocument(Document document) {
        document.setUploadDate(LocalDateTime.now());
        document.setIsDeleted(false);
        document.setVersionNumber(1);
        return documentRepository.save(document);
    }

    public Document updateDocument(Document document) {
        return documentRepository.save(document);
    }

    public void softDeleteDocument(Long id) {
        documentRepository.findById(id).ifPresent(document -> {
            document.setIsDeleted(true);
            documentRepository.save(document);
        });
    }

    public void hardDeleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}
