package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"documents"})
@EqualsAndHashCode(of = {"id"})
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unit;

    private String section;

    @Column(name = "transfer_date")
    private LocalDateTime transferDate;

    private String state;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transfer_documents",
        joinColumns = @JoinColumn(name = "transfer_id"),
        inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    private Set<Document> documents = new HashSet<>();
}
