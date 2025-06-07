package com.ns.iestpffaaarchives.application.service.export;

/**
 * Interfaz para servicios de exportación de documentos
 */
public interface ExportService {
    /**
     * Exporta datos a formato Excel
     * @param data El objeto con datos para exportar
     * @return Arreglo de bytes con el contenido del archivo Excel
     */
    byte[] exportToExcel(Object data);
    
    /**
     * Exporta datos a formato PDF
     * @param data El objeto con datos para exportar
     * @return Arreglo de bytes con el contenido del archivo PDF
     */
    byte[] exportToPdf(Object data);
    
    /**
     * Genera un nombre de archivo para la exportación
     * @param baseFilename Nombre base del archivo
     * @return Nombre de archivo único
     */
    String getExportFilename(String baseFilename);
}
