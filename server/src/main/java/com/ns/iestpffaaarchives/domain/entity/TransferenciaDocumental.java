package com.ns.iestpffaaarchives.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "transferencias_documentales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaDocumental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entidad;

    @Column(name = "unidad_organica")
    private String unidadOrganica;

    private String seccion;

    @Column(name = "nivel_descripcion")
    private String nivelDescripcion;

    @Column(name = "serie_documental")
    private String serieDocumental;

    @Column(name = "codigo_referencia")
    private String codigoReferencia;

    private String soporte;

    @Column(name = "volumen_metros_lineales")
    private Double volumenMetrosLineales;

    private String responsable;

    @Column(name = "elaborado_por")
    private String elaboradoPor;

    @Column(name = "numero_anio_remision")
    private String numeroAnioRemision;

    @Column(name = "lugar_fecha")
    private String lugarFecha;

    @Column(name = "fecha_transferencia")
    private LocalDate fechaTransferencia;
}
