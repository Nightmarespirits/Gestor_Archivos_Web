package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"type", "tags", "security"})
@EqualsAndHashCode(of = {"id", "title", "filePath"})
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private User author;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = false)
    private DocumentType type;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "version_number")
    private Integer versionNumber = 1;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "security_id")
    private DocumentSecurity security;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "document_tags",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIdentityReference(alwaysAsId = true)
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (versionNumber == null) {
            versionNumber = 1;
        }
    }
}
