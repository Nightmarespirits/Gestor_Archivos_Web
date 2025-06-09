package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;
import com.ns.iestpffaaarchives.domain.repository.ReporteDocumentarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReporteDocumentarioRepository reportRepository;

    @Value("${report.output-dir:./reports}")
    private String reportsDir;

    @Autowired
    public ReportController(ReporteDocumentarioRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<Resource> getPdf(@PathVariable Long id) {
        MediaType mediaType = MediaType.APPLICATION_PDF;
        return serveFile(id, mediaType, "pdf");
    }

    @GetMapping("/{id}/excel")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<Resource> getExcel(@PathVariable Long id) {
        MediaType mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return serveFile(id, mediaType, "xlsx");
    }

    private ResponseEntity<Resource> serveFile(Long id, MediaType mediaType, String defaultExtension) {
        Optional<ReporteDocumentario> optionalReport = reportRepository.findById(id);
        if (optionalReport.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(reportsDir).resolve(optionalReport.get().getFilePath());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String fileName = resource.getFilename() != null ? resource.getFilename() : "report." + defaultExtension;
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .contentType(mediaType)
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
