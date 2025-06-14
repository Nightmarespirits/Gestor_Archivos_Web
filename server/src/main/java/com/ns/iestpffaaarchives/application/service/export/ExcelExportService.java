package com.ns.iestpffaaarchives.application.service.export;

import com.ns.iestpffaaarchives.domain.entity.inventory.CatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleCatalogoTransferencia;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Servicio para exportar datos a Excel
 */
@Service
public class ExcelExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportService.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    /**
     * Exporta un Catálogo de Transferencia y sus detalles a un archivo Excel
     * 
     * @param catalogo El catálogo de transferencia a exportar
     * @param detalles Lista de detalles del catálogo
     * @return Array de bytes que representa el archivo Excel
     * @throws IOException Si ocurre un error durante la creación del archivo
     */
    public byte[] exportCatalogoToExcel(CatalogoTransferencia catalogo, List<DetalleCatalogoTransferencia> detalles) throws IOException {
        logger.info("Exportando a Excel el Catálogo de Transferencia ID: {}", catalogo.getId());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Crear hoja para la información del catálogo
            Sheet catalogoSheet = workbook.createSheet("Información General");
            
            // Estilos para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Crear encabezados y datos para la información del catálogo
            Row titleRow = catalogoSheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("CATÁLOGO DE TRANSFERENCIA");
            titleCell.setCellStyle(headerStyle);
            
            // Datos generales del catálogo
            createLabelValueRow(catalogoSheet, 2, "ID", catalogo.getId().toString(), headerStyle);
            createLabelValueRow(catalogoSheet, 3, "Título", catalogo.getCodigoReferencia(), headerStyle);
            createLabelValueRow(catalogoSheet, 4, "Código de Referencia", 
                    catalogo.getCodigoReferencia() != null ? catalogo.getCodigoReferencia() : "", headerStyle);
            createLabelValueRow(catalogoSheet, 5, "Unidad de Organización", 
                    catalogo.getUnidadOrganizacion() != null ? catalogo.getUnidadOrganizacion() : "", headerStyle);
            createLabelValueRow(catalogoSheet, 6, "Año", 
                    catalogo.getNumeroAnioRemision() != null ? catalogo.getNumeroAnioRemision().toString() : "", headerStyle);
            createLabelValueRow(catalogoSheet, 7, "Serie Documental", 
                    catalogo.getSerieDocumental() != null ? catalogo.getSerieDocumental() : "", headerStyle);
            createLabelValueRow(catalogoSheet, 8, "Soporte", 
                    catalogo.getSoporte() != null ? catalogo.getSoporte() : "", headerStyle);
            createLabelValueRow(catalogoSheet, 9, "Estado", 
                    catalogo.getEstado() != null ? catalogo.getEstado().toString() : "", headerStyle);
            createLabelValueRow(catalogoSheet, 10, "Fecha de Creación", 
                    catalogo.getFechaCreacion() != null ? DATE_FORMAT.format(catalogo.getFechaCreacion()) : "", headerStyle);
            createLabelValueRow(catalogoSheet, 11, "Fecha de Modificación", 
                    catalogo.getFechaModificacion() != null ? DATE_FORMAT.format(catalogo.getFechaModificacion()) : "", headerStyle);
            
            // Ajustar ancho de columnas
            catalogoSheet.autoSizeColumn(0);
            catalogoSheet.autoSizeColumn(1);
            
            // Crear hoja para los detalles
            Sheet detallesSheet = workbook.createSheet("Detalles");
            
            // Crear encabezados para la tabla de detalles
            Row headerRow = detallesSheet.createRow(0);
            String[] headers = {
                "N° Item", "N° Caja", "N° Tomo/Paquete", "N° Unidad Documental", 
                "Fecha Unidad Doc.", "Alcance y Contenido", "Información Adicional",
                "Cantidad de Folios", "Observaciones"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos de los detalles
            int rowNumber = 1;
            for (DetalleCatalogoTransferencia detalle : detalles) {
                Row row = detallesSheet.createRow(rowNumber++);
                
                row.createCell(0).setCellValue(detalle.getNumeroItem() != null ? detalle.getNumeroItem() : 0);
                row.createCell(1).setCellValue(detalle.getNumeroCaja() != null ? detalle.getNumeroCaja() : "");
                row.createCell(2).setCellValue(detalle.getNumeroTomoPaquete() != null ? detalle.getNumeroTomoPaquete() : "");
                row.createCell(3).setCellValue(detalle.getNumeroUnidadDocumental() != null ? detalle.getNumeroUnidadDocumental() : "");
                row.createCell(4).setCellValue(detalle.getFechaUnidadDocumental() != null ? 
                        DATE_FORMAT.format(detalle.getFechaUnidadDocumental()) : "");
                row.createCell(5).setCellValue(detalle.getAlcanceContenido() != null ? detalle.getAlcanceContenido() : "");
                row.createCell(6).setCellValue(detalle.getInformacionAdicional() != null ? detalle.getInformacionAdicional() : "");
                row.createCell(7).setCellValue(detalle.getCantidadFolios() != null ? detalle.getCantidadFolios() : 0);
                row.createCell(8).setCellValue(detalle.getObservaciones() != null ? detalle.getObservaciones() : "");
            }
            
            // Ajustar ancho de todas las columnas
            for (int i = 0; i < headers.length; i++) {
                detallesSheet.autoSizeColumn(i);
            }
            
            // Escribir el libro de trabajo a un array de bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private void createLabelValueRow(Sheet sheet, int rowIndex, String label, String value, CellStyle labelStyle) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
    }
}
