package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items_transferencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTransferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id")
    private TransferenciaDocumental transferencia;

    private String codigo;

    private String descripcion;

    @Column(name = "numero_folios")
    private Integer numeroFolios;

    @Column(name = "fechas_extremas")
    private String fechasExtremas;

    @Column(name = "ubicacion_topografica")
    private String ubicacionTopografica;

    private String observaciones;
}
