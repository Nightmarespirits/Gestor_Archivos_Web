package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;
import com.ns.iestpffaaarchives.domain.repository.ReporteDocumentarioRepository;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service responsible for generating transfer reports using predefined templates.
 * The resulting files are stored and referenced via ReporteDocumentario entities.
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final ReporteDocumentarioRepository reporteRepository;
    private final ResourceLoader resourceLoader;

    @Autowired
    public ReportService(ReporteDocumentarioRepository reporteRepository, ResourceLoader resourceLoader) {
        this.reporteRepository = reporteRepository;
        this.resourceLoader = resourceLoader;
    }

    @Transactional
    public ReporteDocumentario generateInventarioTransferencia(Long transferId) {
        return generateExcelReport(transferId, "inventario_transferencia_template.xlsx", "INVENTARIO_TRANSFERENCIA");
    }

    @Transactional
    public ReporteDocumentario generateRegistroTransferencia(Long transferId) {
        return generateExcelReport(transferId, "registro_transferencia_template.xlsx", "REGISTRO_TRANSFERENCIA");
    }

    @Transactional
    public ReporteDocumentario generateCatalogoTransferencia(Long transferId) {
        return generateExcelReport(transferId, "catalogo_transferencia_template.xlsx", "CATALOGO_TRANSFERENCIA");
    }

    private ReporteDocumentario generateExcelReport(Long transferId, String template, String type) {
        try (InputStream is = resourceLoader.getResource("classpath:report-templates/" + template).getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getRow(1) == null) {
                sheet.createRow(1);
            }
            if (sheet.getRow(1).getCell(0) == null) {
                sheet.getRow(1).createCell(0);
            }
            sheet.getRow(1).getCell(0).setCellValue(transferId);

            Path output = Files.createTempFile(type.toLowerCase() + "_" + transferId, ".xlsx");
            try (OutputStream os = Files.newOutputStream(output)) {
                workbook.write(os);
            }

            // JasperReports integration could transform the Excel to PDF if required.
            // The PDF generation has been omitted for brevity.

            ReporteDocumentario reporte = new ReporteDocumentario();
            reporte.setTransferId(transferId);
            reporte.setReportType(type);
            reporte.setFilePath(output.toString());
            return reporteRepository.save(reporte);
        } catch (Exception e) {
            logger.error("Error generating report {}", type, e);
            throw new RuntimeException("Error generating report " + type, e);
        }
    }
}
