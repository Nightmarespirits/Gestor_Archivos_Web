package com.ns.iestpffaaarchives.application.service.export;

import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleRegistroTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.RegistroTransferencia;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Exporta un Registro de Transferencia (Anexo N.º 05) a Excel a partir de la
 * plantilla «Plantilla_Registro_Transferencia.xlsx».
 *
 * • La cabecera se rellena usando rangos con nombre (ver enum {@link NamedCell}).<br>
 * • La fila plantilla de detalles se marca con el rango «detalleTemplate» en la
 *   misma hoja, justo debajo de los datos generales.
 */
@Service
public class RegistroTransferenciaExportService {

    private static final Logger log = LoggerFactory.getLogger(RegistroTransferenciaExportService.class);

    private static final String TEMPLATE_LOCATION = "classpath:templates/Plantilla_Registro_Transferencia.xlsx";
    private static final String DETAILS_TEMPLATE_RANGE = "detalleTemplate";

    private final ResourceLoader resourceLoader;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());

    public RegistroTransferenciaExportService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /* ------------------------------- API -------------------------------- */

    public byte[] exportar(RegistroTransferencia registro) throws IOException {
        if (registro == null) throw new IllegalArgumentException("El registro no puede ser nulo");
        log.info("Exportando Registro de Transferencia (id={}) a Excel", registro.getId());

        Resource template = resourceLoader.getResource(TEMPLATE_LOCATION);
        try (InputStream in = template.getInputStream();
             Workbook wb = new XSSFWorkbook(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            completarCabecera(wb, registro);
            completarDetalles(wb, registro.getDetalles());

            wb.write(out);
            return out.toByteArray();
        }
    }

    /* --------------------------- Cabecera ------------------------------ */

    private void completarCabecera(Workbook wb, RegistroTransferencia r) {
        setValue(wb, NamedCell.NOMBRE_ENTIDAD,            r.getNombreEntidad());
        setValue(wb, NamedCell.UNIDAD_ORGANIZACION,       r.getUnidadOrganizacion());
        setValue(wb, NamedCell.SECCION,                   r.getSeccion());
        setValue(wb, NamedCell.NIVEL_DESCRIPCION,         r.getNivelDescripcion());
        setValue(wb, NamedCell.SERIE_DOCUMENTAL,          r.getSerieDocumental());
        setValue(wb, NamedCell.CODIGO_REFERENCIA,         r.getCodigoReferencia());
        setValue(wb, NamedCell.SOPORTE,                   r.getSoporte());
        setValue(wb, NamedCell.VOLUMEN_ML,                r.getVolumenMetrosLineales().toString());
        setValue(wb, NamedCell.RESPONSABLE_SECCION,       r.getResponsableSeccion());
        setValue(wb, NamedCell.INVENTARIO_ELABORADO_POR,  r.getInventarioElaboradoPor());
        setValue(wb, NamedCell.NUMERO_ANIO_REMISION,      r.getNumeroAnioRemision());
        setValue(wb, NamedCell.LUGAR_FECHA_ELABORACION,   r.getLugarFechaElaboracion());

        // Atributos de control
        setValue(wb, NamedCell.VISTO_BUENO_RESP,          r.getVistoBuenoResponsable());
        setValue(wb, NamedCell.FECHA_TRANSFERENCIA,       format(r.getFechaTransferencia()));
        setValue(wb, NamedCell.FECHA_CREACION,            format(r.getFechaCreacion()));
        setValue(wb, NamedCell.FECHA_MODIFICACION,        format(r.getFechaModificacion()));
        setValue(wb, NamedCell.ESTADO,                    r.getEstado() != null ? r.getEstado().name() : null);
        setValue(wb, NamedCell.VERSION,                   r.getVersion() != null ? String.valueOf(r.getVersion()) : null);
        setValue(wb, NamedCell.RUTA_ARCHIVO,              r.getRutaArchivo());
    }

    /* --------------------------- Detalles ------------------------------ */

    private void completarDetalles(Workbook wb, List<DetalleRegistroTransferencia> detalles) {
        Name tplName = wb.getName(DETAILS_TEMPLATE_RANGE);
        if (tplName == null) {
            log.warn("La plantilla no contiene el rango «{}»; se omiten detalles.", DETAILS_TEMPLATE_RANGE);
            return;
        }

        CellReference tplRef = firstCellOf(tplName.getRefersToFormula());
        Sheet sheet          = wb.getSheet(tplRef.getSheetName());
        Row templateRow      = sheet.getRow(tplRef.getRow());
        if (templateRow == null) {
            log.warn("La fila plantilla para detalles no existe (fila {}).", tplRef.getRow());
            return;
        }

        int rowIdx = templateRow.getRowNum();
        for (DetalleRegistroTransferencia d : detalles) {
            Row row = (rowIdx == templateRow.getRowNum())
                      ? templateRow
                      : cloneRow(sheet, templateRow, rowIdx);
            popularDetalle(row, d);
            rowIdx++;
        }
        if (detalles.isEmpty()) limpiarFila(templateRow);
    }

    private void popularDetalle(Row row, DetalleRegistroTransferencia d) {
        int c = 0;
        set(row, c++, d.getNumeroItem());
        set(row, c++, d.getNumeroCaja());
        set(row, c++, d.getNumeroTomoPaquete());
        set(row, c++, d.getAlcanceContenido());
        set(row, c++, d.getRangoExtremoDel());
        set(row, c++, d.getRangoExtremoAl());
        set(row, c++, d.getFechaExtremaDel());
        set(row, c++, d.getFechaExtremaAl());
        set(row, c++, d.getCantidadFolios());
        set(row, c,   d.getObservaciones());
    }

    /* ----------------------- Utilidades comunes ------------------------ */

    private CellReference firstCellOf(String formula) {
        if (formula.contains(":")) {
            AreaReference area = new AreaReference(formula, SpreadsheetVersion.EXCEL2007);
            return area.getFirstCell();
        }
        return new CellReference(formula);
    }

    private void setValue(Workbook wb, NamedCell namedCell, String value) {
        Name name = wb.getName(namedCell.rangeName);
        if (name == null) { log.debug("Rango «{}» no encontrado.", namedCell.rangeName); return; }

        CellReference ref = firstCellOf(name.getRefersToFormula());
        Sheet sheet = wb.getSheet(ref.getSheetName());
        Row row     = sheet.getRow(ref.getRow());
        Cell cell   = row.getCell(ref.getCol(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null && !value.isBlank()) cell.setCellValue(value); else cell.setBlank();
    }

    /* ---- helpers set(...) overloads null‑safe (String, Integer, BigDecimal, LocalDate) ---- */

    private void set(Row row, int col, String value) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null && !value.isBlank()) cell.setCellValue(value); else cell.setBlank();
    }
    private void set(Row row, int col, Integer value) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null) cell.setCellValue(value); else cell.setBlank();
    }
    private void set(Row row, int col, BigDecimal value) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null) cell.setCellValue(value.doubleValue()); else cell.setBlank();
    }
    private void set(Row row, int col, LocalDate date) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (date != null) cell.setCellValue(DATE_FMT.format(date)); else cell.setBlank();
    }

    /* ------------------ clone / limpiar fila plantilla ----------------- */

    private Row cloneRow(Sheet sh, Row template, int newRowIdx) {
        Row newRow = sh.createRow(newRowIdx);
        for (int i = template.getFirstCellNum(); i < template.getLastCellNum(); i++) {
            Cell src = template.getCell(i);
            Cell tgt = newRow.createCell(i);
            if (src != null) tgt.setCellStyle(src.getCellStyle());
        }
        return newRow;
    }
    private void limpiarFila(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null) cell.setBlank();
        }
    }

    /* ------------------- Enum de rangos con nombre --------------------- */

    private enum NamedCell {
        NOMBRE_ENTIDAD("nombreEntidad"),
        UNIDAD_ORGANIZACION("unidadOrganizacion"),
        SECCION("seccion"),
        NIVEL_DESCRIPCION("nivelDescripcion"),
        SERIE_DOCUMENTAL("serieDocumental"),
        CODIGO_REFERENCIA("codigoReferencia"),
        SOPORTE("soporte"),
        VOLUMEN_ML("volumenMetrosLineales"),
        RESPONSABLE_SECCION("responsableSeccion"),
        INVENTARIO_ELABORADO_POR("inventarioElaboradoPor"),
        NUMERO_ANIO_REMISION("numeroAnioRemision"),
        LUGAR_FECHA_ELABORACION("lugarFechaElaboracion"),
        VISTO_BUENO_RESP("vistoBuenoResponsable"),
        FECHA_TRANSFERENCIA("fechaTransferencia"),
        FECHA_CREACION("fechaCreacion"),
        FECHA_MODIFICACION("fechaModificacion"),
        ESTADO("estado"),
        VERSION("version"),
        RUTA_ARCHIVO("rutaArchivo");

        private final String rangeName;
        NamedCell(String name) { this.rangeName = name; }
    }

    /* ----------------------------- misc ------------------------------- */

    private String format(java.time.temporal.TemporalAccessor t) {
        if (t == null) return null;
        if (t instanceof java.time.LocalDateTime dt) return DATE_TIME.format(dt);
        if (t instanceof java.time.LocalDate     d) return DATE_FMT.format(d);
        return t.toString();
    }
}
