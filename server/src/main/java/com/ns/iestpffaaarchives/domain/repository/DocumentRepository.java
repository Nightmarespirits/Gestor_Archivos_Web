package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    @EntityGraph(attributePaths = {"author", "type", "tags"})
    List<Document> findByIsDeletedFalse();
    
    @EntityGraph(attributePaths = {"author", "type", "tags"})
    Optional<Document> findById(Long id);
    
    @EntityGraph(attributePaths = {"author", "type", "tags"})
    List<Document> findByAuthor(User author);
    
    @EntityGraph(attributePaths = {"author", "type", "tags"})
    List<Document> findByTitleContainingIgnoreCase(String title);
    
    @EntityGraph(attributePaths = {"author", "type", "tags"})
    @Query("SELECT d FROM Document d JOIN d.tags t WHERE t.name = :tagName AND d.isDeleted = false")
    List<Document> findByTagName(String tagName);

    // Búsqueda avanzada con Specification (Criteria API)
    // Elimina el método advancedSearch con @Query, lo implementaremos con Specification
}
