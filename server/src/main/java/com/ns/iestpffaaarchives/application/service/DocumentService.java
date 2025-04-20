package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.repository.DocumentRepository;
import com.ns.iestpffaaarchives.domain.repository.DocumentVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ns.iestpffaaarchives.domain.entity.DocumentVersion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

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
}
