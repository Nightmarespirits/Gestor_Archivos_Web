package com.ns.iestpffaaarchives.infrastructure.web.dto.mapper;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.Tag;
import com.ns.iestpffaaarchives.infrastructure.web.dto.DocumentDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;

public class DocumentMapper {
    
    public static DocumentDTO toDTO(Document document) {
        if (document == null) {
            return null;
        }

        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setDescription(document.getDescription());
        dto.setFilePath(document.getFilePath());
        dto.setFormat(document.getFormat());
        dto.setUploadDate(document.getUploadDate());
        dto.setIsDeleted(document.getIsDeleted());
        dto.setVersionNumber(document.getVersionNumber());
        
        // Set author info
        if (document.getAuthor() != null) {
            dto.setAuthorName(document.getAuthor().getFullName());
            dto.setAuthorId(document.getAuthor().getId());
        }
        
        // Set document type info
        if (document.getType() != null) {
            dto.setDocumentType(document.getType().getName());
            dto.setDocumentTypeId(document.getType().getId());
        }
        
        // Set tags
        if (document.getTags() != null) {
            dto.setTags(document.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet()));
        } else {
            dto.setTags(new HashSet<>());
        }
        
        return dto;
    }
    
    public static List<DocumentDTO> toDTOList(List<Document> documents) {
        return documents.stream()
            .map(DocumentMapper::toDTO)
            .collect(Collectors.toList());
    }
}