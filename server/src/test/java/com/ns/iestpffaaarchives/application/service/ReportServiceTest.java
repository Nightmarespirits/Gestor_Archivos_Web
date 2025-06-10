package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;
import com.ns.iestpffaaarchives.domain.entity.TransferenciaDocumental;
import com.ns.iestpffaaarchives.domain.repository.ReporteDocumentarioRepository;
import com.ns.iestpffaaarchives.domain.enums.TipoReporte;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ReportService.class)
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReporteDocumentarioRepository repository;

    @Test
    void saveReport_persistsReporteDocumentario() {
        TransferenciaDocumental transferencia = new TransferenciaDocumental();
        transferencia.setId(1L);
        transferencia.setEntidad("Unit A");
        transferencia.setSeccion("Section B");
        transferencia.setFechaTransferencia(LocalDateTime.now().toLocalDate());

        byte[] pdf = new byte[]{1,2,3};
        byte[] excel = new byte[]{4,5,6};

        ReporteDocumentario saved = reportService.saveReport(transferencia, TipoReporte.TRANSFERENCIA, pdf, excel);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
        ReporteDocumentario fromDb = repository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getTransferencia().getId()).isEqualTo(1L);
        assertThat(fromDb.getTipoReporte()).isEqualTo(TipoReporte.TRANSFERENCIA);
        assertThat(fromDb.getArchivoPdf()).containsExactly(pdf);
        assertThat(fromDb.getArchivoExcel()).containsExactly(excel);
        assertThat(fromDb.getTamanioPdf()).isEqualTo(pdf.length);
        assertThat(fromDb.getTamanioExcel()).isEqualTo(excel.length);
    }
}
