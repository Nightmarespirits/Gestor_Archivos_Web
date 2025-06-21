package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<Resource> getPdf(@PathVariable Long id) {
        byte[] data = reportService.getPdfContent(id);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        String fileName = "report.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(data));
    }

    @GetMapping("/{id}/excel")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<Resource> getExcel(@PathVariable Long id) {
        byte[] data = reportService.getExcelContent(id);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        String fileName = "report.xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(data));
    }
}
