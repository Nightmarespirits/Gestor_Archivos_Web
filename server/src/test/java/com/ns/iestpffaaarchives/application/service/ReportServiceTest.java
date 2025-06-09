package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;
import com.ns.iestpffaaarchives.domain.entity.Transfer;
import com.ns.iestpffaaarchives.domain.repository.ReporteDocumentarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Collections;

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
        Transfer transfer = new Transfer();
        transfer.setId(1L);
        transfer.setUnit("Unit A");
        transfer.setSection("Section B");
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setState("COMPLETADA");
        transfer.setDocuments(Collections.emptySet());

        byte[] pdf = new byte[]{1,2,3};
        byte[] excel = new byte[]{4,5,6};

        ReporteDocumentario saved = reportService.saveReport(transfer, "SUMMARY", pdf, excel);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
        ReporteDocumentario fromDb = repository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getTransferId()).isEqualTo(1L);
        assertThat(fromDb.getReportType()).isEqualTo("SUMMARY");
        assertThat(fromDb.getPdfContent()).containsExactly(pdf);
        assertThat(fromDb.getExcelContent()).containsExactly(excel);
    }
}
