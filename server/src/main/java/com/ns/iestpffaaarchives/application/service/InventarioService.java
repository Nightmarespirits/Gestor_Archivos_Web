// InventarioService.java
package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.application.service.export.CatalogoTransferenciaExportService;
import com.ns.iestpffaaarchives.application.service.export.InventarioGeneralExportService;
import com.ns.iestpffaaarchives.application.service.export.RegistroTransferenciaExportService;
import com.ns.iestpffaaarchives.domain.entity.inventory.*;
import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.repository.inventory.*;
import com.ns.iestpffaaarchives.domain.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;  
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class InventarioService {
    private static final Logger logger = LoggerFactory.getLogger(InventarioService.class);
    
    @Value("${file.inventarios-dir:uploads/inventarios}")
    private String inventariosDir;
    
    private final InventarioGeneralRepository inventarioGeneralRepository;
    private final RegistroTransferenciaRepository registroTransferenciaRepository;
    private final CatalogoTransferenciaRepository catalogoTransferenciaRepository;
    private final DocumentRepository documentRepository;
    private final UserService userService;
    
    // Servicios de exportación
    private final InventarioGeneralExportService inventarioGeneralExportService;
    private final RegistroTransferenciaExportService registroTransferenciaExportService;
    private final CatalogoTransferenciaExportService catalogoTransferenciaExportService;
    
    @Autowired
    public InventarioService(
            InventarioGeneralRepository inventarioGeneralRepository,
            RegistroTransferenciaRepository registroTransferenciaRepository,
            CatalogoTransferenciaRepository catalogoTransferenciaRepository,
            DocumentRepository documentRepository,
            UserService userService,
            InventarioGeneralExportService inventarioGeneralExportService,
            RegistroTransferenciaExportService registroTransferenciaExportService,
            CatalogoTransferenciaExportService catalogoTransferenciaExportService) {
        this.inventarioGeneralRepository = inventarioGeneralRepository;
        this.registroTransferenciaRepository = registroTransferenciaRepository;
        this.catalogoTransferenciaRepository = catalogoTransferenciaRepository;
        this.documentRepository = documentRepository;
        this.userService = userService;
        this.inventarioGeneralExportService = inventarioGeneralExportService;
        this.registroTransferenciaExportService = registroTransferenciaExportService;
        this.catalogoTransferenciaExportService = catalogoTransferenciaExportService;
    }
    
    @Transactional
    public InventarioGeneral crearInventarioGeneral(InventarioGeneral inventario, List<Long> documentIds) {
        // Asegurar que el directorio exista
        Path dirPath = Paths.get(inventariosDir);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            logger.error("Error al crear directorio de inventarios", e);
            throw new RuntimeException("Error al crear directorio de inventarios", e);
        }
        
        // Generar el archivo Excel
        String fileName = "InventarioGeneral_" + UUID.randomUUID().toString() + ".xlsx";
        Path filePath = dirPath.resolve(fileName);
        
        // Crear el Excel basado en el formato Anexo N° 04
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventario General");
            
            // Título del reporte
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("INVENTARIO GENERAL DE TRANSFERENCIA");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
            
            // Información general
            Row infoRow1 = sheet.createRow(2);
            infoRow1.createCell(0).setCellValue("Unidad de Organización:");
            infoRow1.createCell(1).setCellValue(inventario.getUnidadAdministrativa());
            
            Row infoRow2 = sheet.createRow(3);
            infoRow2.createCell(0).setCellValue("Número y Año de Remisión:");
            infoRow2.createCell(1).setCellValue(inventario.getNumeroAnioRemision());
            
            Row infoRow3 = sheet.createRow(4);
            infoRow3.createCell(0).setCellValue("Sección:");
            infoRow3.createCell(1).setCellValue(inventario.getSeccion());
            
            Row infoRow4 = sheet.createRow(5);
            infoRow4.createCell(0).setCellValue("Fecha de Transferencia:");
            infoRow4.createCell(1).setCellValue(inventario.getFechaTransferencia() != null ? 
                    inventario.getFechaTransferencia().toString() : "");
            
            // Crear encabezados de la tabla
            Row headerRow = sheet.createRow(7);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            String[] headers = {
                    "N° Item", "Nombre de la Serie Documental", "Fecha Extrema Del", "Fecha Extrema Al",
                    "Tipo Unidad de Archivamiento", "Cant. Unidad", "Volumen en metros lineales", "Soporte", "Observaciones"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Agregar datos
            int rowNum = 8;
            int itemNumber = 1;
            
            for (Long docId : documentIds) {
                Document doc = documentRepository.findById(docId).orElse(null);
                if (doc != null) {
                    Row row = sheet.createRow(rowNum++);
                    
                    // Crear detalle
                    DetalleInventarioGeneral detalle = new DetalleInventarioGeneral();
                    detalle.setInventarioGeneral(inventario);
                    detalle.setDocumento(doc);
                    detalle.setNumeroItem(itemNumber);
                    detalle.setSerieDocumental(doc.getType().getName());
                    detalle.setFechaExtremaDel(doc.getUploadDate() != null ? 
                            doc.getUploadDate().toLocalDate() : null);
                    detalle.setFechaExtremaAl(doc.getUploadDate() != null ? 
                            doc.getUploadDate().toLocalDate() : null);
                    detalle.setTipoUnidadArchivamiento("Expediente");
                    detalle.setCantidadUnidadArchivamiento(1);
                    // Asumimos 0.1m lineales por documento como ejemplo
                    detalle.setVolumenMetrosLineales(new java.math.BigDecimal("0.1"));
                    detalle.setSoporte("Papel"); // Valor por defecto
                    detalle.setDescripcion(doc.getDescription());
                    detalle.setObservaciones("");
                    
                    // Llenar la fila de Excel
                    row.createCell(0).setCellValue(itemNumber++);
                    row.createCell(1).setCellValue(detalle.getSerieDocumental());
                    row.createCell(2).setCellValue(detalle.getFechaExtremaDel() != null ? 
                            detalle.getFechaExtremaDel().toString() : "");
                    row.createCell(3).setCellValue(detalle.getFechaExtremaAl() != null ? 
                            detalle.getFechaExtremaAl().toString() : "");
                    row.createCell(4).setCellValue(detalle.getTipoUnidadArchivamiento());
                    row.createCell(5).setCellValue(detalle.getCantidadUnidadArchivamiento());
                    row.createCell(6).setCellValue(detalle.getVolumenMetrosLineales() != null ? 
                            detalle.getVolumenMetrosLineales().toString() : "0");
                    row.createCell(7).setCellValue(detalle.getSoporte());
                    row.createCell(8).setCellValue(detalle.getObservaciones());
                    
                    if (inventario.getDetalles() == null) {
                        inventario.setDetalles(new java.util.ArrayList<>());
                    }
                    inventario.getDetalles().add(detalle);
                }
            }
            
            // Totales y firma
            int lastRow = rowNum + 1;
            Row totalRow = sheet.createRow(lastRow);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.createCell(6).setCellValue(inventario.getTotalVolumen() != null ? 
                    inventario.getTotalVolumen().toString() : "0");
            
            // Información de entrega y recepción
            Row entregaRow = sheet.createRow(lastRow + 3);
            entregaRow.createCell(0).setCellValue("Lugar y fecha de entrega:");
            entregaRow.createCell(1).setCellValue(inventario.getLugarFechaEntrega());
            
            Row recepcionRow = sheet.createRow(lastRow + 4);
            recepcionRow.createCell(0).setCellValue("Lugar y fecha de recepción:");
            recepcionRow.createCell(1).setCellValue(inventario.getLugarFechaRecepcion());
            
            Row firmaEntregaRow = sheet.createRow(lastRow + 6);
            firmaEntregaRow.createCell(0).setCellValue("Firma y sello de la Autoridad que entrega:");
            firmaEntregaRow.createCell(1).setCellValue(inventario.getFirmaSelloAutoridadEntrega());
            
            Row firmaRecibeRow = sheet.createRow(lastRow + 7);
            firmaRecibeRow.createCell(0).setCellValue("Firma y sello de la Autoridad que recibe:");
            firmaRecibeRow.createCell(1).setCellValue(inventario.getFirmaSelloAutoridadRecibe());
            
            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Guardar el archivo
            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                workbook.write(outputStream);
            }
            
            // Actualizar la ruta del archivo en la entidad
            inventario.setRutaArchivo(filePath.toString());
            
        } catch (IOException e) {
            logger.error("Error al generar archivo Excel del inventario general", e);
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
        
        // Establecer el estado inicial y la versión
        if (inventario.getEstado() == null) {
            inventario.setEstado(InventarioGeneral.EstadoDocumento.BORRADOR);
        }
        
        // Guardar en la base de datos
        return inventarioGeneralRepository.save(inventario);
    }
    
    /**
     * Exporta un inventario general a PDF
     * @param id ID del inventario general
     * @return Arreglo de bytes del archivo PDF
     */
    public byte[] exportarInventarioGeneralPDF(Long id) {
        InventarioGeneral inventario = getInventarioGeneralById(id);
        return inventarioGeneralExportService.exportToPdf(inventario);
    }
    
    /**
     * Exporta un inventario general a Excel
     * @param id ID del inventario general
     * @return Arreglo de bytes del archivo Excel
     */
    public byte[] exportarInventarioGeneralExcel(Long id) {
        InventarioGeneral inventario = getInventarioGeneralById(id);
        return inventarioGeneralExportService.exportToExcel(inventario);
    }   
    @Transactional
    public RegistroTransferencia crearRegistroTransferencia(RegistroTransferencia registro, List<Long> documentIds) {
        // Implementación similar para generar Excel de Registro de Transferencia
        // ...
        return registroTransferenciaRepository.save(registro);
    }
    
    @Transactional
    public CatalogoTransferencia crearCatalogoTransferencia(CatalogoTransferencia catalogo, List<Long> documentIds) {
        // Implementación similar para generar Excel de Catálogo de Transferencia
        // ...
        return catalogoTransferenciaRepository.save(catalogo);
    }
    
    // Métodos para obtener los inventarios
    public List<InventarioGeneral> getAllInventariosGenerales() {
        return inventarioGeneralRepository.findAll();
    }
    
    public List<RegistroTransferencia> getAllRegistrosTransferencia() {
        return registroTransferenciaRepository.findAll();
    }
    
    /**
     * Obtiene un registro de transferencia por su ID
     * @param id ID del registro de transferencia
     * @return El registro de transferencia encontrado
     */
    public RegistroTransferencia getRegistroTransferenciaById(Long id) {
        return registroTransferenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de Transferencia no encontrado con ID: " + id));
    }
    
    /**
     * Exporta un registro de transferencia a PDF
     * @param id ID del registro de transferencia
     * @return Arreglo de bytes del archivo PDF
     */
    public byte[] exportarRegistroTransferenciaPDF(Long id) {
        RegistroTransferencia registro = getRegistroTransferenciaById(id);
        return registroTransferenciaExportService.exportToPdf(registro);
    }
    
    /**
     * Exporta un registro de transferencia a Excel
     * @param id ID del registro de transferencia
     * @return Arreglo de bytes del archivo Excel
     */
    public byte[] exportarRegistroTransferenciaExcel(Long id) {
        RegistroTransferencia registro = getRegistroTransferenciaById(id);
        return registroTransferenciaExportService.exportToExcel(registro);
    }
    public List<CatalogoTransferencia> getAllCatalogosTransferencia() {
        return catalogoTransferenciaRepository.findAll();
    }
    
    /**
     * Obtiene un catálogo de transferencia por su ID
     * @param id ID del catálogo de transferencia
     * @return El catálogo de transferencia encontrado
     */
    public CatalogoTransferencia getCatalogoTransferenciaById(Long id) {
        return catalogoTransferenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catálogo de Transferencia no encontrado con ID: " + id));
    }
    
    /**
     * Exporta un catálogo de transferencia a PDF
     * @param id ID del catálogo de transferencia
     * @return Arreglo de bytes del archivo PDF
     */
    public byte[] exportarCatalogoTransferenciaPDF(Long id) {
        CatalogoTransferencia catalogo = getCatalogoTransferenciaById(id);
        return catalogoTransferenciaExportService.exportToPdf(catalogo);
    }
    
    /**
     * Exporta un catálogo de transferencia a Excel
     * @param id ID del catálogo de transferencia
     * @return Arreglo de bytes del archivo Excel
     */
    public byte[] exportarCatalogoTransferenciaExcel(Long id) {
        CatalogoTransferencia catalogo = getCatalogoTransferenciaById(id);
        return catalogoTransferenciaExportService.exportToExcel(catalogo);
    }
    
    public InventarioGeneral getInventarioGeneralById(Long id) {
        return inventarioGeneralRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
    }
    
    /**
     * Actualiza un inventario general existente
     * @param inventario El inventario general con los datos actualizados
     * @param documentIds Lista de IDs de documentos asociados
     * @return El inventario general actualizado
     */
    @Transactional
    public InventarioGeneral actualizarInventarioGeneral(InventarioGeneral inventario, List<Long> documentIds) {
        // Actualizar la fecha de modificación
        inventario.setFechaModificacion(LocalDateTime.now());
        
        // Si se envían nuevos documentos, actualizar los detalles
        if (documentIds != null && !documentIds.isEmpty()) {
            // Limpiamos los detalles existentes si hay nuevos documentos
            inventario.getDetalles().clear();
            
            // Generamos el nuevo archivo Excel con los documentos actualizados
            String fileName = "InventarioGeneral_" + UUID.randomUUID().toString() + ".xlsx";
            Path dirPath = Paths.get(inventariosDir);
            Path filePath = dirPath.resolve(fileName);
            
            try {
                Files.createDirectories(dirPath);
                
                // Lógica similar a crearInventarioGeneral para generar el archivo Excel
                // pero adaptada para actualización
                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Inventario General");
                    
                    // Aquí la lógica para recrear el Excel con los datos actualizados
                    // ...
                    
                    // Guardar el archivo
                    try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                        workbook.write(outputStream);
                    }
                    
                    // Actualizar la ruta del archivo en la entidad
                    inventario.setRutaArchivo(filePath.toString());
                    
                    // Agregar los nuevos detalles
                    int itemNumber = 1;
                    for (Long docId : documentIds) {
                        Document doc = documentRepository.findById(docId).orElse(null);
                        if (doc != null) {
                            DetalleInventarioGeneral detalle = new DetalleInventarioGeneral();
                            detalle.setInventarioGeneral(inventario);
                            detalle.setDocumento(doc);
                            detalle.setNumeroItem(itemNumber++);
                            detalle.setSerieDocumental(doc.getType().getName());
                            detalle.setFechaExtremaDel(doc.getUploadDate() != null ? 
                                doc.getUploadDate().toLocalDate() : null);
                            detalle.setFechaExtremaAl(doc.getUploadDate() != null ? 
                                doc.getUploadDate().toLocalDate() : null);
                            detalle.setTipoUnidadArchivamiento("Expediente");
                            detalle.setCantidadUnidadArchivamiento(1);
                            detalle.setVolumenMetrosLineales(new java.math.BigDecimal("0.1"));
                            detalle.setSoporte("Papel");
                            detalle.setDescripcion(doc.getDescription());
                            detalle.setObservaciones("");
                            
                            if (inventario.getDetalles() == null) {
                                inventario.setDetalles(new java.util.ArrayList<>());
                            }
                            inventario.getDetalles().add(detalle);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error al actualizar archivo Excel del inventario general", e);
                throw new RuntimeException("Error al actualizar archivo Excel", e);
            }
        }
        
        // Guardar en la base de datos
        return inventarioGeneralRepository.save(inventario);
    }
    
    /**
     * Elimina un inventario general por su ID
     * @param id ID del inventario general a eliminar
     */
    @Transactional
    public void eliminarInventarioGeneral(Long id) {
        InventarioGeneral inventario = getInventarioGeneralById(id);
        
        // Eliminar el archivo Excel si existe
        if (inventario.getRutaArchivo() != null) {
            try {
                Path filePath = Paths.get(inventario.getRutaArchivo());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.warn("No se pudo eliminar el archivo Excel del inventario general: {}", e.getMessage());
                // Continuamos con la eliminación aunque falle la eliminación del archivo
            }
        }
        
        // Eliminar de la base de datos
        inventarioGeneralRepository.delete(inventario);
    }
    
    /**
     * Actualiza un registro de transferencia existente
     * @param registro El registro de transferencia con los datos actualizados
     * @param documentIds Lista de IDs de documentos asociados
     * @return El registro de transferencia actualizado
     */
    @Transactional
    public RegistroTransferencia actualizarRegistroTransferencia(RegistroTransferencia registro, List<Long> documentIds) {
        // Actualizar la fecha de modificación
        registro.setFechaModificacion(LocalDateTime.now());
        
        // Si se envían nuevos documentos, actualizar los detalles
        if (documentIds != null && !documentIds.isEmpty()) {
            // Limpiamos los detalles existentes si hay nuevos documentos
            registro.getDetalles().clear();
            
            // Generamos el nuevo archivo Excel con los documentos actualizados
            String fileName = "RegistroTransferencia_" + UUID.randomUUID().toString() + ".xlsx";
            Path dirPath = Paths.get(inventariosDir);
            Path filePath = dirPath.resolve(fileName);
            
            try {
                Files.createDirectories(dirPath);
                
                // Lógica similar a crearRegistroTransferencia para generar el archivo Excel
                // pero adaptada para actualización
                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Registro de Transferencia");
                    
                    // Aquí la lógica para recrear el Excel con los datos actualizados
                    // ...
                    
                    // Guardar el archivo
                    try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                        workbook.write(outputStream);
                    }
                    
                    // Actualizar la ruta del archivo en la entidad
                    registro.setRutaArchivo(filePath.toString());
                    
                    // Agregar los nuevos detalles
                    int itemNumber = 1;
                    for (Long docId : documentIds) {
                        Document doc = documentRepository.findById(docId).orElse(null);
                        if (doc != null) {
                            DetalleRegistroTransferencia detalle = new DetalleRegistroTransferencia();
                            detalle.setRegistroTransferencia(registro);
                            detalle.setDocumento(doc);
                            detalle.setNumeroItem(itemNumber++);
                            detalle.setObservaciones("");
                            
                            if (registro.getDetalles() == null) {
                                registro.setDetalles(new java.util.ArrayList<>());
                            }
                            registro.getDetalles().add(detalle);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error al actualizar archivo Excel del registro de transferencia", e);
                throw new RuntimeException("Error al actualizar archivo Excel", e);
            }
        }
        
        // Guardar en la base de datos
        return registroTransferenciaRepository.save(registro);
    }
    
    /**
     * Elimina un registro de transferencia por su ID
     * @param id ID del registro de transferencia a eliminar
     */
    @Transactional
    public void eliminarRegistroTransferencia(Long id) {
        RegistroTransferencia registro = getRegistroTransferenciaById(id);
        
        // Eliminar el archivo Excel si existe
        if (registro.getRutaArchivo() != null) {
            try {
                Path filePath = Paths.get(registro.getRutaArchivo());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.warn("No se pudo eliminar el archivo Excel del registro de transferencia: {}", e.getMessage());
                // Continuamos con la eliminación aunque falle la eliminación del archivo
            }
        }
        
        // Eliminar de la base de datos
        registroTransferenciaRepository.delete(registro);
    }
    
    /**
     * Actualiza un catálogo de transferencia existente
     * @param catalogo El catálogo de transferencia con los datos actualizados
     * @param documentIds Lista de IDs de documentos asociados
     * @return El catálogo de transferencia actualizado
     */
    @Transactional
    public CatalogoTransferencia actualizarCatalogoTransferencia(CatalogoTransferencia catalogo, List<Long> documentIds) {
        // Actualizar la fecha de modificación
        catalogo.setFechaModificacion(LocalDateTime.now());
        
        // Si se envían nuevos documentos, actualizar los detalles
        if (documentIds != null && !documentIds.isEmpty()) {
            // Limpiamos los detalles existentes si hay nuevos documentos
            catalogo.getDetalles().clear();
            
            // Generamos el nuevo archivo Excel con los documentos actualizados
            String fileName = "CatalogoTransferencia_" + UUID.randomUUID().toString() + ".xlsx";
            Path dirPath = Paths.get(inventariosDir);
            Path filePath = dirPath.resolve(fileName);
            
            try {
                Files.createDirectories(dirPath);
                
                // Lógica similar a crearCatalogoTransferencia para generar el archivo Excel
                // pero adaptada para actualización
                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Catálogo de Transferencia");
                    
                    // Aquí la lógica para recrear el Excel con los datos actualizados
                    // ...
                    
                    // Guardar el archivo
                    try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                        workbook.write(outputStream);
                    }
                    
                    // Actualizar la ruta del archivo en la entidad
                    catalogo.setRutaArchivo(filePath.toString());
                    
                    // Agregar los nuevos detalles
                    int itemNumber = 1;
                    for (Long docId : documentIds) {
                        Document doc = documentRepository.findById(docId).orElse(null);
                        if (doc != null) {
                            DetalleCatalogoTransferencia detalle = new DetalleCatalogoTransferencia();
                            detalle.setCatalogoTransferencia(catalogo);
                            detalle.setDocumento(doc);
                            detalle.setNumeroItem(itemNumber++);
                            detalle.setObservaciones("");
                            
                            if (catalogo.getDetalles() == null) {
                                catalogo.setDetalles(new java.util.ArrayList<>());
                            }
                            catalogo.getDetalles().add(detalle);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error al actualizar archivo Excel del catálogo de transferencia", e);
                throw new RuntimeException("Error al actualizar archivo Excel", e);
            }
        }
        
        // Guardar en la base de datos
        return catalogoTransferenciaRepository.save(catalogo);
    }
    
    /**
     * Elimina un catálogo de transferencia por su ID
     * @param id ID del catálogo de transferencia a eliminar
     */
    @Transactional
    public void eliminarCatalogoTransferencia(Long id) {
        CatalogoTransferencia catalogo = getCatalogoTransferenciaById(id);
        
        // Eliminar el archivo Excel si existe
        if (catalogo.getRutaArchivo() != null) {
            try {
                Path filePath = Paths.get(catalogo.getRutaArchivo());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.warn("No se pudo eliminar el archivo Excel del catálogo de transferencia: {}", e.getMessage());
                // Continuamos con la eliminación aunque falle la eliminación del archivo
            }
        }
        
        // Eliminar de la base de datos
        catalogoTransferenciaRepository.delete(catalogo);
    }
}