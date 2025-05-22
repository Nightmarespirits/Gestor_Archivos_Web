package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    /**
     * Encuentra documentos que no estén eliminados y cuyo nivel de acceso de seguridad esté dentro del conjunto proporcionado.
     * 
     * @param permissibleAccessLevels Conjunto de niveles de acceso permitidos para el usuario actual
     * @return Lista de documentos que coinciden con los criterios
     */
    @EntityGraph(attributePaths = {"author", "type", "tags", "security"})
    @Query("SELECT d FROM Document d WHERE d.isDeleted = false AND d.security.accessLevel IN :permissibleAccessLevels")
    List<Document> findByIsDeletedFalseAndSecurityAccessLevelIn(@Param("permissibleAccessLevels") Set<String> permissibleAccessLevels);
    
    /**
     * Version paginada: Encuentra documentos que no estén eliminados y cuyo nivel de acceso de seguridad esté dentro 
     * del conjunto proporcionado, con soporte para paginación y ordenamiento.
     * 
     * @param permissibleAccessLevels Conjunto de niveles de acceso permitidos para el usuario actual
     * @param pageable Objeto que contiene información de paginación y ordenamiento
     * @return Página de documentos que coinciden con los criterios
     */
    @EntityGraph(attributePaths = {"author", "type", "tags", "security"})
    @Query("SELECT d FROM Document d WHERE d.isDeleted = false AND d.security.accessLevel IN :permissibleAccessLevels")
    Page<Document> findByIsDeletedFalseAndSecurityAccessLevelIn(
            @Param("permissibleAccessLevels") Set<String> permissibleAccessLevels,
            Pageable pageable);
            
    /**
     * Búsqueda paginada de documentos por título.
     */
    @EntityGraph(attributePaths = {"author", "type", "tags"})
    Page<Document> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title, Pageable pageable);
    
    /**
     * Búsqueda paginada de documentos por autor.
     */
    @EntityGraph(attributePaths = {"author", "type", "tags"})
    Page<Document> findByAuthorAndIsDeletedFalse(User author, Pageable pageable);

    // Búsqueda avanzada con Specification (Criteria API)
    // Elimina el método advancedSearch con @Query, lo implementaremos con Specification
}
