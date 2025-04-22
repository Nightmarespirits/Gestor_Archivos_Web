package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.Tag;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.List;

public class DocumentSpecification {
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
