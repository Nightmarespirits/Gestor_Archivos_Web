package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.ReportService;
import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;
import com.ns.iestpffaaarchives.domain.entity.Transfer;
import com.ns.iestpffaaarchives.domain.repository.ReporteDocumentarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReporteDocumentarioRepository repository;

    private Long reportId;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        Transfer transfer = new Transfer();
        transfer.setId(5L);
        transfer.setUnit("Unit");
        transfer.setSection("Sec");
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setState("COMPLETADA");
        transfer.setDocuments(Collections.emptySet());

        byte[] pdf = {9,9};
        byte[] excel = {8,8};
        ReporteDocumentario saved = reportService.saveReport(transfer, "SUMMARY", pdf, excel);
        reportId = saved.getId();
    }

    @Test
    @WithMockUser(authorities = "REPORT_VIEW")
    void getPdf_returnsFileForAuthorizedUser() throws Exception {
        mockMvc.perform(get("/api/reports/" + reportId + "/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"report.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @WithMockUser(authorities = "REPORT_VIEW")
    void getExcel_returnsFileForAuthorizedUser() throws Exception {
        mockMvc.perform(get("/api/reports/" + reportId + "/excel"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"report.xlsx\""))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }
}
