package com.ns.iestpffaaarchives.application.service.export;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ns.iestpffaaarchives.domain.entity.inventory.CatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleCatalogoTransferencia;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Servicio para exportar catálogos de transferencia a Excel y PDF
 */
@Service
public class CatalogoTransferenciaExportService implements ExportService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogoTransferenciaExportService.class);
    
    @Value("${file.inventarios-dir:uploads/inventarios}")
    private String inventariosDir;

    @Override
    public byte[] exportToExcel(Object data) {
        CatalogoTransferencia catalogo = (CatalogoTransferencia) data;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Catálogo de Transferencia");
            
            // Título del reporte
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("CATÁLOGO DE TRANSFERENCIA");
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
            
            // Información general
            org.apache.poi.ss.usermodel.Row infoRow1 = sheet.createRow(2);
            infoRow1.createCell(0).setCellValue("Unidad de Organización:");
            infoRow1.createCell(1).setCellValue(catalogo.getUnidadOrganizacion());
            
            org.apache.poi.ss.usermodel.Row infoRow2 = sheet.createRow(3);
            infoRow2.createCell(0).setCellValue("Número de Catálogo:");
            infoRow2.createCell(1).setCellValue(catalogo.getNumeroAnioRemision());
            
            org.apache.poi.ss.usermodel.Row infoRow3 = sheet.createRow(4);
            infoRow3.createCell(0).setCellValue("Fecha de Elaboración:");
            infoRow3.createCell(1).setCellValue(catalogo.getFechaCreacion() != null ? 
                    catalogo.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            
            // Crear encabezados de la tabla
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(6);
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            String[] headers = {
                    "N° Item", "Código", "Título/Asunto", 
                    "Fechas Extremas", "Autor", "Forma",
                    "Soporte", "Observaciones"
            };
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Agregar datos
            int rowNum = 7;
            
            if (catalogo.getDetalles() != null) {
                for (DetalleCatalogoTransferencia detalle : catalogo.getDetalles()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    
                    // Llenar la fila de Excel
                    row.createCell(0).setCellValue(detalle.getNumeroItem());
                    row.createCell(1).setCellValue(detalle.getCodigoClasificacion());
                    row.createCell(2).setCellValue(detalle.getNombreExpediente());
                    
                    String fechasExtremas = "";
                    if (detalle.getFechaInicial() != null && detalle.getFechaFinal() != null) {
                        fechasExtremas = detalle.getFechaInicial().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                " - " + detalle.getFechaFinal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                    row.createCell(3).setCellValue(fechasExtremas);
                    
                    row.createCell(4).setCellValue(detalle.getAlcanceContenido());
                    row.createCell(5).setCellValue(detalle.getAlcanceContenido());
                    row.createCell(6).setCellValue(detalle.getCodigoClasificacion());
                    row.createCell(7).setCellValue(detalle.getObservaciones());
                }
            }
            
            // Firmas
            int lastRow = rowNum +  2;
            org.apache.poi.ss.usermodel.Row responsableRow = sheet.createRow(lastRow);
            responsableRow.createCell(0).setCellValue("Elaborado por:");
            responsableRow.createCell(4).setCellValue("Aprobado por:");
            
            org.apache.poi.ss.usermodel.Row nombreRow = sheet.createRow(lastRow + 3);
            nombreRow.createCell(0).setCellValue(catalogo.getCreador().getFullName());
            nombreRow.createCell(4).setCellValue(catalogo.getVistoBuenoResponsable());
            
            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Error al generar archivo Excel del catálogo de transferencia", e);
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
    }

    @Override
    public byte[] exportToPdf(Object data) {
        CatalogoTransferencia catalogo = (CatalogoTransferencia) data;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Definir fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            
            // Título
            Paragraph title = new Paragraph("ANEXO N° 06", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph subtitle = new Paragraph("CATÁLOGO DE TRANSFERENCIA", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" ")); // Espacio
            
            // Información general
            document.add(new Paragraph("Unidad de Organización: " + catalogo.getUnidadOrganizacion(), normalFont));
            document.add(new Paragraph("Número de Catálogo: " + catalogo.getNumeroAnioRemision(), normalFont));
            
            String fechaElaboracion = catalogo.getFechaCreacion() != null ? 
                    catalogo.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            document.add(new Paragraph("Fecha de Elaboración: " + fechaElaboracion, normalFont));
            document.add(new Paragraph(" ")); // Espacio
            
            // Tabla de detalles
            PdfPTable table = new PdfPTable(8); // 8 columnas
            table.setWidthPercentage(100);
            
            // Definir anchos relativos
            float[] columnWidths = {5f, 8f, 15f, 12f, 12f, 10f, 8f, 12f};
            table.setWidths(columnWidths);
            
            // Encabezados de tabla
            String[] headers = {
                "N° Item", "Código", "Título/Asunto", 
                "Fechas Extremas", "Autor", "Forma",
                "Soporte", "Observaciones"
            };
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            // Detalles
            if (catalogo.getDetalles() != null) {
                for (DetalleCatalogoTransferencia detalle : catalogo.getDetalles()) {
                    // Número de ítem
                    PdfPCell itemCell = new PdfPCell(new Phrase(String.valueOf(detalle.getNumeroItem()), normalFont));
                    itemCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(itemCell);
                    
                    // Código
                    table.addCell(new PdfPCell(new Phrase(detalle.getCodigoClasificacion(), normalFont)));
                    
                    // Título/Asunto
                    table.addCell(new PdfPCell(new Phrase(detalle.getNombreExpediente(), normalFont)));
                    
                    // Fechas Extremas
                    String fechasExtremas = "";
                    if (detalle.getFechaInicial() != null && detalle.getFechaFinal() != null) {
                        fechasExtremas = detalle.getFechaInicial().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                " - " + detalle.getFechaFinal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                    PdfPCell fechasCell = new PdfPCell(new Phrase(fechasExtremas, normalFont));
                    fechasCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(fechasCell);
                    
                    // Autor
                    table.addCell(new PdfPCell(new Phrase(detalle.getNombreExpediente(), normalFont)));
                    
                    // Forma
                    table.addCell(new PdfPCell(new Phrase(detalle.getUbicacionFisica(), normalFont)));
                    
                    // Soporte
                    table.addCell(new PdfPCell(new Phrase(detalle.getAlcanceContenido(), normalFont)));
                    
                    // Observaciones
                    table.addCell(new PdfPCell(new Phrase(detalle.getObservaciones(), normalFont)));
                }
            }
            
            document.add(table);
            document.add(new Paragraph(" ")); // Espacio
            document.add(new Paragraph(" ")); // Espacio
            
            // Firmas
            PdfPTable firmasTable = new PdfPTable(2);
            firmasTable.setWidthPercentage(100);
            
            PdfPCell elaboradoLabelCell = new PdfPCell(new Phrase("Elaborado por:", boldFont));
            elaboradoLabelCell.setBorder(0);
            firmasTable.addCell(elaboradoLabelCell);
            
            PdfPCell aprobadoLabelCell = new PdfPCell(new Phrase("Aprobado por:", boldFont));
            aprobadoLabelCell.setBorder(0);
            firmasTable.addCell(aprobadoLabelCell);
            
            // Espacios para firmas
            PdfPCell elaboradoCell = new PdfPCell(new Phrase(catalogo.getCreador().getFullName(), normalFont));
            elaboradoCell.setFixedHeight(60);
            elaboradoCell.setBorder(0);
            elaboradoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            firmasTable.addCell(elaboradoCell);
            
            PdfPCell aprobadoCell = new PdfPCell(new Phrase(catalogo.getVistoBuenoResponsable(), normalFont));
            aprobadoCell.setFixedHeight(60);
            aprobadoCell.setBorder(0);
            aprobadoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            firmasTable.addCell(aprobadoCell);
            
            document.add(firmasTable);
            
        } catch (DocumentException e) {
            logger.error("Error al generar PDF del catálogo de transferencia", e);
            throw new RuntimeException("Error al generar PDF", e);
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }

    @Override
    public String getExportFilename(String baseFilename) {
        return baseFilename + "_" + UUID.randomUUID().toString();
    }
}
