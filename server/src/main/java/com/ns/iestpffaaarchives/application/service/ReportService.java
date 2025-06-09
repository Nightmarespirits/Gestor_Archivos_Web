package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;

import com.ns.iestpffaaarchives.domain.entity.Transfer;
import com.ns.iestpffaaarchives.domain.repository.ReporteDocumentarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReportService {

    private final ReporteDocumentarioRepository reporteRepository;

    @Autowired
    public ReportService(ReporteDocumentarioRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Transactional
    public ReporteDocumentario saveReport(Transfer transfer, String reportType,
                                          byte[] pdfContent, byte[] excelContent) {
        ReporteDocumentario reporte = new ReporteDocumentario();
        reporte.setTransferId(transfer != null ? transfer.getId() : null);
        reporte.setReportType(reportType);
        reporte.setPdfContent(pdfContent);
        reporte.setExcelContent(excelContent);
        reporte.setGeneratedAt(LocalDateTime.now());
        return reporteRepository.save(reporte);
    }

    @Transactional(readOnly = true)
    public byte[] getPdfContent(Long id) {
        return reporteRepository.findPdfContentById(id);
    }

    @Transactional(readOnly = true)
    public byte[] getExcelContent(Long id) {
        return reporteRepository.findExcelContentById(id);
    }
}
