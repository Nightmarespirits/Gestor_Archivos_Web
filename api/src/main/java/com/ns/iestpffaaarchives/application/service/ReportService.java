package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;
import com.ns.iestpffaaarchives.domain.enums.TipoReporte;
import com.ns.iestpffaaarchives.domain.repository.ReporteDocumentarioRepository;
import com.ns.iestpffaaarchives.domain.entity.TransferenciaDocumental;
import com.ns.iestpffaaarchives.domain.entity.ItemTransferencia;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
@Slf4j
public class ReportService {

    private final ReporteDocumentarioRepository reporteRepository;

    @Autowired
    public ReportService(ReporteDocumentarioRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Transactional
    public ReporteDocumentario saveReport(TransferenciaDocumental transferencia,
                                          TipoReporte reportType,
                                          byte[] archivoPdf, byte[] archivoExcel) {
        ReporteDocumentario reporte = new ReporteDocumentario();
        reporte.setTransferencia(transferencia);
        reporte.setTipoReporte(reportType);
        long pdfSize = archivoPdf != null ? archivoPdf.length : 0;
        long excelSize = archivoExcel != null ? archivoExcel.length : 0;
        log.info("PDF size: {} bytes, Excel size: {} bytes", pdfSize, excelSize);
        reporte.setArchivoPdf(archivoPdf);
        reporte.setArchivoExcel(archivoExcel);
        reporte.setTamanioPdf(pdfSize != 0 ? pdfSize : null);
        reporte.setTamanioExcel(excelSize != 0 ? excelSize : null);
        reporte.setFilePath("DB");
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
     * using {@link #saveReport(Transfer, TipoReporte, byte[], byte[])}.
     *
     * @param transfer Transfer for which the report will be generated
     * @return the persisted {@link ReporteDocumentario}
     */
    @Transactional
    public ReporteDocumentario generateReportForTransfer(Transfer transfer) {
        try {
            byte[] pdf = createPdfReport(null, null);
            byte[] excel = createExcelReport(null, null);
            return saveReport(null, TipoReporte.TRANSFERENCIA, pdf, excel);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    public ReporteDocumentario generateReportForTransfer(TransferenciaDocumental transferencia,
                                                          List<ItemTransferencia> items) {
        try {
            byte[] pdf = createPdfReport(transferencia, items);
            byte[] excel = createExcelReport(transferencia, items);
            return saveReport(transferencia, TipoReporte.TRANSFERENCIA, pdf, excel);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    private byte[] createExcelReport(TransferenciaDocumental transferencia,
                                     List<ItemTransferencia> items) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transferencia");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Entidad");
            header.createCell(1).setCellValue("Unidad Organica");
            header.createCell(2).setCellValue("Seccion");

            Row row = sheet.createRow(1);
            if (transferencia != null) {
                row.createCell(0).setCellValue(
                        transferencia.getEntidad() != null ? transferencia.getEntidad() : "");
                row.createCell(1).setCellValue(
                        transferencia.getUnidadOrganica() != null ? transferencia.getUnidadOrganica() : "");
                row.createCell(2).setCellValue(
                        transferencia.getSeccion() != null ? transferencia.getSeccion() : "");
            }

            int rowIdx = 3;
            Row itemHeader = sheet.createRow(rowIdx++);
            itemHeader.createCell(0).setCellValue("Codigo");
            itemHeader.createCell(1).setCellValue("Descripcion");
            itemHeader.createCell(2).setCellValue("Folios");
            itemHeader.createCell(3).setCellValue("Fechas Extremas");
            itemHeader.createCell(4).setCellValue("Ubicacion");
            itemHeader.createCell(5).setCellValue("Observaciones");

            if (items != null) {
                for (ItemTransferencia it : items) {
                    Row r = sheet.createRow(rowIdx++);
                    r.createCell(0).setCellValue(it.getCodigo() != null ? it.getCodigo() : "");
                    r.createCell(1).setCellValue(it.getDescripcion() != null ? it.getDescripcion() : "");
                    r.createCell(2).setCellValue(it.getNumeroFolios() != null ? it.getNumeroFolios() : 0);
                    r.createCell(3).setCellValue(it.getFechasExtremas() != null ? it.getFechasExtremas() : "");
                    r.createCell(4).setCellValue(it.getUbicacionTopografica() != null ? it.getUbicacionTopografica() : "");
                    r.createCell(5).setCellValue(it.getObservaciones() != null ? it.getObservaciones() : "");
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createPdfReport(TransferenciaDocumental transferencia,
                                   List<ItemTransferencia> items) throws JRException, IOException {
        try (InputStream templateStream = getClass().getResourceAsStream("/reports/transferencia_documental.jrxml")) {
            if (templateStream == null) {
                throw new IOException("Report template not found");
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            Map<String, Object> params = new HashMap<>();
            if (transferencia != null) {
                params.put("entidad", transferencia.getEntidad());
                params.put("unidadOrganica", transferencia.getUnidadOrganica());
                params.put("seccion", transferencia.getSeccion());
            }

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                    items != null ? items : List.of());
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, dataSource);
            return JasperExportManager.exportReportToPdf(print);
        }
    }
}
