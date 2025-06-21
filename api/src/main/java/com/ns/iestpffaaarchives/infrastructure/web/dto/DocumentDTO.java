package com.ns.iestpffaaarchives.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class DocumentDTO {
    private Long id;
    private String title;
    private String description;
    private String filePath;
    private String format;
    private LocalDateTime uploadDate;
    private String authorName;
    private Long authorId;
    private String documentType;
    private Long documentTypeId;
    private Set<String> tags;
    private Boolean isDeleted;
    private Integer versionNumber;
    
    // Campo para la información de seguridad
    private Map<String, Object> security = new HashMap<>();
}