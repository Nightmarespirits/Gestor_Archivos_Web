package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;
import com.ns.iestpffaaarchives.domain.entity.Transfer;
import com.ns.iestpffaaarchives.domain.repository.ReporteDocumentarioRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Generates PDF and Excel reports for a completed transfer and stores them
     * using {@link #saveReport(Transfer, String, byte[], byte[])}.
     *
     * @param transfer Transfer for which the report will be generated
     * @return the persisted {@link ReporteDocumentario}
     */
    @Transactional
    public ReporteDocumentario generateReportForTransfer(Transfer transfer) {
        try {
            byte[] pdf = createPdfReport(transfer);
            byte[] excel = createExcelReport(transfer);
            return saveReport(transfer, "TRANSFER", pdf, excel);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    private byte[] createExcelReport(Transfer transfer) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transfer");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Unit");
            header.createCell(2).setCellValue("Section");

            Row row = sheet.createRow(1);
            if (transfer != null) {
                if (transfer.getId() != null) {
                    row.createCell(0).setCellValue(transfer.getId());
                }
                row.createCell(1).setCellValue(
                        transfer.getUnit() != null ? transfer.getUnit() : "");
                row.createCell(2).setCellValue(
                        transfer.getSection() != null ? transfer.getSection() : "");
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createPdfReport(Transfer transfer) throws JRException, IOException {
        try (InputStream templateStream = getClass().getResourceAsStream("/reports/transfer_report.jrxml")) {
            if (templateStream == null) {
                throw new IOException("Report template not found");
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

        Map<String, Object> params = new HashMap<>();
        StringBuilder content = new StringBuilder();
        if (transfer != null) {
            content.append("Transfer ID: ")
                   .append(transfer.getId() != null ? transfer.getId() : "-");
            if (transfer.getUnit() != null) {
                content.append(" - Unit: ").append(transfer.getUnit());
            }
            if (transfer.getSection() != null) {
                content.append(" - Section: ").append(transfer.getSection());
            }
        }
        params.put("content", content.toString());

        JasperPrint print = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
        return JasperExportManager.exportReportToPdf(print);
        }
    }
}
