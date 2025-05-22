package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.DocumentSecurity;
import com.ns.iestpffaaarchives.domain.entity.Tag;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class DocumentSpecification {
    /**
     * Crea una especificación para búsqueda avanzada de documentos con filtrado por nivel de acceso.
     * 
     * @param title Título del documento (para búsqueda por coincidencia parcial)
     * @param author Nombre del autor (para búsqueda por coincidencia parcial)
     * @param fromDate Fecha desde (para filtrar por fecha de carga)
     * @param toDate Fecha hasta (para filtrar por fecha de carga)
     * @param documentTypeId ID del tipo de documento
     * @param tagNames Lista de nombres de etiquetas
     * @param permissibleAccessLevels Conjunto de niveles de acceso permitidos para el usuario actual
     * @return Especificación para la búsqueda con todos los criterios aplicados
     */
    public static Specification<Document> advancedSearch(
            String title,
            String author,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long documentTypeId,
            List<String> tagNames,
            Set<String> permissibleAccessLevels
    ) {
        return (Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.isFalse(root.get("isDeleted"));
            
            // Filtrar por niveles de acceso permitidos si se especifican
            if (permissibleAccessLevels != null && !permissibleAccessLevels.isEmpty()) {
                // Unir con DocumentSecurity para acceder a accessLevel
                Join<Document, DocumentSecurity> securityJoin = root.join("security", JoinType.INNER);
                predicate = cb.and(predicate, securityJoin.get("accessLevel").in(permissibleAccessLevels));
            }

            if (title != null && !title.isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (author != null && !author.isEmpty()) {
                Predicate nameMatch = cb.like(cb.lower(root.get("author").get("name")), "%" + author.toLowerCase() + "%");
                Predicate surnameMatch = cb.like(cb.lower(root.get("author").get("surname")), "%" + author.toLowerCase() + "%");
                predicate = cb.and(predicate, cb.or(nameMatch, surnameMatch));
            }
            if (fromDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("uploadDate"), fromDate));
            }
            if (toDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("uploadDate"), toDate));
            }
            if (documentTypeId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type").get("id"), documentTypeId));
            }
            if (tagNames != null && !tagNames.isEmpty()) {
                Join<Document, Tag> tagsJoin = root.joinSet("tags", JoinType.LEFT);
                predicate = cb.and(predicate, tagsJoin.get("name").in(tagNames));
                query.distinct(true);
            }
            return predicate;
        };
    }
    
    /**
     * Sobrecarga del método advancedSearch para mantener compatibilidad con código existente.
     * Versión sin filtrado por nivel de acceso.
     */
    public static Specification<Document> advancedSearch(
            String title,
            String author,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long documentTypeId,
            List<String> tagNames
    ) {
        return (Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.isFalse(root.get("isDeleted"));

            if (title != null && !title.isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (author != null && !author.isEmpty()) {
                Predicate nameMatch = cb.like(cb.lower(root.get("author").get("name")), "%" + author.toLowerCase() + "%");
                Predicate surnameMatch = cb.like(cb.lower(root.get("author").get("surname")), "%" + author.toLowerCase() + "%");
                predicate = cb.and(predicate, cb.or(nameMatch, surnameMatch));
            }
            if (fromDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("uploadDate"), fromDate));
            }
            if (toDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("uploadDate"), toDate));
            }
            if (documentTypeId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type").get("id"), documentTypeId));
            }
            if (tagNames != null && !tagNames.isEmpty()) {
                Join<Document, Tag> tagsJoin = root.joinSet("tags", JoinType.LEFT);
                predicate = cb.and(predicate, tagsJoin.get("name").in(tagNames));
                query.distinct(true);
            }
            return predicate;
        };
    }
}
