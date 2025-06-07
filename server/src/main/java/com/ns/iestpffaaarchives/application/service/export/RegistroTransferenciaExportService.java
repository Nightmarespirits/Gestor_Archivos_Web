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
import com.ns.iestpffaaarchives.domain.entity.inventory.RegistroTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleRegistroTransferencia;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
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
 * Servicio para exportar registros de transferencia a Excel y PDF
 */
@Service
public class RegistroTransferenciaExportService implements ExportService {

    private static final Logger logger = LoggerFactory.getLogger(RegistroTransferenciaExportService.class);
    
    @Value("${file.inventarios-dir:uploads/inventarios}")
    private String inventariosDir;

    @Override
    public byte[] exportToExcel(Object data) {
        RegistroTransferencia registro = (RegistroTransferencia) data;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Registro de Transferencia");
            
            // Título del reporte
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REGISTRO DE TRANSFERENCIA");
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
            
            // Información general
            org.apache.poi.ss.usermodel.Row infoRow1 = sheet.createRow(2);
            infoRow1.createCell(0).setCellValue("Unidad de Organización:");
            infoRow1.createCell(1).setCellValue(registro.getUnidadAdministrativa());
            
            org.apache.poi.ss.usermodel.Row infoRow2 = sheet.createRow(3);
            infoRow2.createCell(0).setCellValue("Número de Transferencia:");
            infoRow2.createCell(1).setCellValue(registro.getNumero());
            
            org.apache.poi.ss.usermodel.Row infoRow3 = sheet.createRow(4);
            infoRow3.createCell(0).setCellValue("Fecha de Transferencia:");
            infoRow3.createCell(1).setCellValue(registro.getFechaTransferencia() != null ? 
                    registro.getFechaTransferencia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            
            // Crear encabezados de la tabla
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(6);
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            String[] headers = {
                    "N° Item", "Código", "Nombre de la Serie Documental", 
                    "Años Extremos", "Descripción", "Ubicación",
                    "Volumen", "Observaciones"
            };
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Agregar datos
            int rowNum = 7;
            
            if (registro.getDetalles() != null) {
                for (DetalleRegistroTransferencia detalle : registro.getDetalles()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    
                    // Llenar la fila de Excel
                    row.createCell(0).setCellValue(detalle.getNumeroItem());
                    row.createCell(1).setCellValue(detalle.getNumeroTomoPaquete());
                    row.createCell(2).setCellValue(detalle.getNumeroCaja());
                    
                    String anosExtremos = "";
                    if (detalle.getFechaExtremaDel() != null && detalle.getFechaExtremaAl() != null) {
                        anosExtremos = detalle.getFechaExtremaDel().format(DateTimeFormatter.ofPattern("yyyy")) + 
                                " - " + detalle.getFechaExtremaAl().format(DateTimeFormatter.ofPattern("yyyy"));
                    }
                    row.createCell(3).setCellValue(anosExtremos);
                    
                    row.createCell(4).setCellValue(detalle.getId());
                    row.createCell(5).setCellValue(detalle.getCantidadFolios());
                    row.createCell(6).setCellValue(detalle.getAlcanceContenido() != null ? 
                            detalle.getAlcanceContenido().toString() : "0");
                    row.createCell(7).setCellValue(detalle.getObservaciones());
                }
            }
            
            // Firmas
            int lastRow = rowNum + 2;
            org.apache.poi.ss.usermodel.Row firmaRow = sheet.createRow(lastRow);
            firmaRow.createCell(0).setCellValue("Responsable de la Entrega:");
            firmaRow.createCell(4).setCellValue("Responsable de la Recepción:");
            
            org.apache.poi.ss.usermodel.Row nombreRow = sheet.createRow(lastRow + 3);
            nombreRow.createCell(0).setCellValue(registro.getResponsableEntrega().getFullName());
            nombreRow.createCell(4).setCellValue(registro.getResponsableRecepcion().getFullName());
            
            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Error al generar archivo Excel del registro de transferencia", e);
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
    }

    @Override
    public byte[] exportToPdf(Object data) {
        RegistroTransferencia registro = (RegistroTransferencia) data;
        
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
            Paragraph title = new Paragraph("ANEXO N° 05", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph subtitle = new Paragraph("REGISTRO DE TRANSFERENCIA", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" ")); // Espacio
            
            // Información general
            document.add(new Paragraph("Unidad de Organización: " + registro.getUnidadAdministrativa(), normalFont));
            document.add(new Paragraph("Número de Transferencia: " + registro.getNumero(), normalFont));
            
            String fechaTransferencia = registro.getFechaTransferencia() != null ? 
                    registro.getFechaTransferencia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            document.add(new Paragraph("Fecha de Transferencia: " + fechaTransferencia, normalFont));
            document.add(new Paragraph(" ")); // Espacio
            
            // Tabla de detalles
            PdfPTable table = new PdfPTable(8); // 8 columnas
            table.setWidthPercentage(100);
            
            // Definir anchos relativos
            float[] columnWidths = {5f, 8f, 15f, 10f, 15f, 10f, 8f, 12f};
            table.setWidths(columnWidths);
            
            // Encabezados de tabla
            String[] headers = {
                "N° Item", "Código", "Nombre de la Serie Documental", 
                "Años Extremos", "Descripción", "Ubicación",
                "Volumen", "Observaciones"
            };
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            // Detalles
            if (registro.getDetalles() != null) {
                for (DetalleRegistroTransferencia detalle : registro.getDetalles()) {
                    // Número de ítem
                    PdfPCell itemCell = new PdfPCell(new Phrase(String.valueOf(detalle.getNumeroItem()), normalFont));
                    itemCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(itemCell);
                    
                    // Código
                    table.addCell(new PdfPCell(new Phrase(detalle.getNumeroTomoPaquete(), normalFont)));
                    
                    // Serie documental
                    table.addCell(new PdfPCell(new Phrase(detalle.getCantidadFolios().toString() , normalFont)));
                    
                    // Años Extremos
                    String anosExtremos = "";
                    if (detalle.getFechaExtremaDel() != null && detalle.getFechaExtremaAl() != null) {
                        anosExtremos = detalle.getFechaExtremaDel().format(DateTimeFormatter.ofPattern("yyyy")) + 
                                " - " + detalle.getFechaExtremaAl().format(DateTimeFormatter.ofPattern("yyyy"));
                    }
                    PdfPCell anosCell = new PdfPCell(new Phrase(anosExtremos, normalFont));
                    anosCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(anosCell);
                    
                    // Descripción
                    table.addCell(new PdfPCell(new Phrase(detalle.getAlcanceContenido(), normalFont)));
                    
                    // Ubicación
                    table.addCell(new PdfPCell(new Phrase(detalle.getDocumento().getTitle(), normalFont)));
                    
                    // Volumen
                    PdfPCell volCell = new PdfPCell(new Phrase(detalle.getNumeroCaja() != null ? 
                            detalle.getNumeroCaja().toString() : "0", normalFont));
                    volCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(volCell);
                    
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
            
            PdfPCell entregaLabelCell = new PdfPCell(new Phrase("Responsable de la Entrega:", boldFont));
            entregaLabelCell.setBorder(0);
            firmasTable.addCell(entregaLabelCell);
            
            PdfPCell recibeLabelCell = new PdfPCell(new Phrase("Responsable de la Recepción:", boldFont));
            recibeLabelCell.setBorder(0);
            firmasTable.addCell(recibeLabelCell);
            
            // Espacios para firmas
            PdfPCell entregaCell = new PdfPCell(new Phrase(registro.getResponsableEntrega().getFullName(), normalFont));
            entregaCell.setFixedHeight(60);
            entregaCell.setBorder(0);
            entregaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            firmasTable.addCell(entregaCell);
            
            PdfPCell recibeCell = new PdfPCell(new Phrase(registro.getResponsableRecepcion().getFullName(), normalFont));
            recibeCell.setFixedHeight(60);
            recibeCell.setBorder(0);
            recibeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            firmasTable.addCell(recibeCell);
            
            document.add(firmasTable);
            
        } catch (DocumentException e) {
            logger.error("Error al generar PDF del registro de transferencia", e);
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
