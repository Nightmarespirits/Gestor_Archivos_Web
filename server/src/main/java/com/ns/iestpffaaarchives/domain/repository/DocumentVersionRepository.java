package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing DocumentVersion entities
 */
@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    
    /**
     * Encuentra todas las versiones del documento filtradas por el ID del documento
     * 
     * @param documentId the ID of the document
     * @return list of document versions
     */
    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(Long documentId);
    
    /**
     * Encuentra la version mas reciente del documento filtrada por el ID del documento
     * 
     * @param documentId the ID of the document
     * @return the latest document version
     */
    DocumentVersion findFirstByDocumentIdOrderByVersionNumberDesc(Long documentId);
    
    /**
     * Encuentra una version especifica del documento filtrada por el ID del documento y el numero de version
     * 
     * @param documentId the ID of the document
     * @param versionNumber the version number
     * @return the requested document version
     */
    DocumentVersion findByDocumentIdAndVersionNumber(Long documentId, Integer versionNumber);
}