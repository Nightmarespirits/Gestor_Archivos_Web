package com.ns.iestpffaaarchives.domain.entity;

import com.ns.iestpffaaarchives.domain.enums.TipoReporte;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_documentarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"transfer"})
@EqualsAndHashCode(of = {"id"})
public class ReporteDocumentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;

    @Enumerated(EnumType.STRING)
    private TipoReporte tipo;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Lob
    private byte[] pdf;

    @Lob
    private byte[] excel;
}
