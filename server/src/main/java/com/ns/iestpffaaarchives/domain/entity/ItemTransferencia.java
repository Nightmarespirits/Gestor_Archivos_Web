package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

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

    @Column(name = "numero_item")
    private String numeroItem;

    @Column(name = "nombre_serie_documental")
    private String nombreSerieDocumental;

    @Column(name = "fecha_extrema_desde")
    private LocalDate fechaExtremaDesde;

    @Column(name = "fecha_extrema_hasta")
    private LocalDate fechaExtremaHasta;

    @Column(name = "tipo_unidad_archivamiento")
    private String tipoUnidadArchivamiento;

    @Column(name = "cantidad_unidad_archivamiento")
    private Integer cantidadUnidadArchivamiento;

    @Column(name = "volumen_metros_lineales")
    private Double volumenMetrosLineales;

    private String soporte;

    @Column(name = "numero_folios")
    private Integer numeroFolios;

    @Column(name = "fechas_extremas")
    private String fechasExtremas;

    @Column(name = "numero_caja")
    private String numeroCaja;

    @Column(name = "numero_tomo_paquete")
    private String numeroTomoPaquete;

    @Column(name = "alcance_contenido")
    private String alcanceContenido;

    @Column(name = "rango_extremo_desde")
    private String rangoExtremoDesde;

    @Column(name = "rango_extremo_hasta")
    private String rangoExtremoHasta;

    @Column(name = "cantidad_folios")
    private Integer cantidadFolios;

    @Column(name = "numero_unidad_documental")
    private String numeroUnidadDocumental;

    @Column(name = "fecha_unidad_documental")
    private LocalDate fechaUnidadDocumental;

    @Column(name = "informacion_adicional")
    private String informacionAdicional;

    @Column(name = "ubicacion_topografica")
    private String ubicacionTopografica;

    private String observaciones;
}
