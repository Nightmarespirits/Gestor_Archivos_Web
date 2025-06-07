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
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleInventarioGeneral;
import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral;

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
 * Servicio para exportar inventarios generales a Excel y PDF
 */
@Service
public class InventarioGeneralExportService implements ExportService {

    private static final Logger logger = LoggerFactory.getLogger(InventarioGeneralExportService.class);
    
    @Value("${file.inventarios-dir:uploads/inventarios}")
    private String inventariosDir;

    @Override
    public byte[] exportToExcel(Object data) {
        InventarioGeneral inventario = (InventarioGeneral) data;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Inventario General");
            
            // Título del reporte
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("INVENTARIO GENERAL DE TRANSFERENCIA");
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
            infoRow1.createCell(1).setCellValue(inventario.getUnidadAdministrativa());
            
            org.apache.poi.ss.usermodel.Row infoRow2 = sheet.createRow(3);
            infoRow2.createCell(0).setCellValue("Número y Año de Remisión:");
            infoRow2.createCell(1).setCellValue(inventario.getNumeroAnioRemision());
            
            org.apache.poi.ss.usermodel.Row infoRow3 = sheet.createRow(4);
            infoRow3.createCell(0).setCellValue("Sección:");
            infoRow3.createCell(1).setCellValue(inventario.getSeccion());
            
            org.apache.poi.ss.usermodel.Row infoRow4 = sheet.createRow(5);
            infoRow4.createCell(0).setCellValue("Fecha de Transferencia:");
            infoRow4.createCell(1).setCellValue(inventario.getFechaTransferencia() != null ? 
                    inventario.getFechaTransferencia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            
            // Crear encabezados de la tabla
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(7);
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            
            String[] headers = {
                    "N° Item", "Nombre de la Serie Documental", "Fecha Extrema Del", "Fecha Extrema Al",
                    "Tipo Unidad de Archivamiento", "Cant. Unidad", "Volumen en metros lineales", "Soporte", "Observaciones"
            };
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Agregar datos
            int rowNum = 8;
            
            if (inventario.getDetalles() != null) {
                for (DetalleInventarioGeneral detalle : inventario.getDetalles()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    
                    // Llenar la fila de Excel
                    row.createCell(0).setCellValue(detalle.getNumeroItem());
                    row.createCell(1).setCellValue(detalle.getSerieDocumental());
                    row.createCell(2).setCellValue(detalle.getFechaExtremaDel() != null ? 
                            detalle.getFechaExtremaDel().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
                    row.createCell(3).setCellValue(detalle.getFechaExtremaAl() != null ? 
                            detalle.getFechaExtremaAl().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
                    row.createCell(4).setCellValue(detalle.getTipoUnidadArchivamiento());
                    row.createCell(5).setCellValue(detalle.getCantidadUnidadArchivamiento());
                    row.createCell(6).setCellValue(detalle.getVolumenMetrosLineales() != null ? 
                            detalle.getVolumenMetrosLineales().toString() : "0");
                    row.createCell(7).setCellValue(detalle.getSoporte());
                    row.createCell(8).setCellValue(detalle.getObservaciones());
                }
            }
            
            // Totales y firma
            int lastRow = rowNum + 1;
            org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(lastRow);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.createCell(6).setCellValue(inventario.getTotalVolumen() != null ? 
                    inventario.getTotalVolumen().toString() : "0");
            
            // Información de entrega y recepción
            org.apache.poi.ss.usermodel.Row entregaRow = sheet.createRow(lastRow + 3);
            entregaRow.createCell(0).setCellValue("Lugar y fecha de entrega:");
            entregaRow.createCell(1).setCellValue(inventario.getLugarFechaEntrega());
            
            org.apache.poi.ss.usermodel.Row recepcionRow = sheet.createRow(lastRow + 4);
            recepcionRow.createCell(0).setCellValue("Lugar y fecha de recepción:");
            recepcionRow.createCell(1).setCellValue(inventario.getLugarFechaRecepcion());
            
            org.apache.poi.ss.usermodel.Row firmaEntregaRow = sheet.createRow(lastRow + 6);
            firmaEntregaRow.createCell(0).setCellValue("Firma y sello de la Autoridad que entrega:");
            firmaEntregaRow.createCell(1).setCellValue(inventario.getFirmaSelloAutoridadEntrega());
            
            org.apache.poi.ss.usermodel.Row firmaRecibeRow = sheet.createRow(lastRow + 7);
            firmaRecibeRow.createCell(0).setCellValue("Firma y sello de la Autoridad que recibe:");
            firmaRecibeRow.createCell(1).setCellValue(inventario.getFirmaSelloAutoridadRecibe());
            
            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Error al generar archivo Excel del inventario general", e);
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
    }

    @Override
    public byte[] exportToPdf(Object data) {
        InventarioGeneral inventario = (InventarioGeneral) data;
        
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
            Paragraph title = new Paragraph("ANEXO N° 04", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph subtitle = new Paragraph("INVENTARIO GENERAL DE TRANSFERENCIA", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" ")); // Espacio
            
            // Información general
            document.add(new Paragraph("Unidad de Organización: " + inventario.getUnidadAdministrativa(), normalFont));
            document.add(new Paragraph("Número y Año de Remisión: " + inventario.getNumeroAnioRemision(), normalFont));
            document.add(new Paragraph("Sección: " + inventario.getSeccion(), normalFont));
            
            String fechaTransferencia = inventario.getFechaTransferencia() != null ? 
                    inventario.getFechaTransferencia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            document.add(new Paragraph("Fecha de Transferencia: " + fechaTransferencia, normalFont));
            document.add(new Paragraph(" ")); // Espacio
            
            // Tabla de detalles
            PdfPTable table = new PdfPTable(9); // 9 columnas
            table.setWidthPercentage(100);
            
            // Definir anchos relativos
            float[] columnWidths = {5f, 15f, 10f, 10f, 12f, 8f, 10f, 8f, 15f};
            table.setWidths(columnWidths);
            
            // Encabezados de tabla
            String[] headers = {
                "N° Item", "Nombre de la Serie Documental", "Fecha Extrema Del", "Fecha Extrema Al",
                "Tipo Unidad de Archivamiento", "Cant. Unidad", "Volumen en metros lineales", 
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
            if (inventario.getDetalles() != null) {
                for (DetalleInventarioGeneral detalle : inventario.getDetalles()) {
                    // Número de ítem
                    PdfPCell itemCell = new PdfPCell(new Phrase(String.valueOf(detalle.getNumeroItem()), normalFont));
                    itemCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(itemCell);
                    
                    // Serie documental
                    table.addCell(new PdfPCell(new Phrase(detalle.getSerieDocumental(), normalFont)));
                    
                    // Fecha extrema del
                    String fechaDel = detalle.getFechaExtremaDel() != null ? 
                            detalle.getFechaExtremaDel().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                    PdfPCell fechaDelCell = new PdfPCell(new Phrase(fechaDel, normalFont));
                    fechaDelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(fechaDelCell);
                    
                    // Fecha extrema al
                    String fechaAl = detalle.getFechaExtremaAl() != null ? 
                            detalle.getFechaExtremaAl().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                    PdfPCell fechaAlCell = new PdfPCell(new Phrase(fechaAl, normalFont));
                    fechaAlCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(fechaAlCell);
                    
                    // Tipo unidad archivamiento
                    table.addCell(new PdfPCell(new Phrase(detalle.getTipoUnidadArchivamiento(), normalFont)));
                    
                    // Cantidad unidad
                    PdfPCell cantCell = new PdfPCell(new Phrase(String.valueOf(detalle.getCantidadUnidadArchivamiento()), normalFont));
                    cantCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cantCell);
                    
                    // Volumen
                    PdfPCell volCell = new PdfPCell(new Phrase(detalle.getVolumenMetrosLineales().toString(), normalFont));
                    volCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(volCell);
                    
                    // Soporte
                    table.addCell(new PdfPCell(new Phrase(detalle.getSoporte(), normalFont)));
                    
                    // Observaciones
                    table.addCell(new PdfPCell(new Phrase(detalle.getObservaciones(), normalFont)));
                }
            }
            
            document.add(table);
            document.add(new Paragraph(" ")); // Espacio
            
            // Total
            Paragraph total = new Paragraph("TOTAL: " + (inventario.getTotalVolumen() != null ? 
                    inventario.getTotalVolumen().toString() : "0") + " metros lineales", boldFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);
            document.add(new Paragraph(" ")); // Espacio
            
            // Información de entrega y recepción
            document.add(new Paragraph("Lugar y fecha de entrega: " + inventario.getLugarFechaEntrega(), normalFont));
            document.add(new Paragraph("Lugar y fecha de recepción: " + inventario.getLugarFechaRecepcion(), normalFont));
            document.add(new Paragraph(" ")); // Espacio
            
            // Firmas
            PdfPTable firmasTable = new PdfPTable(2);
            firmasTable.setWidthPercentage(100);
            
            PdfPCell entregaLabelCell = new PdfPCell(new Phrase("Firma y sello de la Autoridad que entrega:", boldFont));
            entregaLabelCell.setBorder(0);
            firmasTable.addCell(entregaLabelCell);
            
            PdfPCell recibeLabelCell = new PdfPCell(new Phrase("Firma y sello de la Autoridad que recibe:", boldFont));
            recibeLabelCell.setBorder(0);
            firmasTable.addCell(recibeLabelCell);
            
            // Espacios para firmas
            PdfPCell entregaCell = new PdfPCell(new Phrase(inventario.getFirmaSelloAutoridadEntrega(), normalFont));
            entregaCell.setFixedHeight(60);
            entregaCell.setBorder(0);
            entregaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            firmasTable.addCell(entregaCell);
            
            PdfPCell recibeCell = new PdfPCell(new Phrase(inventario.getFirmaSelloAutoridadRecibe(), normalFont));
            recibeCell.setFixedHeight(60);
            recibeCell.setBorder(0);
            recibeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            firmasTable.addCell(recibeCell);
            
            document.add(firmasTable);
            
        } catch (DocumentException e) {
            logger.error("Error al generar PDF del inventario general", e);
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
