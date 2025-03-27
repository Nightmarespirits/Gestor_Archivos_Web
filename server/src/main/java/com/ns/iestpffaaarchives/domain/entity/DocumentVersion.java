package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    private String changes;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "version_date", nullable = false)
    private LocalDateTime versionDate;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @PrePersist
    protected void onCreate() {
        versionDate = LocalDateTime.now();
    }
}
