package com.ns.iestpffaaarchives.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

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
}