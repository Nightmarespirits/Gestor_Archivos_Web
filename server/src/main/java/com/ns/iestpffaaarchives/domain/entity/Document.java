package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String format;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private DocumentType type;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "security_id")
    private DocumentSecurity security;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "version_number")
    private Integer versionNumber = 1;

    @ManyToMany
    @JoinTable(
        name = "document_tags",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    private Set<DocumentVersion> versions = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    private Set<AccessControl> accessControls = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}
